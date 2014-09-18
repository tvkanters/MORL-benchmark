package nl.uva.morlb.rg.experiment.model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Pareto front populated with policies that are represented by coordinates. I.e., the value for each objective.
 */
public class ParetoFront {

    /** The amount of objectives policies have values for */
    private final int mNumObjectives;

    /** The policies in the Pareto front */
    private final List<ParetoPolicy> mPolicies = new ArrayList<>();

    /**
     * Creates a Pareto front with a given amount of objectives.
     *
     * @param numObjectives
     *            The amount of objectives policies have values for
     */
    public ParetoFront(final int numObjectives) {
        mNumObjectives = numObjectives;
    }

    /**
     * Creates a Pareto front from a string representation in the form of (v,...,v),...,(v,...,v).
     *
     * @param paretoFront
     *            The string representation of the Pareto front
     */
    public ParetoFront(final String paretoFront) {
        final String[] policies = paretoFront.substring(1, paretoFront.length() - 1).replaceAll(" ", "")
                .split("\\),\\(");
        mNumObjectives = policies[0].length() - policies[0].replace(",", "").length() + 1;
        for (final String policy : policies) {
            final double[] values = new double[mNumObjectives];

            final String[] valuesStr = policy.split(",");
            for (int i = 0; i < values.length; ++i) {
                values[i] = Double.parseDouble(valuesStr[i]);
            }

            addPolicy(new ParetoPolicy(values));
        }
    }

    /**
     * Adds a policy to the Pareto front. The policy's number of objectives must match that of the front.
     *
     * @param policy
     *            The policy to add
     */
    public void addPolicy(final ParetoPolicy policy) {
        if (policy.getNumObjectives() != mNumObjectives) {
            throw new InvalidParameterException(
                    "The number of objectives in the policy must match that of the Pareto front");
        }

        mPolicies.add(policy);
    }

    /**
     * @return An immutable list of policies in the Pareto front
     */
    public List<ParetoPolicy> getPolicies() {
        return Collections.unmodifiableList(mPolicies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < mPolicies.size(); ++i) {
            str += (i != 0 ? "," : "") + mPolicies.get(i);
        }
        return str;
    }

}
