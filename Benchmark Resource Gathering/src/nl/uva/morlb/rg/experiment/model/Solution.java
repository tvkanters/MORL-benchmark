package nl.uva.morlb.rg.experiment.model;

import java.util.Arrays;

/**
 * A solution within a solution set. Represented by its coordinates. I.e., the value for each objective.
 */
public class Solution {

    /** The coordinates of the solution in the solution set */
    private final double[] mValues;

    /**
     * Creates a new solution at the given coordinates in the solution set.
     *
     * @param values
     *            The coordinates
     */
    public Solution(final double... values) {
        mValues = values;
    }

    /**
     * @return The coordinates of the solution in the solution set as a clone so that modifications don't alter the
     *         solution
     */
    public double[] getValues() {
        return mValues.clone();
    }

    /**
     * @return The amount of objectives the solution has a value for
     */
    public int getNumObjectives() {
        return mValues.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String str = "(";
        for (int i = 0; i < mValues.length; ++i) {
            str += (i != 0 ? "," : "") + mValues[i];
        }
        str += ")";
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Solution)) return false;
        return Arrays.equals(mValues, ((Solution) obj).mValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mValues.hashCode();
    }

}
