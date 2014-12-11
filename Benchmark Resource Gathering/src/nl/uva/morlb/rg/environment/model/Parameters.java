package nl.uva.morlb.rg.environment.model;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import nl.uva.morlb.util.Util;

/**
 * The parameters affecting the resource gathering problem.
 */
public class Parameters {

    /** The value indicating a fully observable state */
    public static final double FULLY_OBSERVABLE = 1;
    /** The value indicating only 3 actions */
    public static final int ACTIONS_TINY = 0;
    /** The value indicating only 5 actions */
    public static final int ACTIONS_SMALL = 1;
    /** The value indicating 9 actions */
    public static final int ACTIONS_FULL = 2;
    /** The value indicating a discrete state and action space */
    public static final boolean STATES_DISCRETE = false;
    /** The value indicating a continuous action and action space */
    public static final boolean STATES_CONTINUOUS = true;
    /** The value indicating a continuous action and action space */
    public static final int MAX_PICKED_UP_UNLIMITED = Integer.MAX_VALUE;

    /** The highest possible x value of a location */
    public final double maxX;
    /** The highest possible y value of a location */
    public final double maxY;

    /** The resources to place in the problem */
    public final List<Resource> resources;
    /** The amount of resources */
    public final int numResources;
    /** The amount of different types of resources available, related to the amount of objectives */
    public final int numResourceTypes;

    /** Which action space to use */
    public final int actionSpace;
    /** Which action space to use */
    public final int actionMax;
    /** The probability that an additional random action will be performed when taking an action */
    public final double actionFailProb;

    /** Whether or not the problem has a finite horizon rather than an infinite horizon */
    public final boolean finiteHorizon;
    /** The discount factor applied to rewards */
    public final double discountFactor;

    /** Whether or not the state is fully observable */
    public final boolean fullyObservable;
    /** The maximum Manhattan distance that an agent can see in a partial observable setting */
    public final double viewDistance;
    /** The chance that an observation is successful, {@link #FULLY_OBSERVABLE} indicates full observability */
    public final double observationSuccess;

    /** Whether or not the state and actions are continuous */
    public final boolean continuousStatesActions;
    /** The maximum distance an agent can move in a direction */
    public final double maxStepSize = 1;

    /** The amount of resources an agent can pick up */
    public final int maxPickedUp;

    /**
     * Creates a new parameter set for a discrete problem.
     *
     * @param maxX
     *            The highest possible x value of a location
     * @param maxY
     *            The highest possible y value of a location
     * @param resources
     *            The resources to place in the problem
     * @param actionsExpanded
     *            Which action space to use
     * @param discountFactor
     *            The discount factor applied to rewards, indicates a finite horizon problem when the value is 1
     * @param actionFailProb
     *            The probability that an additional random action will be performed when taking an action
     * @param observationSuccess
     *            The chance that an observation is successful, {@link #FULLY_OBSERVABLE} indicates full observability
     *            but with partial observability, view distance is limited to 1
     * @param continuousStatesActions
     *            Whether or not the state and actions are continuous
     * @param maxPickedUp
     *            The amount of resources an agent can pick up
     */
    public Parameters(final double maxX, final double maxY, final List<Resource> resources, final int actionSpace,
            final double discountFactor, final double actionFailProb, final double observationSuccess,
            final boolean continuousStatesActions, final int maxPickedUp) {
        // Define the state space size
        this.maxX = maxX;
        this.maxY = maxY;

        // Define the resources and some cached values
        this.resources = resources;
        numResources = resources.size();
        int maxType = 0;
        for (final Resource resource : resources) {
            maxType = Math.max(maxType, resource.getType());
        }
        numResourceTypes = maxType + 1;

        // Define the action space size
        this.actionSpace = actionSpace;
        switch (actionSpace) {
            case Parameters.ACTIONS_TINY:
                actionMax = 2;
                break;
            case Parameters.ACTIONS_SMALL:
                actionMax = 4;
                break;
            case Parameters.ACTIONS_FULL:
                actionMax = 8;
                break;
            default:
                throw new InvalidParameterException("Invalid action space");
        }

        // Define the discount factor
        if (discountFactor <= 0 || discountFactor > 1) {
            throw new InvalidParameterException("Discount factor must be in range ]0,1]");
        }
        finiteHorizon = discountFactor == 1;
        this.discountFactor = discountFactor;

        // Define the probability or an action failing
        if (actionFailProb < 0 || actionFailProb > 1) {
            throw new InvalidParameterException("Action failure probability must be in range [0,1]");

        }
        this.actionFailProb = actionFailProb;

        // Define the observability of the problem
        this.observationSuccess = observationSuccess;
        fullyObservable = (observationSuccess == FULLY_OBSERVABLE);
        viewDistance = (fullyObservable ? Double.POSITIVE_INFINITY : 1);

        // Define the continuity of the states and actions
        this.continuousStatesActions = continuousStatesActions;

        // Define the amount of resources an agent can pick up
        this.maxPickedUp = maxPickedUp;
    }

    /**
     * Shuffles the locations of the resources to random locations.
     *
     * @param rng
     *            The random number generator to determine the new positions with
     */
    public void shuffleResources(final Random rng) {
        for (int i = 0; i < resources.size(); ++i) {
            final Resource resource = resources.get(i);

            final double x = (continuousStatesActions ? rng.nextDouble() * maxX : rng.nextInt((int) maxX + 1));
            final double y = (continuousStatesActions ? rng.nextDouble() * maxY : rng.nextInt((int) maxY + 1));
            final RewardRange reward = resource.getReward();

            resources.remove(i);
            resources.add(i, new Resource(resource.getType(), x, y, reward.min, reward.max));
        }
    }

    /**
     * @return The parameters in the format: maxX maxY actionSpace discountFactor actionFailProb viewDistance
     *         continuousStatesActions maxPickedUp resource...
     */
    @Override
    public String toString() {
        String str = maxX + " " + maxY + " " + actionSpace + " " + discountFactor + " " + actionFailProb + " "
                + observationSuccess + " " + continuousStatesActions + " " + maxPickedUp;
        for (final Resource resource : resources) {
            str += " " + resource;
        }
        return str;
    }

    /**
     * Converts a string representation of parameters to an object.
     *
     * @param str
     *            The string in the toString format
     * @param rng
     *            The random number generator to determine the new positions with
     *
     * @return The parameters
     */
    public static Parameters fromString(final String str, final Random rng) {
        return fromString(str.split(" "), rng);
    }

    /**
     * Converts a string representation of parameters to an object.
     *
     * @param values
     *            The string in the toString format split on spaces
     * @param rng
     *            The random number generator to determine the new positions with
     *
     * @return The parameters
     */
    public static Parameters fromString(final String[] values, final Random rng) {
        final double maxX = Double.parseDouble(values[0]);
        final double maxY = Double.parseDouble(values[1]);
        final boolean continuousStatesActions = Boolean.parseBoolean(values[6]);

        final List<Resource> resources = new LinkedList<>();
        if (values.length == 10) {
            final int numObjectives = Integer.parseInt(values[8]);
            final int numResources = Integer.parseInt(values[9]);
            for (int i = 0; i < numResources; ++i) {
                final int type = (i < numObjectives ? i : Util.RNG.nextInt(numObjectives));
                final double x = (continuousStatesActions ? rng.nextDouble() * maxX : rng.nextInt((int) maxX + 1));
                final double y = (continuousStatesActions ? rng.nextDouble() * maxY : rng.nextInt((int) maxY + 1));
                resources.add(new Resource(type, x, y));
            }
        } else {
            for (int i = 8; i < values.length; i += 5) {
                resources.add(Resource.fromString(values[i] + " " + values[i + 1] + " " + values[i + 2] + " "
                        + values[i + 3] + " " + values[i + 4]));
            }
        }

        return new Parameters(maxX, maxY, resources, Integer.parseInt(values[2]), Double.parseDouble(values[3]),
                Double.parseDouble(values[4]), Double.parseDouble(values[5]), continuousStatesActions,
                Integer.parseInt(values[7]));
    }

    /**
     * Checks if this state has the same contents as the given one.
     *
     * @param other
     *            The state to compare
     *
     * @return True iff the contents are the same
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Parameters)) {
            return false;
        }

        return toString().equals(((Parameters) other).toString());
    }

    /**
     * Hashes the state based on the contents.
     *
     * @return The hash code for the state
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
