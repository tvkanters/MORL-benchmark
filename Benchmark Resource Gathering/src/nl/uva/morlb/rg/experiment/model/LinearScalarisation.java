package nl.uva.morlb.rg.experiment.model;

import java.security.InvalidParameterException;

/**
 * A linear scalarisation function.
 */
public class LinearScalarisation extends Scalarisation {

    /** The amount of values this function accepts */
    private final int mNumValues;

    /**
     * Creates a new scalarisation function for the given amount of values.
     * 
     * @param numValues
     *            The amount of values that will be scalarised
     */
    public LinearScalarisation(final int numValues) {
        mNumValues = numValues;
    }

    /**
     * {@inheritDoc}
     * 
     * Calculates the dot product of the values and weights.
     */
    @Override
    public double scalarise(final double[] values, final double[] weights) {
        if (values.length != weights.length) {
            throw new InvalidParameterException("Values must have the same length as the weights");
        }

        double scalar = 0;
        for (int i = 0; i < values.length; ++i) {
            scalar += values[i] * weights[i];
        }

        return scalar;
    }

    /**
     * {inheritDoc}
     * 
     * Makes sure that the weights sum to one
     */
    @Override
    public double[] randomWeightVector() {
        final double[] randomWeightVector = super.randomWeightVector(mNumValues);
        // make the weights sum to one
        double sum = 0;
        for (int i = 0; i < mNumValues; i++) {
            sum += randomWeightVector[i];
        }
        for (int j = 0; j < mNumValues; j++) {
            randomWeightVector[j] /= sum;
        }
        return randomWeightVector;
    }

}
