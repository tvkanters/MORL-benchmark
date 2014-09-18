package nl.uva.morlb.util;

import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.environment.model.DiscreteAction;

public class QTableEntry {

    private final State mState;
    private final DiscreteAction mAction;

    public QTableEntry(final State state, final DiscreteAction action) {
        mState = state;
        mAction = action;
    }

    public State getState() {
        return mState;
    }

    public DiscreteAction getAction() {
        return mAction;
    }

    /**
     * Checks if this q-table entry has the same contents as the given one.
     *
     * @param other
     *            The q-table entry to compare
     *
     * @return True iff the contents are the same
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof QTableEntry)) {
            return false;
        }

        final QTableEntry otherEntry = (QTableEntry) other;
        return otherEntry.mState.equals(mState) && otherEntry.mAction.equals(mAction);
    }

    /**
     * Hashes the q-table entry based on the contents.
     *
     * @return The hash code for the q-table entry
     */
    @Override
    public int hashCode() {
        return mState.hashCode() * 10 + mAction.ordinal();
    }

}
