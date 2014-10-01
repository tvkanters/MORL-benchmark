package nl.uva.morlb.rg.agent.convexhull;

import java.security.InvalidParameterException;
import java.util.HashMap;

import nl.uva.morlb.rg.agent.model.QTableEntry;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.State;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;
import nl.uva.morlb.util.Util;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

public class ConvexHullQLearning implements AgentInterface {

    /** The discount factor of Q table updates */
    private static final double DISCOUNT = 1;
    /** The amount of times the solution set has to be the same before converging */
    private static final int REPEAT_CONVERGE_LIMIT = 100;

    /** The Q table to store values for state-action pairs */
    private final HashMap<QTableEntry, SolutionSet> mQTable = new HashMap<>();
    /** The Q value to use when it has not been set yet */
    private SolutionSet mDefaultQValue;
    /** The amount of objectives in the problem */
    private int mNumObjectives;
    /** The minimal action possible */
    private int mMinAction;
    /** The maximum action possible */
    private int mMaxAction;

    /** The Q table entry that should be updated next */
    private QTableEntry mLastEntry;
    /** The amount of times the same solution set has been returned */
    private int mRepeatCount = 0;
    /** The solution set previous returned */
    private String mPrevSolutionSet = "";

    /**
     * Called when preparing the problem.
     */
    @Override
    public void agent_init(final String taskSpec) {
        final TaskSpecVRLGLUE3 tSpec = new TaskSpecVRLGLUE3(taskSpec);

        mNumObjectives = tSpec.getNumOfObjectives();
        mMinAction = tSpec.getDiscreteActionRange(0).getMin();
        mMaxAction = tSpec.getDiscreteActionRange(0).getMax();

        mDefaultQValue = new SolutionSet(mNumObjectives);
        final double[] solutionValues = new double[mNumObjectives];
        solutionValues[0] = -100;
        mDefaultQValue.addSolution(new Solution(solutionValues));
    }

    /**
     * Called when the environment just started and returned the initial observation.
     * 
     * @param observation
     *            The observation as given by the environment
     * 
     * @return The action to perform next
     */
    @Override
    public Action agent_start(final Observation observation) {
        final State state = generateState(observation);
        final DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    /**
     * Called after performing an action.
     * 
     * @param reward
     *            The reward given by performing the previous action
     * @param observation
     *            The observation as given by the environment
     * 
     * @return The action to perform next
     */
    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        final State state = generateState(observation);

        // Union the Q values of the state over the actions
        final SolutionSet union = new SolutionSet(mNumObjectives);
        for (int i = mMinAction; i <= mMaxAction; ++i) {
            final QTableEntry entry = new QTableEntry(state, DiscreteAction.values()[i]);
            union.addSolutionSet(getQValue(entry));
        }

        // Convert the union to a convex coverage set
        final SolutionSet convexCoverageSet = CPrune.prune(union);

        // Discount the convex coverage set and add the reward to create the new Q value
        final SolutionSet newQValue = new SolutionSet(mNumObjectives);
        for (final Solution solution : convexCoverageSet.getSolutions()) {
            final double[] solutionValues = solution.getValues();
            final double[] newSolutionValues = new double[mNumObjectives];

            for (int i = 0; i < mNumObjectives; ++i) {
                newSolutionValues[i] = reward.doubleArray[i] + DISCOUNT * solutionValues[i];
            }

            newQValue.addSolution(new Solution(newSolutionValues));
        }

        // Save the new Q value
        mQTable.put(mLastEntry, newQValue);

        // Perform the next action
        final DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    /**
     * Called when a terminal state has been reached or a time limit is reached.
     * 
     * @param reward
     *            The reward given by performing the previous action
     */
    @Override
    public void agent_end(final Reward reward) {
        // Update the Q value based on the reward gotten
        final SolutionSet newQValue = new SolutionSet(mNumObjectives);
        newQValue.addSolution(new Solution(reward.doubleArray));

        mQTable.put(mLastEntry, newQValue);
    }

    /**
     * Resets the agent to the initial state.
     */
    @Override
    public void agent_cleanup() {
        // Do the actual clean up work
        mQTable.clear();
        mRepeatCount = 0;
        mPrevSolutionSet = "";
    }

    /**
     * Handles Glue messages.
     * 
     * @param message
     *            The message to handle
     */
    @Override
    public String agent_message(final String message) {
        switch (message) {
        case "getSolutionSet":
            return getSolutionSet().toString();

        case "isConverged":
            final String solutionSetString = getSolutionSet().toString();
            if (mPrevSolutionSet.equals(solutionSetString)) {
                ++mRepeatCount;
                if (mRepeatCount == REPEAT_CONVERGE_LIMIT) {
                    return Boolean.TRUE.toString();
                }
            } else {
                mRepeatCount = 0;
                mPrevSolutionSet = solutionSetString;
            }
            return Boolean.FALSE.toString();
        }

        throw new InvalidParameterException("Unknown message: " + message);
    }

    /**
     * Unions the solutions sets of the initial state-actions to return the full Pareto front.
     * 
     * @return The solution set
     */
    private SolutionSet getSolutionSet() {
        final SolutionSet union = new SolutionSet(mNumObjectives);

        // Union the Q values of the state over the actions
        final State initState = new State(new Location(0, 0), new boolean[2]);
        for (int i = mMinAction; i <= mMaxAction; ++i) {
            final QTableEntry entry = new QTableEntry(initState, DiscreteAction.values()[i]);
            union.addSolutionSet(getQValue(entry));
        }

        union.pruneDominatedSolutions();

        return union;
    }

    /**
     * Retrieves the saved Q value or creates a default one
     * 
     * @param key
     *            The state-action pair to search
     * 
     * @return The saved Q value or a solution set with a 0-solution
     */
    private SolutionSet getQValue(final QTableEntry key) {
        if (mQTable.containsKey(key)) {
            return mQTable.get(key);
        } else {
            return mDefaultQValue.copy();
        }
    }

    /**
     * Generates a state based on the given observation.
     * 
     * @param observation
     *            The observation given by the environment
     * 
     * @return The state corresponding to the observation
     */
    public State generateState(final Observation observation) {
        final double[] obsVals = observation.doubleArray;

        final Location currentLocation = new Location(obsVals[0], obsVals[1]);
        final boolean[] pickedUp = new boolean[(obsVals.length - 4) / 3];
        for (int i = 0; i < pickedUp.length; ++i) {
            pickedUp[i] = Double.isNaN(obsVals[i * 3 + 4]);
        }

        return new State(currentLocation, pickedUp);
    }

    /**
     * @return A random action to perform
     */
    public DiscreteAction getRandomAction() {
        return DiscreteAction.values()[Util.RNG.nextInt(mMaxAction - mMinAction + 1) + mMinAction];
    }

}
