package nl.uva.morlb.rg.agent.momcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.uva.morlb.rg.agent.model.BenchmarkReward;
import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.experiment.Judge;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;
import nl.uva.morlb.util.Util;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

/**
 * Multi-Objective Monte-Carlo Tree Search
 */
public class MOMCTSAgent implements AgentInterface {

    /** The search tree used by our tree walks **/
    private final SearchTree mSearchTree = new SearchTree();

    /** The RL-Glue multi-objective task spec **/
    private TaskSpecVRLGLUE3 mTaskSpec;

    /** The list of available actions defined by the environment **/
    private final List<DiscreteAction> mAvailableActions = new LinkedList<DiscreteAction>();

    /*
     * State specific values
     */
    /** The current inventory **/
    private int[] mInventory;

    /*
     * Tree walk values
     */

    /** The accumulated reward over the whole episode **/
    private BenchmarkReward mR_u;

    /** The pareto front **/
    private SolutionSet mParetoFront;

    /** The reference point for the hypervolume indicator **/
    private double[] mReferencePoint;

    /**
     * Defines the random walk phase
     */
    private enum RandomWalkPhase {
        OUT,
        STARTED,
        IN
    }

    /** Are we in the random walk phase **/
    private RandomWalkPhase mRandomWalk = RandomWalkPhase.OUT;

    @Override
    public void agent_init(final String taskSpec) {
        mTaskSpec = new TaskSpecVRLGLUE3(taskSpec);
        mParetoFront = new SolutionSet(mTaskSpec.getNumOfObjectives());

        for(int action = mTaskSpec.getDiscreteActionRange(0).getMin(); action <= mTaskSpec.getDiscreteActionRange(0).getMax(); action++) {
            mAvailableActions.add(DiscreteAction.values()[action]);
        }

        mInventory = new int[mTaskSpec.getNumOfObjectives() -1];
        mReferencePoint = new double[mTaskSpec.getNumOfObjectives()];

        //Fill the reference point
        mReferencePoint[0] = -100;
        for(int i = 1; i < mReferencePoint.length; ++i) {
            mReferencePoint[i] = -1;
        }
    }

    @Override
    public Action agent_start(final Observation observation) {
        mRandomWalk = RandomWalkPhase.OUT;
        resetInventory();
        State currentState = generateState(observation, mInventory);

        if(!mSearchTree.isInitialised()) {
            mSearchTree.initialise(currentState);
        } else {
            mSearchTree.reset();
        }

        //start a new r_u
        mR_u = new BenchmarkReward(new double[mTaskSpec.getNumOfObjectives()]);

        return treeWalk(currentState).convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        mR_u = handleReward(reward);

        State currentState = generateState(observation, mInventory);

        return treeWalk(currentState).convertToRLGlueAction();
    }

    private BenchmarkReward handleReward(final Reward reward) {
        //Calculate the current inventory
        for(int i = 1; i < reward.doubleArray.length; ++i) {
            if(reward.doubleArray[i] != 0) {
                mInventory[i-1]++;

                if(mInventory[i-1] == 2)
                    System.out.println("Picked up second ressource");
            }
        }

        return mR_u.add(new BenchmarkReward(reward.doubleArray));
    }

    private DiscreteAction treeWalk(final State currentState) {
        DiscreteAction resultingAction = null;

        //Add Progressive Widening condition here (Sec. 2.2)
        if(mRandomWalk == RandomWalkPhase.IN || mRandomWalk == RandomWalkPhase.STARTED ) {
            if(mRandomWalk == RandomWalkPhase.STARTED ) {
                //Tree building step 2, save the resulting state
                mSearchTree.completeTreeBuilding(currentState);
                mRandomWalk = RandomWalkPhase.IN;
            }

            resultingAction = randomWalk();
        } else if(!mSearchTree.isLeafNode() && !progressiveWidening()) {

            //TODO perform maximization over action using the paretofront projection

            List<DiscreteAction> availableActions = mSearchTree.getPerformedActionsForCurrentNode();
            resultingAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));
            mSearchTree.performActionOnCurrentNode(resultingAction);
        } else {

            //TODO Use RAVE
            List<DiscreteAction> nonAvailableActions = mSearchTree.getPerformedActionsForCurrentNode();
            List<DiscreteAction> availableActions = new ArrayList<DiscreteAction>(mAvailableActions);
            availableActions.removeAll(nonAvailableActions);
            System.out.println(availableActions.get(0).name());
            resultingAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));

            //Tree building step 1, save the action
            mSearchTree.saveTreeBuildingAction(resultingAction);

            mRandomWalk = RandomWalkPhase.STARTED;
        }

        return resultingAction;
    }

    /**
     * Get the next random walk action
     * @return The next action determined by random walk
     */
    private DiscreteAction randomWalk() {
        return mAvailableActions.get(Util.RNG.nextInt(mAvailableActions.size()));
    }

    /**
     * Calculates if the progressive widening condition is met
     * @return True if we should do progressive widening, false if not
     */
    private boolean progressiveWidening() {
        int v_s = mSearchTree.getCurrentNode().getVisitationCount();

        return Math.pow(v_s, 0.5) >= mSearchTree.getCurrentNode().getAmountOfChildren()
                && mSearchTree.getCurrentNode().getAmountOfChildren() < mAvailableActions.size();
    }

    @Override
    public void agent_end(final Reward reward) {
        mR_u = handleReward(reward);

        if(mRandomWalk == RandomWalkPhase.STARTED ) {
            //Tree building step 2, save the resulting state
            //We do not get an observation for the end state so we create a fake end state to fit our datastructure
            Observation fakeEndstateObservation = new Observation();
            fakeEndstateObservation.doubleArray = new double[2];
            fakeEndstateObservation.doubleArray[0] = fakeEndstateObservation.doubleArray[1] = -1;

            mSearchTree.completeTreeBuilding(generateState(fakeEndstateObservation, mInventory));
        }

        Solution currentSolution = new Solution(mR_u.getRewardVector());

        if(!mParetoFront.isDominated(currentSolution) && mParetoFront.addSolution(currentSolution)) {

            System.out.print( mParetoFront +" -> ");
            mParetoFront.pruneDominatedSolutions();
            System.out.print(mParetoFront +" Hypervolume: ");
            System.out.println(Judge.hypervolume(mParetoFront));
        }

        mRandomWalk = RandomWalkPhase.OUT;
        mR_u = null;
    }

    @Override
    public String agent_message(final String message) {
        return null;
    }

    @Override
    public void agent_cleanup() {

        System.out.println(mSearchTree.info());
    }


    /**
     * Generate the current state from the observation and the current inventory
     *
     * @param observation The current observation
     * @return The current state
     */
    public State generateState(final Observation observation, final int[] inventory) {
        double[] observationArray = observation.doubleArray;

        Location currentLocation = new Location(observationArray[0], observationArray[1]);
        return new State(currentLocation, Arrays.copyOf(inventory, inventory.length));
    }

    /**
     * The next action form our random policy
     * @return The next action
     */
    public DiscreteAction getRandomAction() {
        return DiscreteAction.values()[Util.RNG.nextInt(5)];
    }

    /**
     * Resets the inventory to empty
     */
    private void resetInventory() {
        for(int i = 0; i < mInventory.length; ++i) {
            mInventory[i] = 0;
        }
    }

}
