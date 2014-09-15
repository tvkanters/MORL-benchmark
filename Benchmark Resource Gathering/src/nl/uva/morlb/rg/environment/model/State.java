package nl.uva.morlb.rg.environment.model;

import java.util.Arrays;

/**
 * A state within the resource gathering problem. Cannot be modified after instantiation to prevent illegal state
 * changes.
 */
public class State {

    /** The initial hash value; must be prime */
    private static final int HASH_SEED = 7;
    /** The hash offset for following numbers; must be prime */
    private static final int HASH_OFFSET = 31;

    /** The agent's location */
    private final Location mAgent;
    /** A value for each resource indicating whether it has been picked up */
    private final boolean[] mPickedUp;

    /**
     * Creates a new state with the given variables.
     * 
     * @param agent
     *            The agent's location
     * @param numResources
     *            The amount of resources in the problem
     */
    public State(final Location agent, final int numResources) {
        this(agent, new boolean[numResources]);
    }

    /**
     * Creates a new state with the given variables.
     * 
     * @param agent
     *            The agent's location
     * @param pickedUp
     *            A value for each resource indicating whether it has been picked up
     */
    public State(final Location agent, final boolean[] pickedUp) {
        mAgent = agent;
        mPickedUp = pickedUp;
    }

    /**
     * @return The location of the agent
     */
    public Location getAgent() {
        return mAgent;
    }

    /**
     * Checks if the resource at a given index has been picked up.
     * 
     * @param index
     *            The resource index
     * 
     * @return True iff the resource has been picked up
     */
    public boolean isPickedUp(final int index) {
        return mPickedUp[index];
    }

    /**
     * @return A clone of the array indicating which resources have been picked up.
     */
    public boolean[] getPickedUp() {
        return mPickedUp.clone();
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
        if (!(other instanceof State)) {
            return false;
        }

        final State state = (State) other;
        return mAgent.equals(state.mAgent) && Arrays.equals(mPickedUp, state.mPickedUp);
    }

    /**
     * Hashes the state based on the contents.
     * 
     * @return The hash code for the state
     */
    @Override
    public int hashCode() {
        int intHash = HASH_SEED;
        intHash = intHash * HASH_OFFSET + mAgent.hashCode();
        intHash = intHash * HASH_OFFSET + Arrays.hashCode(mPickedUp);
        return intHash;
    }

    /**
     * @return The state in (xAgent,yAgent,pickedUp1,pickedUp2,...,pickedUpN) format
     */
    @Override
    public String toString() {
        String str = "(" + mAgent.x + "," + mAgent.y;
        for (final boolean pickedUp : mPickedUp) {
            str += "," + (pickedUp ? "1" : "0");
        }
        str += ")";

        return str;
    }

    /**
     * Creates a state from a string representation of a state.
     * 
     * @param str
     *            The string representation of a state
     * 
     * @return The state
     */
    public static State fromString(final String str) {
        int prevComma = 0;
        int nextComma;

        nextComma = str.indexOf(',');
        final double x = Double.parseDouble(str.substring(prevComma + 1, nextComma));

        prevComma = nextComma;
        nextComma = str.indexOf(',', nextComma + 1);
        final double y = Double.parseDouble(str.substring(prevComma + 1, nextComma));

        final String[] pickedUpStr = str.substring(nextComma + 1, str.length() - 1).split(",");
        final boolean[] pickedUp = new boolean[pickedUpStr.length];
        for (int i = 0; i < pickedUp.length; ++i) {
            pickedUp[i] = pickedUpStr[i].equals("1");
        }

        return new State(new Location(x, y), pickedUp);
    }
}
