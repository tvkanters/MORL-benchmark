package nl.uva.morlb.rg.experiment.model;

import java.security.InvalidParameterException;

/**
 * A product scalarisation function that simply multiplies everything.
 */
public class ProductScalarisation implements Scalarisation {

    /**
     * {@inheritDoc}
     *
     * Calculates the product of all the values and weights.
     */
    @Override
    public double scalarise(final double[] values, final double[] weights) {
        if (values.length != weights.length) {
            throw new InvalidParameterException("Values must have the same length as the weights");
        }

        double scalar = 1;
        for (int i = 0; i < values.length; ++i) {
            scalar *= values[i] * weights[i];
        }

        return scalar;
    }
}
