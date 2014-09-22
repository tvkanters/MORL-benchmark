package nl.uva.morlb.rg.experiment.model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A solution set populated with values of policies (solutions), represented by coordinates. I.e., the value for each
 * objective.
 */
public class SolutionSet {

    /** The amount of objectives solutions have values for */
    private final int mNumObjectives;

    /** The solutions in the solution set */
    private final List<Solution> mSolutions = new ArrayList<>();

    /**
     * Creates a solution set with a given amount of objectives.
     *
     * @param numObjectives
     *            The amount of objectives a solution has
     */
    public SolutionSet(final int numObjectives) {
        mNumObjectives = numObjectives;
    }

    /**
     * Creates a solution set from a string representation in the form of (v,...,v),...,(v,...,v).
     *
     * @param solutionSet
     *            The string representation of the solution set
     */
    public SolutionSet(final String solutionSet) {
        final String[] solutions = solutionSet.substring(1, solutionSet.length() - 1).replaceAll(" ", "")
                .split("\\),\\(");
        mNumObjectives = solutions[0].length() - solutions[0].replace(",", "").length() + 1;
        for (final String solution : solutions) {
            final double[] values = new double[mNumObjectives];

            final String[] valuesStr = solution.split(",");
            for (int i = 0; i < values.length; ++i) {
                values[i] = Double.parseDouble(valuesStr[i]);
            }

            addSolution(new Solution(values));
        }
    }

    /**
     * Adds a solution to the solution set. The solution's number of objectives must match that of the set.
     *
     * @param solution
     *            The solution to add
     */
    public void addSolution(final Solution solution) {
        if (solution.getNumObjectives() != mNumObjectives) {
            throw new InvalidParameterException(
                    "The number of objectives in the solution must match that of the solution set");
        }

        mSolutions.add(solution);
    }

    /**
     * @return An immutable list of solutions in the solution set
     */
    public List<Solution> getSolutions() {
        return Collections.unmodifiableList(mSolutions);
    }

    /**
     * @return The number of objectives solutions have values for
     */
    public int getNumObjectives() {
        return mNumObjectives;
    }

    /**
     * @return The cardinality of the solution set
     */
    public int getNumSolutions() {
        return Collections.unmodifiableList(mSolutions).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < mSolutions.size(); ++i) {
            str += (i != 0 ? "," : "") + mSolutions.get(i);
        }
        return str;
    }

}