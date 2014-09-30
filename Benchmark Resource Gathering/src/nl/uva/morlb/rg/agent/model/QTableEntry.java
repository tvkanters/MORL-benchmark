package nl.uva.morlb.rg.agent.model;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.State;

public class QTableEntry {

    public final State state;
    public final DiscreteAction action;

    public QTableEntry(final State state, final DiscreteAction action) {
        this.state = state;
        this.action = action;
    }

    /**
     * Checks if this Q-table entry has the same contents as the given one.
     *
     * @param other
     *            The Q-table entry to compare
     *
     * @return True iff the contents are the same
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof QTableEntry)) {
            return false;
        }

        final QTableEntry otherEntry = (QTableEntry) other;
        return otherEntry.state.equals(state) && otherEntry.action.equals(action);
    }

    /**
     * Hashes the Q-table entry based on the contents.
     *
     * @return The hash code for the q-table entry
     */
    @Override
    public int hashCode() {
        return state.hashCode() * 10 + action.ordinal();
    }

}
