package nl.uva.morlb.rg.environment.model;

import java.security.InvalidParameterException;

import nl.uva.morlb.util.Util;

/**
 * A resource placed in a resource gathering problem.
 */
public class Resource {

    /** The type of resource */
    private final int mType;
    /** The minimum possible reward for collecting this resource */
    private final double mMinReward;
    /** The maximum possible reward for collecting this resource */
    private final double mMaxReward;

    /** The location of the resource */
    private final Location mLocation;
    /** Whether or not the resource has been picked up this episode */
    private boolean mPickedUp = false;

    /**
     * Creates a resource to place in a problem
     * 
     * @param type
     *            The type of resource
     * @param x
     *            The location's x-coordinate
     * @param y
     *            The location's y-coordinate
     */
    public Resource(final int type, final double x, final double y) {
        this(type, x, type, 1, 1);
    }

    /**
     * Creates a resource to place in a problem
     * 
     * @param type
     *            The type of resource
     * @param x
     *            The location's x-coordinate
     * @param y
     *            The location's y-coordinate
     */
    public Resource(final int type, final double x, final double y, final double minReward, final double maxReward) {
        if (type < 0) {
            throw new InvalidParameterException("Resource types must be 0 or higher");
        }
        mType = type;

        if (minReward > maxReward) {
            throw new InvalidParameterException("Minimum reward may now exceed maximum reward");
        }
        mMinReward = minReward;
        mMaxReward = maxReward;

        mLocation = new Location(x, y);
    }

    /**
     * @return The type of resource
     */
    public int getType() {
        return mType;
    }

    /**
     * Calculates the reward that should be given when the resource is picked up. When the minimal reward is different
     * from the maximum reward, a uniformly chosen random reward will be returned.
     * 
     * @return A reward for picking up this resource
     */
    public double calculateReward() {
        return Util.RNG.nextDouble() * (mMaxReward - mMinReward) + mMinReward;
    }

    /**
     * @return The location of the resource
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Sets whether or not a resource has been picked up.
     * 
     * @param pickedUp
     *            True iff the resource is picked up
     */
    public void setPickedUp(final boolean pickedUp) {
        mPickedUp = pickedUp;
    }

    /**
     * @return True iff the resource has been picked up
     */
    public boolean isPickedUp() {
        return mPickedUp;
    }

}
