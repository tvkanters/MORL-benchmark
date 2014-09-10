package nl.uva.morlb.rg.environment;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;
import nl.uva.morlb.util.Util;

/**
 * The main resource gathering problem. Controls the states, transitions and rewards based on a set of parameters.
 */
public class ResourceGathering {

    /** The parameters affecting the problem */
    private final Parameters mParameters;

    /** The agent's location */
    private Location mAgent;
    /** The goal's location that when reached by the agent indicates a terminal state */
    private final Location mGoal;
    /** The resources that can be collected by the agent */
    private final List<Resource> mResources;

    /**
     * Creates a new resource gathering problem based on the given parameters.
     * 
     * @param parameters
     *            The parameters that define the shape of the problem
     */
    public ResourceGathering(final Parameters parameters) {
        mParameters = parameters;

        mGoal = new Location(mParameters.maxX, mParameters.maxY);
        mResources = mParameters.resources;
    }

    /**
     * Resets the problem to the initial state. Should be called before running each episode.
     */
    public void reset() {
        mAgent = new Location(0, 0);
        for (final Resource resource : mResources) {
            resource.setPickedUp(false);
        }
    }

    /**
     * Lets the agent perform a certain action to transition to the next state and collect a reward. The action must
     * follow the parameter's action space size restrictions.
     * 
     * @param action
     *            The action to perform
     * 
     * @return The reward gathered by performing the action
     */
    public double[] performAction(final DiscreteAction action) {
        if (!mParameters.actionsExpanded && action.ordinal() > 3) {
            throw new InvalidParameterException("Action value cannot exceed 3 with a non-expanded action space");
        }

        // Get the agent's new location
        final Location newAgent = mAgent.sum(action.getLocation());

        // Check if an action failure occurs
        if (Util.RNG.nextDouble() < mParameters.actionFailProb) {
            // Determine which action from 1 to 8 is the failure that altered the next state outcome
            final int failureIndex = Util.RNG.nextInt(mParameters.actionsExpanded ? 7 : 3) + 1;
            final DiscreteAction failure = DiscreteAction.values()[failureIndex];
            newAgent.sum(failure.getLocation());
        }

        // Set the agent's new location bound within the problem size
        mAgent = newAgent.bound(0, mParameters.maxX, 0, mParameters.maxY);

        // Collect resources and calculate reward
        final double[] reward = new double[mParameters.numResourceTypes + 1];
        reward[0] = -1;
        for (final Resource resource : mResources) {
            if (mAgent.equals(resource.getLocation())) {
                reward[resource.getType() + 1] += resource.calculateReward();

                if (mParameters.finiteHorizon) {
                    resource.setPickedUp(true);
                }
            }
        }

        return reward;
    }

    /**
     * Checks if the agent is at the goal, indicating a terminal state.
     * 
     * @return True iff the current state is terminal
     */
    public boolean isTerminal() {
        return mAgent.equals(mGoal);
    }

    /**
     * @return The location of the agent
     */
    public Location getAgent() {
        return mAgent;
    }

    /**
     * @return The location of the goal
     */
    public Location getGoal() {
        return mGoal;
    }

    /**
     * @return The list of resources in the game
     */
    public List<Resource> getResources() {
        return Collections.unmodifiableList(mResources);
    }

}
