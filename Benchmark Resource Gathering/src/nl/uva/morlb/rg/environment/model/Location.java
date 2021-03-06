package nl.uva.morlb.rg.environment.model;

/**
 * A location for an object.
 */
public class Location {

    /** The initial hash value; must be prime */
    private static final int HASH_SEED = 7;
    /** The hash offset for following numbers; must be prime */
    private static final int HASH_OFFSET = 31;

    /** The x-coordinate for this location */
    public final double x;
    /** The y-coordinate for this location */
    public final double y;

    /**
     * Sums the coordinates of two locations.
     *
     * @param l1
     *            The left hand location
     * @param l2
     *            The right hand location
     *
     * @return The summed up location
     */
    public static Location sum(final Location l1, final Location l2) {
        return new Location(l1.x + l2.x, l1.y + l2.y);
    }

    /**
     * Calculates the Manhattan distance between two locations.
     *
     * @param l1
     *            The left hand location
     * @param l2
     *            The right hand location
     *
     * @return The Manhattan distance
     */
    public static double distance(final Location l1, final Location l2) {
        return Math.abs(l1.x - l2.x) + Math.abs(l1.y - l2.y);
    }

    /**
     * Creates a new location for the specified coordinates.
     *
     * @param x
     *            The x-coordinate
     * @param y
     *            The y-coordinate
     */
    public Location(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Bounds the coordinates of a location to ensure they're within a certain range.
     *
     * @param minX
     *            The minimum possible x-coordinate value
     * @param maxX
     *            The maximum possible x-coordinate value
     * @param minY
     *            The minimum possible y-coordinate value
     * @param maxY
     *            The maximum possible y-coordinate value
     *
     * @return The new bound location
     */
    public Location bound(final double minX, final double maxX, final double minY, final double maxY) {
        return new Location(Math.max(Math.min(x, maxX), minX), Math.max(Math.min(y, maxY), minY));
    }

    /**
     * Checks if this location has the same coordinates as the given one.
     *
     * @param other
     *            The location to compare
     *
     * @return True iff the coordinates are the same
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Location)) {
            return false;
        }

        final Location location = (Location) other;
        return x == location.x && y == location.y;
    }

    /**
     * Hashes the location based on the coordinates.
     *
     * @return The hash code for the location
     */
    @Override
    public int hashCode() {
        int intHash = HASH_SEED;
        intHash += HASH_OFFSET * x;
        intHash += HASH_OFFSET * y * (int) Math.pow(2, 16);
        return intHash;
    }

    /**
     * @return The location in (x,y) format
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
