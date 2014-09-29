package nl.uva.morlb.rg.agent.momcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jmetal.qualityIndicator.Hypervolume;
import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.agent.model.StateValue;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
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
    private boolean[] mInventory;

    /*
     * Tree walk values
     */

    /** The accumulated reward over the whole episode **/
    private StateValue mR_u;

    /** The pareto front **/
    private final List<StateValue> mParetoFront = new ArrayList<StateValue>();

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

        for(int action = mTaskSpec.getDiscreteActionRange(0).getMin(); action <= mTaskSpec.getDiscreteActionRange(0).getMax(); action++) {
            mAvailableActions.add(DiscreteAction.values()[action]);
        }

        mInventory = new boolean[mTaskSpec.getNumOfObjectives() -1];
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
        mR_u = new StateValue(new double[mTaskSpec.getNumOfObjectives()]);

        return treeWalk(currentState).convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        mR_u = mR_u.add(new StateValue(reward.doubleArray));

        //Calculate the current inventory
        for(int i = 1; i < reward.doubleArray.length; ++i) {
            if(reward.doubleArray[i] != 0) {
                mInventory[i-1] = true;
            }
        }

        State currentState = generateState(observation, mInventory);

        return treeWalk(currentState).convertToRLGlueAction();
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
        } else if(!mSearchTree.isLeafNode() && !progressiveWidening()) { //TODO && mSearchTree. DO Progressive widening

            //TODO perform maximization over action using the paretofront projection

            List<DiscreteAction> availableActions = mSearchTree.getPerformedActionsForCurrentNode();
            resultingAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));
            mSearchTree.performActionOnCurrentNode(resultingAction);
        } else {

            //TODO Use RAVE
            List<DiscreteAction> nonAvailableActions = mSearchTree.getPerformedActionsForCurrentNode();
            List<DiscreteAction> availableActions = new ArrayList<DiscreteAction>(mAvailableActions);
            availableActions.removeAll(nonAvailableActions);

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

        return Math.pow(v_s, 1/2) >= mSearchTree.getCurrentNode().getAmountOfChildren() && mSearchTree.getCurrentNode().getAmountOfChildren() < mAvailableActions.size();
    }

    @Override
    public void agent_end(final Reward reward) {
        System.out.println(mSearchTree.info());
        System.out.println(mR_u);

        //TODO calculate the pareto front, it seems like metal can't handle 0 values for objective rewards
        Hypervolume hypervolume = new Hypervolume();
        final double[][] solutionSetDoubleArray = new double[mParetoFront.size()][mTaskSpec.getNumOfObjectives()];
        for(int paretoPoint = 0; paretoPoint < mParetoFront.size(); paretoPoint++) {
            for(int rewardPosition = 0; rewardPosition < mTaskSpec.getNumOfObjectives(); rewardPosition++) {
                solutionSetDoubleArray[paretoPoint][rewardPosition] = mParetoFront.get(paretoPoint).getRewardForObjective(rewardPosition);
            }
        }

        System.out.println("Hypervolume indicator" +hypervolume.calculateHypervolume(solutionSetDoubleArray, mParetoFront.size(), mTaskSpec.getNumOfObjectives()));

        mParetoFront.add(mR_u);

        mRandomWalk = RandomWalkPhase.OUT;
        mR_u = null;
    }

    @Override
    public String agent_message(final String message) {
        return null;
    }

    @Override
    public void agent_cleanup() {}


    /**
     * Generate the current state from the observation and the current inventory
     *
     * @param observation The current observation
     * @return The current state
     */
    public State generateState(final Observation observation, final boolean[] inventory) {
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
            mInventory[i] = false;
        }
    }

}
