package nl.uva.morlb.rg.environment.model;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

/**
 * The parameters affecting the resource gathering problem.
 */
public class Parameters {

    /** The value indicating a fully observable state */
    public static final double FULLY_OBSERVABLE = Double.POSITIVE_INFINITY;

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

    /** Whether or not the action space has an increased size */
    public final boolean actionsExpanded;
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

    /** Whether or not the state and actions are continuous */
    public final boolean continuousStatesActions;
    /** The maximum distance an agent can move in a direction */
    public final double maxStepSize = 1;

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
     *            Whether or not the action space has an increased size
     * @param discountFactor
     *            The discount factor applied to rewards, indicates a finite horizon problem when the value is 1
     * @param actionFailProb
     *            The probability that an additional random action will be performed when taking an action
     * @param viewDistance
     *            The maximum Manhattan distance that an agent can see, {@link #FULLY_OBSERVABLE} indicates full
     *            observability
     */
    public Parameters(final double maxX, final double maxY, final List<Resource> resources,
            final boolean actionsExpanded, final double discountFactor, final double actionFailProb,
            final double viewDistance, final boolean continuousStatesActions) {
        // Define the state space size
        this.maxX = maxX;
        this.maxY = maxY;

        // Define the resources and some cached values
        this.resources = Collections.unmodifiableList(resources);
        numResources = resources.size();
        int maxType = 0;
        for (final Resource resource : resources) {
            maxType = Math.max(maxType, resource.getType());
        }
        numResourceTypes = maxType + 1;

        // Define the action space size
        this.actionsExpanded = actionsExpanded;

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
        fullyObservable = (viewDistance == FULLY_OBSERVABLE);
        this.viewDistance = viewDistance;

        // Define the continuity of the states and actions
        this.continuousStatesActions = continuousStatesActions;
    }

}
