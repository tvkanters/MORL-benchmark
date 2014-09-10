package nl.uva.morlb.rg.environment.model;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

/**
 * The parameters affecting the resource gathering problem.
 */
public class Parameters {

    /** The highest possible x value of a location */
    public final double maxX;
    /** The highest possible y value of a location */
    public final double maxY;

    /** The resources to place in the problem */
    public final List<PlacedResource> resources;
    /** The amount of resources */
    public final int numResources;
    /** The amount of different types of resources available, related to the amount of objectives */
    public final int numResourceTypes;

    /** Whether or not the action space has an increased size */
    public final boolean actionsExpanded;

    /** Whether or not the problem has a finite horizon rather than an infinite horizon */
    public final boolean finiteHorizon;
    /** The discount factor applied to rewards */
    public final double discountFactor;

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
     */
    public Parameters(final double maxX, final double maxY, final List<PlacedResource> resources,
            final boolean actionsExpanded, final double discountFactor) {
        // Define the state space size
        this.maxX = maxX;
        this.maxY = maxY;

        // Sets the resources and some cached values
        this.resources = Collections.unmodifiableList(resources);
        numResources = resources.size();
        int maxType = 0;
        for (final PlacedResource resource : resources) {
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
    }

}
