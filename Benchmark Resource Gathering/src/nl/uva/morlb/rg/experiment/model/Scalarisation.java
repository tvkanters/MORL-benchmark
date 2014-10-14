package nl.uva.morlb.rg.experiment.model;

import java.util.Random;

/**
 * The abstract class for scalarisation functions that turn multi-objective rewards into a single reward given the
 * weights.
 */
public abstract class Scalarisation {

    /** The minimum value a weight can take */
    protected double mMinWeight = 0;
    /** The maximum value a weight can take */
    protected double mMaxWeight = 1;
    /** The seed for the random generator */
    protected long mSeed = 314;
    /** The random function which represents the distribution over weights */
    protected Random mRand = new Random(mSeed);

    /**
     * Scalarises the multi-objective rewards into a single reward given the weights.
     * 
     * @param values
     *            The rewards to scalarise
     * @param weights
     *            The weights used to perform scalarisation
     * @return The scalarised reward
     */
    public abstract double scalarise(final double[] values, final double[] weights);

    /**
     * Returns a random weight according to the distribution over weights that's belonging to the scalarisation
     * function. This distribution is uniform by default.
     * 
     * @return a random (uniformly drawn) weight
     */
    public double randomWeight() {
        return mRand.nextDouble();
    }

    /**
     * Returns a vector of randomly drawn weights that can be input into the scalarise method
     * 
     * @param dimensions
     *            The dimensions of the weight vector
     * @return a randomly drawn weight vector
     */
    protected double[] randomWeightVector(final int dimensions) {
        final double[] randomWeightVector = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            randomWeightVector[i] = mRand.nextDouble();
        }
        return randomWeightVector;
    }

    /**
     * Returns a vector of randomly drawn weights that can be input into the scalarise method
     * 
     * @param dimensions
     *            The dimensions of the weight vector
     * @return a randomly drawn weight vector
     */
    public abstract double[] randomWeightVector();

    /**
     * Returns the minimum value a weight can take in the scalarisation function
     * 
     * @return the minimum possible weight
     */
    public double getMinWeight() {
        return mMinWeight;
    }

    /**
     * Returns the maximum value a weight can take in the scalarisation function
     * 
     * @return the maximum possible weight
     */
    public double getMaxWeight() {
        return mMaxWeight;
    }
}
