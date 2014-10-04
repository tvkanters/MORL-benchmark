package nl.uva.morlb.rg.experiment;

import java.util.HashMap;
import java.util.Map;

import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

/**
 * A collection of optimal solution sets for problem parameters.
 */
public class OptimalSolutions {

    /** The mapping from parameters to optimal solutions */
    private static final Map<Parameters, SolutionSet> sOptimalSolutions = new HashMap<>();

    static {
        // The solutions for the simple problems
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 1 2 1 1 1 2 1 1 1", null),
                new SolutionSet("(-6,1,0),(-6,0,1),(-8,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 3 3 1 1 1 3 0 1 1", null),
                new SolutionSet("(-6,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 1 3 1 1 1 0 1 1 1", null),
                new SolutionSet("(-6,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 3 1 1 1 1 0 0 1 1", null),
                new SolutionSet("(-6,1,0),(-7,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 2 0 1 1 1 0 3 1 1", null),
                new SolutionSet("(-6,0,1),(-6,1,0),(-10,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 2 1 1 1 1 0 1 1 1", null),
                new SolutionSet("(-6,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 1 0 1 1 1 3 2 1 1", null),
                new SolutionSet("(-6,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 2 2 1 1 1 0 2 1 1", null),
                new SolutionSet("(-6,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 0 3 1 1 1 1 2 1 1", null),
                new SolutionSet("(-6,0,1),(-6,1,0),(-8,1,1)"));
        sOptimalSolutions.put(Parameters.fromString("3 3 1 1 0 Infinity false 2147483647 0 1 1 1 1 1 1 2 1 1", null),
                new SolutionSet("(-6,1,1)"));

        // The solutions for the large problems
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 3 2 1 1 1 2 5 1 1 1 4 9 1 1 0 8 4 1 1 2 4 1 1 1", null), new SolutionSet(
                "(-18,0,2,0),(-18,0,1,1),(-18,1,0,1),(-18,2,0,0),(-20,1,2,0),(-20,2,0,1),(-20,1,1,1),(-22,1,2,1),"
                        + "(-32,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 5 1 1 1 1 6 9 1 1 1 2 0 1 1 0 6 4 1 1 2 7 4 1 1", null), new SolutionSet(
                "(-18,2,2,0),(-18,1,2,1),(-20,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 5 7 1 1 1 5 9 1 1 1 3 8 1 1 0 9 6 1 1 2 8 1 1 1", null), new SolutionSet(
                "(-18,1,0,1),(-18,0,2,0),(-18,1,1,0),(-20,1,2,0),(-20,2,0,0),(-26,2,2,0),(-26,2,1,1),(-28,1,2,1),"
                        + "(-30,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 9 0 1 1 1 2 1 1 1 1 7 5 1 1 0 3 2 1 1 2 5 5 1 1", null), new SolutionSet(
                "(-18,1,2,1),(-22,2,1,0),(-24,2,2,0),(-30,2,1,1),(-32,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 8 1 1 1 1 0 7 1 1 1 3 9 1 1 0 9 7 1 1 2 0 7 1 1", null), new SolutionSet(
                "(-18,0,2,1),(-18,1,1,1),(-18,0,0,2),(-22,1,2,1),(-30,2,1,0),(-32,2,1,1),(-34,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 9 8 1 1 1 2 9 1 1 1 1 6 1 1 0 5 6 1 1 2 2 8 1 1", null), new SolutionSet(
                "(-18,0,2,1),(-18,2,1,0),(-18,1,1,1),(-20,1,2,1),(-22,2,1,1),(-24,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 4 2 1 1 1 8 6 1 1 1 5 9 1 1 0 2 2 1 1 2 0 8 1 1", null), new SolutionSet(
                "(-18,0,1,1),(-18,2,1,0),(-22,1,1,1),(-24,2,1,1),(-24,0,2,1),(-24,2,2,0),(-34,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 0 5 1 1 1 5 7 1 1 1 0 2 1 1 0 4 3 1 1 2 1 6 1 1", null), new SolutionSet(
                "(-18,1,2,1),(-22,2,2,0),(-24,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 2 5 1 1 1 2 3 1 1 1 9 1 1 1 0 4 3 1 1 2 0 3 1 1", null), new SolutionSet(
                "(-18,1,1,1),(-22,2,1,1),(-22,1,2,1),(-26,2,2,1)"));
        sOptimalSolutions.put(Parameters.fromString("9 9 1 1 0 Infinity false 2147483647 "
                + "0 9 5 1 1 1 1 0 1 1 1 3 0 1 1 0 5 2 1 1 2 5 7 1 1", null), new SolutionSet(
                "(-18,2,2,0),(-18,1,2,1),(-22,2,2,1)"));
    }

    /**
     * Retrieves the optimal solution set given a set of parameters.
     *
     * @param parameters
     *            The problem parameters
     *
     * @return The optimal solution set or null if none exist for the parameters
     */
    public static SolutionSet getSolution(final Parameters parameters) {
        return sOptimalSolutions.get(parameters);
    }

}
