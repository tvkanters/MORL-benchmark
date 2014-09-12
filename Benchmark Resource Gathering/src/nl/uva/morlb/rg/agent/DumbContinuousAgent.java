package nl.uva.morlb.rg.agent;

import java.util.Arrays;

import nl.uva.morlb.util.Log;
import nl.uva.morlb.util.Util;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

/**
 * A sanity check agent that just runs to the top-right.
 */
public class DumbContinuousAgent implements AgentInterface {

    /**
     * Called when preparing the problem.
     */
    @Override
    public void agent_init(final String taskSpecStr) {}

    /**
     * @return The action to perform next
     */
    public double[] getAction() {
        return new double[] { Util.RNG.nextDouble(), Util.RNG.nextDouble() };
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
        final Action action = new Action();
        action.doubleArray = getAction();
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

        final Action action = new Action();
        action.doubleArray = getAction();
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
    }

    /**
     * Resets the agent to the initial state.
     */
    @Override
    public void agent_cleanup() {}

    /**
     * Handles Glue messages, not implemented yet
     *
     * @param message
     *            The message to handle
     */
    @Override
    public String agent_message(final String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
