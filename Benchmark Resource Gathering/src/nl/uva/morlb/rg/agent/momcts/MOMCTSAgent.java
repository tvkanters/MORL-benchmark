package nl.uva.morlb.rg.agent.momcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.uva.morlb.rg.agent.model.BenchmarkReward;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.State;
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

    public static double[] sInitialReward;

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
     * RL_GLUE values
     */

    private static final int CONVERGENCE_THRESHOLD = 15;

    private int mConvergenceCounter = CONVERGENCE_THRESHOLD;

    private double mHypervolume = Double.NEGATIVE_INFINITY;

    /*
     * Tree walk values
     */

    /** The action history of the current tree walk  **/
    private final List<DiscreteAction> mActionHistory = new LinkedList<DiscreteAction>();

    /** The state history of the current tree walk **/
    private final List<State> mStateHistory = new LinkedList<State>();

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

        mInventory = new boolean[mTaskSpec.getNumOfObjectives() -1];
        mReferencePoint = new double[mTaskSpec.getNumOfObjectives()];

        //Fill the reference point
        mReferencePoint[0] = -100;
        for(int i = 1; i < mReferencePoint.length; ++i) {
            mReferencePoint[i] = -1;
        }

        sInitialReward = new double[mTaskSpec.getNumOfObjectives()];
        for(int i = 0; i < sInitialReward.length; ++i) {
            sInitialReward[i] = -100;
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

        DiscreteAction actionToTake = treeWalk(currentState);
        mActionHistory.add(actionToTake);
        mStateHistory.add(currentState);

        return actionToTake.convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        mR_u = handleReward(reward);
        State currentState = generateState(observation, mInventory);

        DiscreteAction actionToTake = treeWalk(currentState);

        if(mRandomWalk != RandomWalkPhase.IN) {
            mActionHistory.add(actionToTake);
            mStateHistory.add(currentState);
        }

        return actionToTake.convertToRLGlueAction();
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
            //            resultingAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));

            double bestSum = Double.NEGATIVE_INFINITY;
            for(DiscreteAction action : availableActions) {
                final double currentSum = mSearchTree.getCurrentNode().getRewardForAction(action).getSum();
                if(bestSum < currentSum) {
                    bestSum = currentSum;
                    resultingAction = action;
                }
            }

            mSearchTree.performActionOnCurrentNode(resultingAction);
        } else {

            //TODO Use RAVE
            List<DiscreteAction> nonAvailableActions = mSearchTree.getPerformedActionsForCurrentNode();
            List<DiscreteAction> availableActions = new ArrayList<DiscreteAction>(mAvailableActions);
            availableActions.removeAll(nonAvailableActions);

            if(availableActions.size() != 0) {
                resultingAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));
            } else {
                resultingAction = mAvailableActions.get(Util.RNG.nextInt(mAvailableActions.size()));
            }
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
            //We do not get an observation for the end state so we create a fake end state to fit our data structure
            Observation fakeEndstateObservation = new Observation();
            fakeEndstateObservation.doubleArray = new double[2];
            fakeEndstateObservation.doubleArray[0] = fakeEndstateObservation.doubleArray[1] = -1;

            mSearchTree.completeTreeBuilding(generateState(fakeEndstateObservation, mInventory));
        }

        //Update r*head*_s,a
        for(int historyPosition = 0; historyPosition < mStateHistory.size(); ++historyPosition) {
            TreeNode toEvaluateNode = mSearchTree.getNodeForState(mStateHistory.get(historyPosition));
            DiscreteAction takenAction = mActionHistory.get(historyPosition);

            BenchmarkReward oldReward = toEvaluateNode.getRewardForAction(takenAction);
            int actionCounter = toEvaluateNode.getNumOfTimesActionWasTaken(takenAction);

            BenchmarkReward newReward = oldReward.mult(actionCounter).add(mR_u).mult(1.0d / (actionCounter +1));
            toEvaluateNode.setRewardForAction(takenAction, newReward);

            toEvaluateNode.increaseActionCounterFor(takenAction);
        }

        mStateHistory.clear();
        mActionHistory.clear();

        //Build pareto front
        Solution currentSolution = mR_u.toSolution();

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
        switch (message) {
            case "isConverged":
                final double hypervolume = Judge.hypervolume(mParetoFront);
                if(hypervolume == mHypervolume) {
                    mConvergenceCounter--;

                    if(mConvergenceCounter == 0) {
                        return "true";
                    }
                } else {
                    mHypervolume = hypervolume;
                    mConvergenceCounter = CONVERGENCE_THRESHOLD;
                }

                return "false";
            case "getSolutionSet":
                return mParetoFront.toString();
            default:
                return "Cannot parse message";
        }
    }

    @Override
    public void agent_cleanup() {
        resetInventory();

        mStateHistory.clear();
        mActionHistory.clear();

        mRandomWalk = RandomWalkPhase.OUT;
        mR_u = null;

        mSearchTree.clear();

        //        Observation fakeTestObs= new Observation();
        //        fakeTestObs.doubleArray = new double[2];
        //        fakeTestObs.doubleArray[0] = 0;
        //        fakeTestObs.doubleArray[1] = 1;
        //
        //        State fakeTestState = generateState(fakeTestObs, mInventory);
        //        System.out.println(fakeTestState);
        //        TreeNode toPrint = mSearchTree.getNodeForState(fakeTestState);
        //        for(DiscreteAction action : mAvailableActions) {
        //            System.out.println(action.name() +" " +toPrint.getRewardForAction(action));
        //        }
        //
        //        resetInventory();
        //
        //        fakeTestObs= new Observation();
        //        fakeTestObs.doubleArray = new double[2];
        //        fakeTestObs.doubleArray[0] = 0;
        //        fakeTestObs.doubleArray[1] = 2;
        //
        //        fakeTestState = generateState(fakeTestObs, mInventory);
        //        System.out.println();
        //        System.out.println(fakeTestState);
        //        toPrint = mSearchTree.getNodeForState(fakeTestState);
        //        for(DiscreteAction action : mAvailableActions) {
        //            System.out.println(action.name() +" " +toPrint.getRewardForAction(action));
        //        }
        //
        //
        //        fakeTestObs= new Observation();
        //        fakeTestObs.doubleArray = new double[2];
        //        fakeTestObs.doubleArray[0] = 0;
        //        fakeTestObs.doubleArray[1] = 3;
        //
        //        fakeTestState = generateState(fakeTestObs, mInventory);
        //        System.out.println();
        //        System.out.println(fakeTestState);
        //        toPrint = mSearchTree.getNodeForState(fakeTestState);
        //        for(DiscreteAction action : mAvailableActions) {
        //            System.out.println(action.name() +" " +toPrint.getRewardForAction(action));
        //        }

    }

    /**
     * Calculate the appropriate reward and the current inventory based on the last observed reward
     * @param reward The last observed reward
     * @return The last observed reward
     */
    private BenchmarkReward handleReward(final Reward reward) {
        //Calculate the current inventory
        for(int i = 1; i < reward.doubleArray.length; ++i) {
            if(reward.doubleArray[i] != 0) {
                mInventory[i-1] = true;
            }
        }

        return mR_u.add(new BenchmarkReward(reward.doubleArray));
    }

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
