package nl.uva.morlb.rg.environment.model;

import java.security.InvalidParameterException;

import nl.uva.morlb.util.Util;

/**
 * A reward specification for a resource.
 */
public class RewardRange {

    /** The minimum possible reward for collecting a resource */
    public final double min;
    /** The maximum possible reward for collecting a resource */
    public final double max;

    /**
     * Creates reward specification for a resource
     *
     * @param min
     *            The minimum possible reward for collecting a resource
     * @param max
     *            The maximum possible reward for collecting a resource
     */
    public RewardRange(final double min, final double max) {
        if (min > max) {
            throw new InvalidParameterException("Minimum reward may not exceed maximum reward");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Calculates the reward that should be given when a resource is collected. When the minimal reward is different
     * from a maximum reward, a uniformly chosen random reward will be returned.
     *
     * @return A reward for collecting a resource
     */
    public double calculateReward() {
        return Util.RNG.nextDouble() * (max - min) + min;
    }

    /**
     * Sums the range of this reward and another to create a new reward specification.
     *
     * @param other
     *            The reward to sum the range with
     *
     * @return The new reward
     */
    public RewardRange sum(final RewardRange other) {
        return new RewardRange(min + other.min, max + other.max);
    }

}
