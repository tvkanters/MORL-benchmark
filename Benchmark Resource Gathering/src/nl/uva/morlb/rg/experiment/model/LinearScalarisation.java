package nl.uva.morlb.rg.experiment.model;

import java.security.InvalidParameterException;

/**
 * A linear scalarisation function.
 */
public class LinearScalarisation implements Scalarisation {

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
}
