package nl.uva.morlb.rg.environment.model;

import org.rlcommunity.rlglue.codec.types.Action;

/**
 * Denotes the different possible actions an agent can perform.
 */
public enum DiscreteAction {

    WAIT(0, 0),
    UP(0, 1),
    RIGHT(1, 0),
    DOWN(0, -1),
    LEFT(-1, 0),
    UPRIGHT(1, 1),
    DOWNRIGHT(1, -1),
    DOWNLEFT(-1, -1),
    UPLEFT(-1, 1);

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

    /**
     * Converts this action into the appropriate rl-glue action
     * 
     * @return The rl-glue action
     */
    public Action convertToRLGlueAction() {
        Action action = new Action(1, 0);
        action.intArray[0] = this.ordinal();

        return action;
    }

}
