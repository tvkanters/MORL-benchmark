package nl.uva.morlb.rg.environment.model;

import java.security.InvalidParameterException;

/**
 * A resource placed in a resource gathering problem.
 */
public class Resource {

    /** The type of resource */
    private final int mType;
    /** The reward for collecting this resource */
    private final RewardRange mReward;
    /** The location of the resource */
    private final Location mLocation;

    /**
     * Creates a resource to place in a problem.
     *
     * @param type
     *            The type of resource
     * @param x
     *            The location's x-coordinate
     * @param y
     *            The location's y-coordinate
     */
    public Resource(final int type, final double x, final double y) {
        this(type, x, y, 1, 1);
    }

    /**
     * Creates a resource to place in a problem.
     *
     * @param type
     *            The type of resource
     * @param x
     *            The location's x-coordinate
     * @param y
     *            The location's y-coordinate
     * @param minReward
     *            The minimum possible reward for collecting this resource
     * @param maxReward
     *            The maximum possible reward for collecting this resource
     */
    public Resource(final int type, final double x, final double y, final double minReward, final double maxReward) {
        if (type < 0) {
            throw new InvalidParameterException("Resource types must be 0 or higher");
        }

        mType = type;
        mReward = new RewardRange(minReward, maxReward);
        mLocation = new Location(x, y);
    }

    /**
     * @return The reward for collecting this resource
     */
    public RewardRange getReward() {
        return mReward;
    }

    /**
     * @return The type of resource
     */
    public int getType() {
        return mType;
    }

    /**
     * @return The location of the resource
     */
    public Location getLocation() {
        return mLocation;
    }

}
