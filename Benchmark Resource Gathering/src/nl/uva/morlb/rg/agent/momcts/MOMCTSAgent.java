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
import org.rlcommunity.rlglue.codec.util.AgentLoader;

/**
 * Multi-Objective Monte-Carlo Tree Search
 */
public class MOMCTSAgent implements AgentInterface {

    /** The initial reward values **/
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
     * Tree walk values
     */

    /** The action history of the current tree walk **/
    private final List<DiscreteAction> mActionHistory = new LinkedList<DiscreteAction>();

    /** The state history of the current tree walk **/
    private final List<TreeNode> mStateHistory = new LinkedList<TreeNode>();

    /** The accumulated reward over the whole episode **/
    private BenchmarkReward mR_u;

    /** The pareto front **/
    private SolutionSet mParetoFront;

    /** The current hypervolume indicator **/
    private double mHypervolumeIndicator = Double.NEGATIVE_INFINITY;

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

        for (int action = mTaskSpec.getDiscreteActionRange(0).getMin(); action <= mTaskSpec.getDiscreteActionRange(0)
                .getMax(); action++) {
            mAvailableActions.add(DiscreteAction.values()[action]);
        }

        mInventory = new boolean[mTaskSpec.getNumOfObjectives() - 1];

        sInitialReward = new double[mTaskSpec.getNumOfObjectives()];
        for (int i = 0; i < sInitialReward.length; ++i) {
            if (i == 0) sInitialReward[i] = -6;

            sInitialReward[i] = 1;
        }
    }

    @Override
    public Action agent_start(final Observation observation) {
        mRandomWalk = RandomWalkPhase.OUT;
        resetInventory();
        final State currentState = generateState(observation, mInventory);

        if (!mSearchTree.isInitialised()) {
            mSearchTree.initialise(currentState);
        } else {
            mSearchTree.reset();
        }

        // start a new r_u
        mR_u = new BenchmarkReward(new double[mTaskSpec.getNumOfObjectives()]);

        mStateHistory.add(mSearchTree.getCurrentNode());
        final DiscreteAction actionToTake = treeWalk(currentState);
        mActionHistory.add(actionToTake);

        return actionToTake.convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        mR_u = handleReward(reward);

        final State currentState = generateState(observation, mInventory);
        final TreeNode currentNode = mSearchTree.getCurrentNode();

        final DiscreteAction actionToTake = treeWalk(currentState);

        if (mRandomWalk != RandomWalkPhase.IN) {
            mActionHistory.add(actionToTake);
            mStateHistory.add(currentNode);
        }

        return actionToTake.convertToRLGlueAction();
    }

    private DiscreteAction treeWalk(final State currentState) {

        // Add Progressive Widening condition here (Sec. 2.2)
        if (mRandomWalk == RandomWalkPhase.IN || mRandomWalk == RandomWalkPhase.STARTED) {
            if (mRandomWalk == RandomWalkPhase.STARTED) {
                // Tree building step 2, save the resulting state
                mSearchTree.completeTreeBuilding(currentState);
                mRandomWalk = RandomWalkPhase.IN;
            }

            return randomWalk();
        } else if (mSearchTree.getPerformedActionsForCurrentNode().size() == mAvailableActions.size()
                || !mSearchTree.isLeafNode() && !progressiveWidening()) {

            final List<DiscreteAction> availableActions = mSearchTree.getPerformedActionsForCurrentNode();
            DiscreteAction choosenAction = null;
            double bestLookingActionValue = Double.NEGATIVE_INFINITY;

            if (availableActions.size() > 1) {

                for (final DiscreteAction consideredAction : availableActions) {
                    final BenchmarkReward actionReward = mSearchTree.getCurrentNode().getRewardForAction(
                            consideredAction);

                    if (mParetoFront.isDominated(new Solution(actionReward.getRewardVector()))) {
                        final double actionValue = mHypervolumeIndicator
                                - calculateParetoCubeProjection(actionReward).sub(actionReward).getLength();
                        if (actionValue > bestLookingActionValue) {
                            bestLookingActionValue = actionValue;
                            choosenAction = consideredAction;
                        }
                    } else {
                        if (mHypervolumeIndicator > bestLookingActionValue) {
                            bestLookingActionValue = mHypervolumeIndicator;
                            choosenAction = consideredAction;
                        }
                    }
                }

            } else {
                choosenAction = availableActions.get(0);
            }

            mSearchTree.performActionOnCurrentNode(choosenAction);

            return choosenAction;
        } else {
            final List<DiscreteAction> nonAvailableActions = mSearchTree.getPerformedActionsForCurrentNode();
            final List<DiscreteAction> availableActions = new ArrayList<DiscreteAction>(mAvailableActions);
            availableActions.removeAll(nonAvailableActions);

            final DiscreteAction choosenAction = availableActions.get(Util.RNG.nextInt(availableActions.size()));
            // Tree building step 1, save the action
            mSearchTree.saveTreeBuildingAction(choosenAction);
            mRandomWalk = RandomWalkPhase.STARTED;

            return choosenAction;
        }
    }

    /**
     * Get the next random walk action
     *
     * @return The next action determined by random walk
     */
    private DiscreteAction randomWalk() {
        return mAvailableActions.get(Util.RNG.nextInt(mAvailableActions.size()));
    }

    /**
     * Calculates if the progressive widening condition is met
     *
     * @return True if we should do progressive widening, false if not
     */
    private boolean progressiveWidening() {
        final int v_s = mSearchTree.getCurrentNode().getVisitationCount();
        return (int) Math.pow(v_s + 1, 0.5) != (int) Math.pow(v_s, 0.5);
    }

    /**
     * Calculates the projection on the pareto front
     *
     * @param reward
     *            The reward to project
     * @return The projected point
     */
    private BenchmarkReward calculateParetoCubeProjection(final BenchmarkReward reward) {
        double maxGradient = Double.NEGATIVE_INFINITY;
        final BenchmarkReward referencePoint = new BenchmarkReward(Judge.standardReferencepoint(
                mTaskSpec.getNumOfObjectives(), Integer.MAX_VALUE - 1));

        for (final Solution solution : mParetoFront.getSolutions()) {
            final BenchmarkReward solutionTemp = new BenchmarkReward(solution.getValues());
            final BenchmarkReward gradients = pointwiseDivision(solutionTemp.sub(referencePoint),
                    reward.sub(referencePoint));
            final double gradient = gradients.getMinimumRewardEntry();

            if (maxGradient < gradient) {
                maxGradient = gradient;
            }
        }

        return reward.sub(referencePoint).mult(maxGradient).add(referencePoint);
    }

    /**
     * Calculates the pointwise division of a reward with a solution
     *
     * @param solution
     *            The solution set
     * @param reward
     *            The reward
     * @return A pointwise division from the reward with the solution
     */
    private BenchmarkReward pointwiseDivision(final BenchmarkReward solution, final BenchmarkReward reward) {
        final double[] result = new double[reward.getDimension()];
        for (int i = 0; i < result.length; i++) {
            result[i] = solution.getRewardForObjective(i) / reward.getRewardForObjective(i);
        }

        return new BenchmarkReward(result);
    }

    @Override
    public void agent_end(final Reward reward) {
        mR_u = handleReward(reward);

        // Update r*head*_s,a
        for (int historyPosition = 0; historyPosition < mStateHistory.size(); ++historyPosition) {
            final TreeNode toEvaluateNode = mStateHistory.get(historyPosition);

            final DiscreteAction takenAction = mActionHistory.get(historyPosition);

            final BenchmarkReward oldReward = toEvaluateNode.getRewardForAction(takenAction);
            final int actionCounter = toEvaluateNode.getNumOfTimesActionWasTaken(takenAction);

            final BenchmarkReward newReward = oldReward.mult(actionCounter).add(mR_u).mult(1.0d / (actionCounter + 1));
            toEvaluateNode.setRewardForAction(takenAction, newReward);

            toEvaluateNode.increaseActionCounterFor(takenAction);
            toEvaluateNode.increaseVisitationCount();
        }

        mStateHistory.clear();
        mActionHistory.clear();

        // Build pareto front
        final Solution currentSolution = mR_u.toSolution();
        if (!mParetoFront.isDominated(currentSolution) && mParetoFront.addSolution(currentSolution)) {
            mParetoFront.pruneDominatedSolutions();
            mHypervolumeIndicator = Judge.hypervolume(mParetoFront, Integer.MAX_VALUE - 1);
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

        mAvailableActions.clear();
        mSearchTree.clear();
    }

    /**
     * Calculate the appropriate reward and the current inventory based on the last observed reward
     *
     * @param reward
     *            The last observed reward
     * @return The last observed reward
     */
    private BenchmarkReward handleReward(final Reward reward) {
        // Calculate the current inventory
        for (int i = 1; i < reward.doubleArray.length; ++i) {
            if (reward.doubleArray[i] != 0) {
                mInventory[i - 1] = true;
            }
        }

        return mR_u.add(new BenchmarkReward(reward.doubleArray));
    }

    /**
     * Generate the current state from the observation and the current inventory
     *
     * @param observation
     *            The current observation
     * @return The current state
     */
    public State generateState(final Observation observation, final boolean[] inventory) {
        final double[] observationArray = observation.doubleArray;

        final Location currentLocation = new Location(observationArray[0], observationArray[1]);
        return new State(currentLocation, Arrays.copyOf(inventory, inventory.length));
    }

    /**
     * Resets the inventory to empty
     */
    private void resetInventory() {
        for (int i = 0; i < mInventory.length; ++i) {
            mInventory[i] = false;
        }
    }

    public static void main(final String[] args) {
        // Start the agent
        new Thread(new Runnable() {
            @Override
            public void run() {
                new AgentLoader(new MOMCTSAgent()).run();
            }
        }).start();
    }

}
