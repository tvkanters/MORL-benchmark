package nl.uva.morlb.rg.experiment.model;

/**
 * A product scalarisation function that simply multiplies everything.
 */
public class MinScalarisation extends Scalarisation {

    /**
     * {@inheritDoc}
     */
    @Override
    public double scalarise(final double[] values, final double[] weights) {
        double sum = 0;
        double min = Double.MAX_VALUE;
        for (final double value : values) {
            sum += value;
            min = Math.min(min, value);
        }

        return sum + weights[0] * min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] randomWeightVector() {
        return randomWeightVector(2);
    }

}
