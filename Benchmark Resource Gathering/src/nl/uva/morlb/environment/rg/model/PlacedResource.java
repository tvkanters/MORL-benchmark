package nl.uva.morlb.environment.rg.model;

/**
 * A resource placed in a resource gathering problem.
 */
public class PlacedResource {

    /** Which resource is placed */
    private final Resource mResource;
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
    public PlacedResource(final int type, final double x, final double y) {
        mResource = new Resource(type);
        mLocation = new Location(x, y);
    }

    /**
     * @return The type of resource
     */
    public int getType() {
        return mResource.getType();
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
