package nl.uva.morlb.rg.experiment;

import java.util.HashMap;
import java.util.Map;

import nl.uva.morlb.rg.environment.SdpCollection;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

/**
 * A collection of optimal solution sets for problem parameters.
 */
public class OptimalSolutions {

    /** The mapping from parameters to optimal solutions */
    private static final Map<Parameters, SolutionSet> sOptimalSolutions = new HashMap<>();

    static {
        sOptimalSolutions.put(SdpCollection.getSimpleProblem(), new SolutionSet("(-6,1,0),(-6,0,1),(-8,1,1)"));
        sOptimalSolutions.put(SdpCollection.getFullActionsProblem(), new SolutionSet(""));
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
