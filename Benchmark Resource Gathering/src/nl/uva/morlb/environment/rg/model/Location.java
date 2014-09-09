package nl.uva.morlb.environment.rg.model;

/**
 * A location for an object.
 */
public class Location {

    /** The initial hash value; must be prime */
    private static final int HASH_SEED = 7;

    /** The hash offset for following numbers; must be prime */
    private static final int HASH_OFFSET = 31;

    /** The x coordinate for this location */
    private final double mX;

    /** The y coordinate for this location */
    private final double mY;

    /**
     * Creates a new location for the specified coordinates.
     *
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     */
    public Location(final double x, final double y) {
        mX = x;
        mY = y;
    }

    /**
     * @return The x coordinate
     */
    public double getX() {
        return mX;
    }

    /**
     * @return The y coordinate
     */
    public double getY() {
        return mY;
    }

    /**
     * Checks if this location has the same coordinates as the given one.
     *
     * @param location
     *            The location to compare
     *
     * @return True of the coordinates are the same, false otherwise
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Location)) {
            return false;
        }

        final Location location = (Location) other;
        return mX == location.mX && mY == location.mY;
    }

    /**
     * Hashes the location based on the coordinates.
     *
     * @return The hash code for the location
     */
    @Override
    public int hashCode() {
        int intHash = HASH_SEED;
        intHash += HASH_OFFSET * mX;
        intHash += HASH_OFFSET * mY * (int) Math.pow(2, 16);
        return intHash;
    }

    /**
     * @return The location in (x,y) format.
     */
    @Override
    public String toString() {
        return "(" + mX + "," + mY + ")";
    }
}
