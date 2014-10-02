package nl.uva.morlb.rg.agent.model;

import java.util.Arrays;

import nl.uva.morlb.rg.experiment.model.Solution;

/**
 * Represents the reward array
 */
public class BenchmarkReward {

    private final double[] mReward;

    public BenchmarkReward(final double[] reward) {
        mReward = reward;
    }

    /**
     * Adds another reward to this reward
     *
     * @param other
     * @return this
     */
    public BenchmarkReward add(final BenchmarkReward other) {
        return add(other, 1.0d);
    }

    /**
     * Subtracts another reward from this reward
     *
     * @param other
     * @return this
     */
    public BenchmarkReward sub(final BenchmarkReward other) {
        return sub(other, 1.0d);
    }

    /**
     * Adds another discounted reward to this reward
     *
     * @param other
     * @return this
     */
    public BenchmarkReward add(final BenchmarkReward other, final double discount) {
        if (other.getDimension() != getDimension()) {
            throw new RuntimeException("Dimensions are not aligned");
        }

        final double[] newReward = Arrays.copyOf(mReward, mReward.length);

        for (int i = 0; i < mReward.length; ++i) {
            newReward[i] += discount * other.mReward[i];
        }

        return new BenchmarkReward(newReward);
    }

    /**
     * Subtracts another discounted reward from this reward
     *
     * @param other
     * @return this
     */
    public BenchmarkReward sub(final BenchmarkReward other, final double discount) {
        if (other.getDimension() != getDimension()) {
            throw new RuntimeException("Dimensions are not aligned");
        }

        final double[] newReward = Arrays.copyOf(mReward, mReward.length);

        for (int i = 0; i < mReward.length; ++i) {
            newReward[i] -= discount * other.mReward[i];
        }

        return new BenchmarkReward(newReward);
    }

    /**
     * Multiply a reward by a scalar value
     * @param multiplicant The scalar value
     * @return The multiplied reward
     */
    public BenchmarkReward mult(final double multiplicant) {
        final double[] newReward = Arrays.copyOf(mReward, mReward.length);

        for (int i = 0; i < mReward.length; ++i) {
            newReward[i] *= multiplicant;
        }

        return new BenchmarkReward(newReward);
    }

    /**
     * Scalarise this reward
     * @param scalar The scalar
     * @return The scalarised reward
     */
    public BenchmarkReward scalarise(final double[] scalar) {
        if (scalar.length != mReward.length) {
            throw new RuntimeException("Dimensions do not align");
        }

        final double[] scalarisedReward = Arrays.copyOf(mReward, mReward.length);

        for (int i = 0; i < scalar.length; ++i) {
            scalarisedReward[i] *= scalar[i];
        }

        return new BenchmarkReward(scalarisedReward);
    }

    /**
     * Sum all reward entries
     * @return The sum of the reward
     */
    public double getSum() {
        double result = 0;
        for (final double d : mReward) {
            result += d;
        }

        return result;
    }

    /**
     * Convert this benchmark reward to a solution
     * @return The converted solution
     */
    public Solution toSolution() {
        return new Solution(mReward);
    }

    /**
     * Get the dimensionality of this reward
     *
     * @return The dimensionality of this reward
     */
    public int getDimension() {
        return mReward.length;
    }

    /**
     * Returns the reward for the given objective
     * @param rewardPosition The given objective
     * @return The reward for the given objective
     */
    public double getRewardForObjective(final int rewardPosition) {
        return mReward[rewardPosition];
    }

    /**
     * Get the complete reward vector
     * @return The complete reward vector
     */
    public double[] getRewardVector() {
        return mReward;
    }

    @Override
    public String toString() {
        String result = "[ ";
        for(double reward : mReward) {
            result += reward +" ";
        }
        result += " ]";

        return result;
    }

    /**
     * Calculate the minimum reward entry
     * @return The smallest reward entry
     */
    public double getMinimumRewardEntry() {
        double minimum = Double.POSITIVE_INFINITY;
        for(double rewardEntry : mReward) {
            if(rewardEntry < minimum) {
                minimum = rewardEntry;
            }
        }

        return minimum;
    }

    /**
     * Calculate the length of this reward vector
     * @return The length of this vector
     */
    public double getLength() {
        double result = 0;
        for(double entry : mReward) {
            result += Math.pow(entry, 2.0);
        }

        return Math.sqrt(result);
    }

}
