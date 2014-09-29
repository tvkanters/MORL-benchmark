package nl.uva.morlb.rg.agent.convexhull;

import java.util.LinkedList;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

/**
 * A pruner that converts solution sets to convex hulls
 */
public class ConvexHullPruner {

    /**
     * Prunes a solution set to the convex coverage set.
     *
     * @param solutionSet
     *            The solution set to prune
     *
     * @return The pruned solution set
     */
    public static SolutionSet prune(final SolutionSet solutionSet) {
        // Make sure we're only dealing with the Pareto front
        final SolutionSet paretoFront = solutionSet.copy();
        paretoFront.pruneDominatedSolutions();
        final LinkedList<Solution> solutions = new LinkedList<Solution>(paretoFront.getSolutions());

        final int numObjectives = paretoFront.getNumObjectives();
        final SolutionSet convexCoverageSet = new SolutionSet(numObjectives);

        // Collect the solutions that are best for a single objective
        for (int i = 0; i < numObjectives; i++) {
            Solution bestSolution = null;
            for (final Solution solution : solutions) {
                if (bestSolution == null || solution.getValues()[i] > bestSolution.getValues()[i]) {
                    bestSolution = solution;
                }
            }
            convexCoverageSet.addSolution(bestSolution);
        }
        solutions.removeAll(convexCoverageSet.getSolutions());

        // Find the weights for each solution and save ones composing the convex hull
        final Scalarisation scalarisation = new LinearScalarisation();
        while (!solutions.isEmpty()) {
            Solution target = solutions.peekFirst();
            final double[] weights = findWeights(target, convexCoverageSet);
            if (weights != null) {
                // Find which solution is the best for the current weights
                double bestScalar = Double.MIN_VALUE;
                for (final Solution solution : solutions) {
                    final double scalar = scalarisation.scalarise(solution.getValues(), weights);
                    if (scalar > bestScalar) {
                        target = solution;
                        bestScalar = scalar;
                    }
                }

                convexCoverageSet.addSolution(target);
            }

            solutions.remove(target);
        }

        return convexCoverageSet;
    }

    /**
     * Find the weights for a target in a solution set
     *
     * @param target
     *            The target solution to base the weights on
     * @param solutionSet
     *            The solution set to find the weights for
     *
     * @return The weights
     */
    public static double[] findWeights(final Solution target, final SolutionSet solutionSet) {
        double[] result = null;
        final int numObjectives = target.getNumObjectives();

        LpSolve solver = null;
        try {
            solver = LpSolve.makeLp(0, numObjectives + 1);
            solver.setVerbose(1);

            // Add constraint per solution based on the difference in each dimension
            for (final Solution solution : solutionSet.getSolutions()) {
                final double[] constraint = new double[numObjectives + 2];
                for (int dim = 0; dim < numObjectives; ++dim) {
                    constraint[dim + 1] = (target.getValues()[dim] - solution.getValues()[dim]);
                }
                constraint[numObjectives + 1] = -1;
                solver.addConstraint(constraint, LpSolve.GE, 0);
            }

            // Add a fixed constraint
            final double[] constraint = new double[numObjectives + 2];
            for (int j = 0; j <= numObjectives; ++j) {
                constraint[j + 1] = 1;
            }
            constraint[numObjectives + 1] = 0;
            solver.addConstraint(constraint, LpSolve.EQ, 1);

            // Set the objective function weights
            final double[] objective = new double[numObjectives + 2];
            objective[numObjectives + 1] = -1;
            solver.setObjFn(objective);

            // Solve the problem and gather the results
            solver.solve();
            if (solver.getStatus() == 0) {
                final double[] var = solver.getPtrVariables();
                result = new double[numObjectives];
                for (int i = 0; i < var.length - 1; ++i) {
                    result[i] = var[i];
                }
            }

        } catch (final LpSolveException ex) {
            throw new RuntimeException(ex);

        } finally {
            // Free memory
            if (solver != null) {
                solver.deleteLp();
            }
        }

        return result;
    }

    public static void main(final String[] args) {
        final SolutionSet solutionSet = new SolutionSet("(0,3),(3,0),(1,1),(0.5,0.5),(0.5,2.5)");

        System.out.println(ConvexHullPruner.prune(solutionSet));
    }
}
