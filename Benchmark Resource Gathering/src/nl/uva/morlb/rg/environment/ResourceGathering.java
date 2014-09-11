package nl.uva.morlb.rg.environment;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;
import nl.uva.morlb.rg.environment.model.State;
import nl.uva.morlb.util.Util;

/**
 * The main resource gathering problem. Controls the states, transitions and rewards based on a set of parameters.
 */
public class ResourceGathering {

    /** The parameters affecting the problem */
    private final Parameters mParameters;
    /** The resources that can be collected by the agent */
    private final List<Resource> mResources;
    /** The goal's location that when reached by the agent indicates a terminal state */
    private final Location mGoal;

    /** The state that the problem starts with */
    private final State mInitialState;
    /** The state that the problem is currently in */
    private State mCurrentState;

    /**
     * Creates a new resource gathering problem based on the given parameters.
     *
     * @param parameters
     *            The parameters that define the shape of the problem
     */
    public ResourceGathering(final Parameters parameters) {
        mParameters = parameters;

        mResources = mParameters.resources;
        mGoal = new Location(mParameters.maxX, mParameters.maxY);

        mInitialState = new State(new Location(0, 0), mResources.size());
        reset();
    }

    /**
     * Resets the problem to the initial state. Should be called before running each episode.
     */
    public void reset() {
        mCurrentState = mInitialState;
    }

    /**
     * Lets the agent perform a certain action to transition to the next state and collect a reward. The action must
     * follow the parameter's action space size restrictions.
     *
     * @param action
     *            The action to perform
     *
     * @return The state resulting from performing the action
     */
    public State performAction(final DiscreteAction action) {
        if (!mParameters.actionsExpanded && action.ordinal() > 4) {
            throw new InvalidParameterException("Action value cannot exceed 4 with a non-expanded action space");
        }

        // Get the agent's new location
        Location newAgent = mCurrentState.getAgent().sum(action.getLocation());

        // Check if an action failure occurs
        if (Util.RNG.nextDouble() < mParameters.actionFailProb) {
            // Determine which action from 1 to 8 is the failure that altered the next state outcome
            final int failureIndex = Util.RNG.nextInt(mParameters.actionsExpanded ? 7 : 3) + 1;
            final DiscreteAction failure = DiscreteAction.values()[failureIndex];
            newAgent.sum(failure.getLocation());
        }

        // Set the agent's new location bound within the problem size
        newAgent = newAgent.bound(0, mParameters.maxX, 0, mParameters.maxY);

        // Collect resources and calculate reward
        final double[] reward = new double[mParameters.numResourceTypes + 1];
        reward[0] = -1;
        int i = 0;
        final boolean[] pickedUp = mCurrentState.getPickedUp();
        for (final Resource resource : mResources) {
            if (!pickedUp[i] && newAgent.equals(resource.getLocation())) {
                reward[resource.getType() + 1] += resource.calculateReward();

                if (mParameters.finiteHorizon) {
                    pickedUp[i] = true;
                }
            }
            ++i;
        }

        mCurrentState = new State(newAgent, pickedUp, reward, newAgent.equals(mGoal));

        return mCurrentState;
    }

    /**
     * @return The list of resources in the game
     */
    public List<Resource> getResources() {
        return Collections.unmodifiableList(mResources);
    }

    /**
     * @return The state that the problem is currently in
     */
    public State getCurrentState() {
        return mCurrentState;
    }

    /**
     * @return The location of the goal
     */
    public Location getGoal() {
        return mGoal;
    }

}
