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
    /** The current value of the hypervolume indicator **/
    private final double mHypervolume = Double.NEGATIVE_INFINITY;

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

    /** The current hypervolume indicator **/
    private double mHypervolumeIndicator = Double.NEGATIVE_INFINITY;

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

            List<DiscreteAction> availableActions = mSearchTree.getPerformedActionsForCurrentNode();
            resultingAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));

            if(availableActions.size() > 1) {

                double bestLookingActionValue = Double.NEGATIVE_INFINITY;
                for(DiscreteAction consideredAction : availableActions) {
                    final BenchmarkReward actionReward = mSearchTree.getCurrentNode().getRewardForAction(consideredAction);

                    if(mParetoFront.isDominated(new Solution(actionReward.getRewardVector()))) {
                        final double actionValue = mHypervolumeIndicator - calculateParetoProjection(actionReward).sub(actionReward).getLength();

                        if(actionValue > bestLookingActionValue) {
                            bestLookingActionValue = actionValue;
                            resultingAction = consideredAction;
                        }
                    } else {
                        if(mHypervolume > bestLookingActionValue) {
                            bestLookingActionValue = mHypervolume;
                            resultingAction = consideredAction;
                        }
                    }
                }

            } else {
                resultingAction = availableActions.get(0);
            }

            mSearchTree.performActionOnCurrentNode(resultingAction);
        } else {
            //Using RAVE at this position did not improve the performance of the algorithm but rather made it worse.

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

        return Math.pow(v_s, 0.5) >= mSearchTree.getCurrentNode().getAmountOfChildren(); //&& mSearchTree.getCurrentNode().getAmountOfChildren() < mAvailableActions.size();
    }

    /**
     * Calculates the projection on the pareto front
     * @param reward The reward to project
     * @return The projected point
     */
    private BenchmarkReward calculateParetoProjection(final BenchmarkReward reward) {
        double maxGradient = Double.NEGATIVE_INFINITY;

        for(Solution solution : mParetoFront.getSolutions()) {
            BenchmarkReward gradients = pointwiseDivision (solution, reward);
            final double gradient = gradients.getMinimumRewardEntry();

            if(maxGradient < gradient) {
                maxGradient = gradient;
            }
        }

        return reward.mult(maxGradient);
    }

    /**
     * Calculates the pointwise division of a reward with a solutiont
     * @param solution The solution set
     * @param reward The reward
     * @return A pointwise division from the reward with the solution
     */
    private BenchmarkReward pointwiseDivision(final Solution solution, final BenchmarkReward reward) {
        double[] result = new double[reward.getDimension()];
        for(int i = 0; i < result.length; i++) {
            result[i] = solution.getValues()[i] / reward.getRewardForObjective(i);
        }

        return new BenchmarkReward(result);
    }

    @Override
    public void agent_end(final Reward reward) {
        mR_u = handleReward(reward);

        //This code must be used in a infinite horizon setting
        //        if(mRandomWalk == RandomWalkPhase.STARTED ) {
        //            //Tree building step 2, save the resulting state
        //            //We do not get an observation for the end state so we create a fake end state to fit our data structure
        //            Observation fakeEndstateObservation = new Observation();
        //            fakeEndstateObservation.doubleArray = new double[2];
        //            fakeEndstateObservation.doubleArray[0] = fakeEndstateObservation.doubleArray[1] = -1;
        //
        //            mSearchTree.completeTreeBuilding(generateState(fakeEndstateObservation, mInventory));
        //        }

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

            System.out.println( mParetoFront +" -> ");
            mParetoFront.pruneDominatedSolutions();
            System.out.println(mParetoFront);
            mHypervolumeIndicator = Judge.hypervolume(mParetoFront);
            System.out.println(" Hypervolume: " +mHypervolumeIndicator);
        }

        mRandomWalk = RandomWalkPhase.OUT;
        mR_u = null;
    }

    @Override
    public String agent_message(final String message) {
        switch (message) {
            case "isConverged":
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
     * Resets the inventory to empty
     */
    private void resetInventory() {
        for(int i = 0; i < mInventory.length; ++i) {
            mInventory[i] = false;
        }
    }

}
