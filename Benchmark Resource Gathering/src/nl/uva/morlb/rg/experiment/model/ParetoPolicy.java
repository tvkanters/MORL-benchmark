package nl.uva.morlb.rg.experiment.model;

/**
 * A policy within a Pareto front. Represented by its coordinates. I.e., the value for each objective.
 */
public class ParetoPolicy {

    /** The coordinates of the policy in the Pareto front */
    private final double[] mValues;

    /**
     * Creates a new policy at the given coordinates in the Pareto front.
     *
     * @param values
     *            The coordinates
     */
    public ParetoPolicy(final double... values) {
        mValues = values;
    }

    /**
     * @return The coordinates of the policy in the Pareto front as a clone so that modifications don't alter the policy
     */
    public double[] getValues() {
        return mValues.clone();
    }

    /**
     * @return The amount of objectives the policy has a value for
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

}
