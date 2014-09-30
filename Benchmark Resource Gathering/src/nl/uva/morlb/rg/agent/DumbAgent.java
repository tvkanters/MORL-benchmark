package nl.uva.morlb.rg.agent;

import java.security.InvalidParameterException;
import java.util.Arrays;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;
import nl.uva.morlb.util.Log;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

/**
 * A sanity check agent that just runs to the top-right.
 */
public class DumbAgent implements AgentInterface {

    /** The amount of possible actions according to the task spec */
    private int numActions;
    /** Whether the agent went up last term to enable diagonal walking */
    private boolean mWentUp;
    /** The solution set found in this test */
    private SolutionSet mSolutionSet;
    /** The return this episode */
    private double[] mReturn;

    /**
     * Called when preparing the problem.
     */
    @Override
    public void agent_init(final String taskSpecStr) {
        final TaskSpecVRLGLUE3 taskSpec = new TaskSpecVRLGLUE3(taskSpecStr);
        numActions = taskSpec.getDiscreteActionRange(0).getMax();
        mReturn = new double[taskSpec.getNumOfObjectives()];

        if (mSolutionSet == null) {
            mSolutionSet = new SolutionSet(mReturn.length);
        }
    }

    /**
     * @return The action to perform next
     */
    public int getAction() {
        DiscreteAction action;
        if (numActions == 8) {
            action = DiscreteAction.UPRIGHT;
        } else {
            mWentUp = !mWentUp;
            action = (mWentUp ? DiscreteAction.RIGHT : DiscreteAction.UP);
        }

        return action.ordinal();
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
        mReturn = new double[mReturn.length];
        final Action action = new Action(1, 0);
        action.setInt(0, getAction());
        return action;
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
        Log.d("AGENT: Got a reward of " + Arrays.toString(reward.doubleArray));
        incrementReturn(reward);

        final Action action = new Action(1, 0);
        action.setInt(0, getAction());
        return action;
    }

    /**
     * Called when a terminal state has been reached or a time limit is reached.
     *
     * @param reward
     *            The reward given by performing the previous action
     */
    @Override
    public void agent_end(final Reward reward) {
        Log.d("AGENT: Got a reward of " + Arrays.toString(reward.doubleArray));
        incrementReturn(reward);
        mSolutionSet.addSolution(new Solution(mReturn));
    }

    /**
     * Increments the return with the received reward.
     *
     * @param reward
     *            The reward received
     */
    private void incrementReturn(final Reward reward) {
        for (int i = 0; i < mReturn.length; ++i) {
            mReturn[i] += reward.doubleArray[i];
        }
    }

    /**
     * Resets the agent to the initial state.
     */
    @Override
    public void agent_cleanup() {
        mSolutionSet = null;
    }

    /**
     * Handles Glue messages.
     *
     * @param message
     *            The message to handle
     */
    @Override
    public String agent_message(final String message) {
        if (message.equals("getSolutionSet")) {
            System.out.println(mSolutionSet.toString());
            return mSolutionSet.toString();
        }

        throw new InvalidParameterException("Unknown message: " + message);
    }

    public static void main(final String[] args) {
        // Start the agent
        new Thread(new Runnable() {
            @Override
            public void run() {
                new AgentLoader(new DumbAgent()).run();
            }
        }).start();
    }

}
