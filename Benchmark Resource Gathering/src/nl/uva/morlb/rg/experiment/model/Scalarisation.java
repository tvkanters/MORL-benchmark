package nl.uva.morlb.rg.experiment.model;

/**
 * The interface for scalarisation functions that turn multi-objective rewards into a single reward given the weights.
 */
public interface Scalarisation {

    /**
     * Scalarises the multi-objective rewards into a single reward given the weights.
     *
     * @param values
     *            The rewards to scalarise
     * @param weights
     *            The weights used to perform scalarisation
     * @return The scalarised reward
     */
    public double scalarise(double[] values, double[] weights);

}
