package nl.uva.morlb.rg.experiment;

import nl.uva.morlb.rg.experiment.model.ParetoFront;
import nl.uva.morlb.rg.experiment.model.Scalarisation;

/**
 * A class that gives scores/ranks to algorithms based on the quality of the provided Pareto fronts.
 */
public class Judge {

    /**
     * Judges a Pareto front based on the given scalarisation function.
     *
     * @param paretoFront
     *            The Pareto front to rank
     * @param scalarisation
     *            The scalarisation function used to calculate rewards
     *
     * @return The Pareto front's score
     */
    public double judge(final ParetoFront paretoFront, final Scalarisation scalarisation) {
        return 0;
    }

    /**
     * Judges a Pareto front compared to the optimal reference front based on the given scalarisation function.
     *
     * @param paretoFront
     *            The Pareto front to rank
     * @param referenceFront
     *            The optimal front used to compare the other front with
     * @param scalarisation
     *            The scalarisation function used to calculate rewards
     *
     * @return The Pareto front's score
     */
    public double judge(final ParetoFront paretoFront, final ParetoFront referenceFront,
            final Scalarisation scalarisation) {
        return 0;
    }

}
