package nl.uva.morlb.environment.rg;

import java.util.Collections;
import java.util.List;

import nl.uva.morlb.environment.rg.model.DiscreteAction;
import nl.uva.morlb.environment.rg.model.Location;
import nl.uva.morlb.environment.rg.model.Parameters;
import nl.uva.morlb.environment.rg.model.PlacedResource;

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
    private final List<PlacedResource> mResources;

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

        reset();
    }

    /**
     * Resets the problem to the initial state.
     */
    public void reset() {
        mAgent = new Location(0, 0);
        for (final PlacedResource resource : mResources) {
            resource.setPickedUp(false);
        }
    }

    /**
     * Lets the agent perform a certain action to transition to the next state and collect a reward.
     *
     * @param action
     *            The action to perform
     *
     * @return The reward gathered by performing the action
     */
    public double[] performAction(final DiscreteAction action) {
        // Get the agent's new location
        double x = mAgent.getX() + action.getLocation().getX();
        double y = mAgent.getY() + action.getLocation().getY();

        // Keep the agent within bounds
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        x = Math.min(x, mParameters.maxX);
        y = Math.min(x, mParameters.maxY);

        // Set the agent's new location
        mAgent = new Location(x, y);

        // Collect resources and calculate reward
        final double[] reward = new double[mParameters.numResourceTypes + 1];
        reward[0] = -1;
        for (final PlacedResource resource : mResources) {
            if (mAgent.equals(resource.getLocation())) {
                ++reward[resource.getType() + 1];
                resource.setPickedUp(true);
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
    public List<PlacedResource> getResources() {
        return Collections.unmodifiableList(mResources);
    }

}
