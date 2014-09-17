package nl.uva.morlb.rg.agent;

import java.util.Arrays;

/**
 * Represents the reward array
 */
public class StateValue{

    private final double[] mReward;

    public StateValue(final double[] reward) {
        mReward = reward;
    }

    /**
     * Adds another reward to this reward
     * 
     * @param other
     * @return this
     */
    public StateValue add(final StateValue other) {
        return add(other, 1.0d);
    }

    /**
     * Subtracts another reward from this reward
     * 
     * @param other
     * @return this
     */
    public StateValue sub(final StateValue other) {
        return sub(other, 1.0d);
    }

    /**
     * Adds another discounted reward to this reward
     * 
     * @param other
     * @return this
     */
    public StateValue add(final StateValue other, final double discount) {
        if(other.getDimension() != this.getDimension()) {
            throw new RuntimeException("Dimensions are not alligned");
        }

        double[] newReward = Arrays.copyOf(mReward, mReward.length);

        for(int i = 0; i < mReward.length; ++i) {
            newReward[i] += discount* other.mReward[i];
        }

        return new StateValue(newReward);
    }

    /**
     * Subtracts another discounted reward from this reward
     * 
     * @param other
     * @return this
     */
    public StateValue sub(final StateValue other, final double discount) {
        if(other.getDimension() != this.getDimension()) {
            throw new RuntimeException("Dimensions are not alligned");
        }

        double[] newReward = Arrays.copyOf(mReward, mReward.length);

        for(int i = 0; i < mReward.length; ++i) {
            newReward[i] -= discount* other.mReward[i];
        }

        return new StateValue(newReward);
    }

    public StateValue scalarize(final double[] scalar) {
        if(scalar.length != mReward.length) {
            throw new RuntimeException("Dimensions do not allign");
        }

        double[] scalarizedReward = Arrays.copyOf(mReward, mReward.length);

        for(int i = 0; i < scalar.length; ++i) {
            scalarizedReward[i] *= scalar[i];
        }

        return new StateValue(scalarizedReward);
    }

    public double getSum() {
        double result = 0;
        for(double d : mReward) {
            result += d;
        }

        return result;
    }

    /**
     * Get the dimensionality of this reward
     * @return The dimensionality of this reward
     */
    public int getDimension() {
        return mReward.length;
    }

}
