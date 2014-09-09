package nl.uva.morlb.environment.rg.model;

/**
 * Denotes the different possible actions an agent can perform.
 */
public enum DiscreteAction {

    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    /** The relative location that this direction goes to */
    private final Location mLocation;

    /**
     * Prepares an action with the specified coordinates.
     *
     * @param x
     *            The change in the x coordinate after going in this direction
     * @param y
     *            The change in the y coordinate after going in this direction
     */
    private DiscreteAction(final int x, final int y) {
        mLocation = new Location(x, y);
    }

    /**
     * @return The location change of this action
     */
    public Location getLocation() {
        return mLocation;
    }

}
