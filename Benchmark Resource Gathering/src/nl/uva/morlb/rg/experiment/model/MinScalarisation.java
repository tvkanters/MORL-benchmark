package nl.uva.morlb.rg.experiment.model;

/**
 * A product scalarisation function that simply multiplies everything.
 */
public class MinScalarisation extends Scalarisation {

    /** The amount of values this function accepts */
    private final int mNumValues;

    /**
     * Creates a new scalarisation function for the given amount of values.
     * 
     * @param numValues
     *            The amount of values that will be scalarised
     */
    public MinScalarisation(final int numValues) {
        mNumValues = numValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double scalarise(final double[] values, final double[] weights) {
        double sum = 0;
        double min = Double.MAX_VALUE;
        for (int i = 1; i < values.length; ++i) {
            sum += weights[i] * values[i];
            min = Math.min(min, values[i]);
        }

        return sum + 10 * min + values[0] * weights[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] randomWeightVector() {
        return randomWeightVector(mNumValues);
    }

}
