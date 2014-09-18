package nl.uva.morlb.rg.agent.model;

import java.util.Arrays;

import nl.uva.morlb.rg.environment.model.Location;

public class State {

    /** The initial hash value; must be prime */
    private static final int HASH_SEED = 7;
    /** The hash offset for following numbers; must be prime */
    private static final int HASH_OFFSET = 31;

    private final Location mLocation;

    private final boolean[] mPickedUpResources;

    public State(final Location location, final boolean[] pickedUpResources) {
        mLocation = location;
        mPickedUpResources = pickedUpResources;
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
        return mLocation.equals(state.mLocation) && Arrays.equals(mPickedUpResources, state.mPickedUpResources);
    }

    /**
     * Hashes the state based on the contents.
     *
     * @return The hash code for the state
     */
    @Override
    public int hashCode() {
        int intHash = HASH_SEED;
        intHash = intHash * HASH_OFFSET + mLocation.hashCode();
        intHash = intHash * HASH_OFFSET + Arrays.hashCode(mPickedUpResources);
        return intHash;
    }

}
