package nl.uva.morlb.rg.experiment;

import jmetal.qualityIndicator.Hypervolume;
import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.ProductScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

/**
 * A class that gives scores/ranks to algorithms based on the quality of the provided solution set.
 */
public class Judge {

    /** The default reference point for the hypervolume for the time dimension */
    public static final double HYPERVOLUME_REFERENCE_POINT_TIME = -1000;
    /** The default reference point fo rthe hypervolume for the resource dimensions */
    public static final double HYPERVOLUME_REFERENCE_POINT_RESOURCES = -1;

    /**
     * Estimates the average scalarised value and the corresponding standard deviation of a solution set achieves using
     * random weight samples and a provided scalarisation function.
     *
     * @param solutionSet
     *            The solution set that is evaluated
     * @param scalarisation
     *            The scalarisation function upon which the solution set is evaluated
     *
     * @return double array of the average reward and the standard deviation that was estimated for the solution set
     */
    public static double[] averageReward(final SolutionSet solutionSet, final Scalarisation scalarisation) {
        // the number of weight values we want to test per objective
        final int weightValuesPerObjective = 2;
        // total number of tests that will be performed
        final int totalNumTests = (int) Math.pow(weightValuesPerObjective,
                solutionSet.getNumObjectives());

        // the weight vector
        double rewardSum = 0;
        double rewardSqSum = 0; // for the variance
        // perform the tests
        for (int test = 0; test < totalNumTests; test++) {
            double[] weights = scalarisation.randomWeightVector(solutionSet.getNumObjectives());
            // find the point in the solution set for which the scalarised value is maximal
            double maxScalarisedValue = Double.NEGATIVE_INFINITY;
            double scalarisedValue;
            Solution solution;
            for (int i = 0; i < solutionSet.getNumSolutions(); i++) {
                solution = solutionSet.getSolutions().get(i);
                scalarisedValue = scalarisation.scalarise(solution.getValues(), weights);
                maxScalarisedValue = Math.max(maxScalarisedValue, scalarisedValue);
            }
            // sum up the maximal scalarised values (the value that this solution would get for the given weights)
            rewardSum += maxScalarisedValue;
            rewardSqSum += Math.pow(maxScalarisedValue, 2);
        }
        // return the average reward that this solution set received across the performed tests
        double averageReward = rewardSum / totalNumTests;
        double variance = (rewardSqSum - Math.pow(rewardSum, 2) / totalNumTests) / (totalNumTests - 1);
        double standardDev = Math.sqrt(variance);
        double[] returnArray = { averageReward, standardDev };
        return returnArray;
    }

    /**
     * Estimates the additive epsilon indicator for a given solution set and a reference set, i.e. the smallest epsilon
     * which has to be added to the solution set so that it weakly dominates the reference set. (See E. Zitzler, L.
     * Thiele, M. Laumanns, C.M. Fonesca, V. Grunert da Fonseca: Performance Assessment of Multiobjective Optimizers: An
     * Analysis and Review. IEEE Transactions on Evolutionary Computation 7(2), 117-132 (2003))
     *
     * @param solutionSet
     *            The solution set that is evaluated
     * @param referenceSet
     *            The true Pareto front or a good approximation to which the solution can be compared
     *
     * @return The additive epsilon indicator
     */
    public static double additiveEpsilonIndicator(final SolutionSet solutionSet, final SolutionSet referenceSet) {
        if (referenceSet.getNumObjectives() != solutionSet.getNumObjectives()) {
            System.err.println("Reference and solution set must have same number of objectives");
        }
        Solution ref;
        Solution sol;
        // the smallest epsilon for which it is true that for all values v from the reference set there exists one value
        // v' from the solution set that weakly epsilon-dominates v
        double epsilon = Double.NEGATIVE_INFINITY;
        for (int refIndex = 0; refIndex < referenceSet.getNumSolutions(); refIndex++) {
            ref = referenceSet.getSolutions().get(refIndex);
            // singleEpsilon is the smallest epsilon so that there exists one (!) solution in the solution set that
            // weakly epsilon-dominates the current reference point ref
            double singleEpsilon = Double.POSITIVE_INFINITY;
            for (int solIndex = 0; solIndex < solutionSet.getNumSolutions(); solIndex++) {
                sol = solutionSet.getSolutions().get(solIndex);
                // maxEpsilonPerSingleDim is the smallest epsilon for which the current solution sol weakly
                // epsilon-dominates the currect reference point ref
                double maxEpsilonPerSingleDim = Double.NEGATIVE_INFINITY;
                for (int dim = 0; dim < solutionSet.getNumObjectives(); dim++) {
                    final double distance = ref.getValues()[dim] - sol.getValues()[dim];
                    maxEpsilonPerSingleDim = Math.max(distance, maxEpsilonPerSingleDim);
                }
                singleEpsilon = Math.min(singleEpsilon, maxEpsilonPerSingleDim);
            }
            // pick the epsilon that's smallest but satisfies the epsilon-condition across all points from the reference
            // set
            epsilon = Math.max(singleEpsilon, epsilon);
        }
        return epsilon;
    }

    /**
     * Estimates the multiplicative epsilon indicator for a given solution set and a reference set, i.e. the smallest
     * epsilon so that to the solution weakly dominates the reference set if multiplied with the factor epsilon. (See E.
     * Zitzler, L. Thiele, M. Laumanns, C.M. Fonesca, V. Grunert da Fonseca: Performance Assessment of Multiobjective
     * Optimizers: An Analysis and Review. IEEE Transactions on Evolutionary Computation 7(2), 117-132 (2003))
     *
     * @param solutionSet
     *            The solution set that is evaluated
     * @param referenceSet
     *            The true Pareto front or a good approximation to which the solution can be compared
     *
     * @return The multiplicative epsilon indicator
     */
    public static double multiplicativeEpsilonIndicator(final SolutionSet solutionSet, final SolutionSet referenceSet) {
        if (referenceSet.getNumObjectives() != solutionSet.getNumObjectives()) {
            System.err.println("Reference and solution set must have same number of objectives");
        }
        Solution sol;
        Solution ref;
        // the smallest epsilon for which it is true that for all values v from the reference set there exists one value
        // v' from the solution set that weakly epsilon-dominates v
        double epsilon = Double.NEGATIVE_INFINITY;
        for (int refIndex = 0; refIndex < referenceSet.getNumSolutions(); refIndex++) {
            ref = referenceSet.getSolutions().get(refIndex);
            // singleEpsilon is the smallest epsilon so that there exists one (!) solution in the solution set that
            // weakly epsilon-dominates the current reference point ref
            double singleEpsilon = Double.POSITIVE_INFINITY;
            for (int solIndex = 0; solIndex < solutionSet.getNumSolutions(); solIndex++) {
                sol = solutionSet.getSolutions().get(solIndex);
                // maxEpsilonPerSingleDim is the smallest epsilon for which the current solution sol weakly
                // epsilon-dominates the currect reference point ref
                double maxEpsilonPerSingleDim = Double.NEGATIVE_INFINITY;
                for (int dim = 0; dim < solutionSet.getNumObjectives(); dim++) {
                    final double distance = ref.getValues()[dim] / sol.getValues()[dim];
                    maxEpsilonPerSingleDim = Math.max(distance, maxEpsilonPerSingleDim);
                }
                singleEpsilon = Math.min(singleEpsilon, maxEpsilonPerSingleDim);
            }
            // pick the epsilon that's smallest but satisfies the epsilon-condition across all points from the reference
            // set
            epsilon = Math.max(singleEpsilon, epsilon);
        }
        return epsilon;
    }

    /**
     * Overall Nondominated Vector Generation (ONVG) gives an indication of convergence for a provided solution set. It
     * is literally just the number of solutions the algorithm found. It should neither be too high nor too low, and is
     * only useful in combination with other quality indicators, or in comparison with the ONVG of a reference set. (See
     * J. R. Schott: Fault tolerant design using single and multicriteria genetic algorithm optimization, M.S. thesis,
     * Dept. Aeronautics Astronautics, Massachusetts Instit. Technology, Cambridge, MA, USA, 1995)
     *
     * @param solutionSet
     *            The solution set that is evaluated
     *
     * @return The number of solutions in the solution set
     */
    public static int overallNondominatedVectorGeneration(final SolutionSet solutionSet) {
        return solutionSet.getNumSolutions();
    }

    /**
     * This metric gives an indication to how uniformly a provided solution set is distributed. The lower this value,
     * the better. The runtime is quadratic in the number of objectives. (See J. R. Schott: Fault tolerant design using
     * single and multicriteria genetic algorithm optimization, M.S. thesis, Dept. Aeronautics Astronautics,
     * Massachusetts Instit. Technology, Cambridge, MA, USA, 1995)
     *
     * @param solutionSet
     *            The solution set that is evaluated
     *
     * @return The indicator of how evenly the solutions in the solution set are distributed. A lower value indicates a
     *         better(more uniform) distribution. Returns POSITIVE_INIFINITY if there exists no or only one solution.
     */
    public static double schottSpacingMetric(final SolutionSet solutionSet) {
        // estimate the minimal distances d_i for each solution i
        final double[] minDistances = new double[solutionSet.getNumSolutions()];
        int index = 0;
        for (int i = 0; i < solutionSet.getNumSolutions(); i++) {
            double minDistance = Double.POSITIVE_INFINITY;
            for (int j = 0; j < solutionSet.getNumSolutions(); j++) {
                if (i != j) {
                    double distance = 0;
                    for (int dim = 0; dim < solutionSet.getNumObjectives(); dim++) {
                        // sum up the dimension-wise distances between two distinct solutions in the solution set
                        distance += Math.abs(solutionSet.getSolutions().get(i).getValues()[dim]
                                - solutionSet.getSolutions().get(j).getValues()[dim]);
                    }
                    minDistance = Math.min(minDistance, distance);
                }
            }
            minDistances[index] = minDistance;
            index++;
        }
        // estimate the average of the d_i
        double averageMinDist = 0;
        for (int k = 0; k < minDistances.length; k++) {
            averageMinDist += minDistances[k];
        }
        averageMinDist /= minDistances.length;
        // build the sum inside the sqrt of the schott metric
        double sum = 0;
        for (int l = 0; l < solutionSet.getNumSolutions(); l++) {
            sum += Math.pow(averageMinDist - minDistances[l], 2);
        }
        // estimate the value of the schott metric (delta)
        double delta;
        if (solutionSet.getNumSolutions() > 1) {
            delta = Math.sqrt(sum / (solutionSet.getNumSolutions() - 1));
        } else {
            delta = Double.POSITIVE_INFINITY;
        }
        return delta;
    }

    /**
     * Estimates the maximum spread of a solution set, which is an indicator for how well the solutions are spread. A
     * higher value indicates a better spread of solutions. (E. Zitzler, K. Deb, and L. Thiele: Comparison of
     * multiobjective evolutionary algorithms: Empirical results, Evol. Comput., vol. 8, no. 2, pp. 173-195, Jun. 2000)
     *
     * @param solutionSet
     *            The solution set that is evaluated
     *
     * @return The maximum spread. A higher value indicates a beter (larger) spread.
     */
    public static double maximumSpread(final SolutionSet solutionSet) {
        double sum = 0;
        for (int dim = 0; dim < solutionSet.getNumObjectives(); dim++) {
            double maxVal = Double.NEGATIVE_INFINITY;
            double minVal = Double.POSITIVE_INFINITY;
            for (int s = 0; s < solutionSet.getNumSolutions(); s++) {
                final double solutionValue = solutionSet.getSolutions().get(s).getValues()[dim];
                maxVal = Math.max(solutionValue, maxVal);
                minVal = Math.min(solutionValue, minVal);
            }
            sum += Math.pow(maxVal - minVal, 2);
        }
        final double maxSpread = Math.sqrt(sum);
        return maxSpread;
    }

    /**
     * Estimates the hypervolume of a soulution set (and a default reference point). The runtime is exponential in the
     * number of objectives. (See E. Zitzler and L. Thiele Multiobjective Evolutionary Algorithms: A Comparative Case
     * Study and the Strength Pareto Approach, IEEE Transactions on Evolutionary Computation, vol. 3, no. 4, pp.
     * 257-271, 1999)
     *
     * @param solutionSet
     *            The solution set that is evaluated
     *
     * @return the hypervolume of the solution set
     */
    public static double hypervolume(final SolutionSet solutionSet) {
        // set up default reference point
        final double[] referencePoint = new double[solutionSet.getNumObjectives()];
        referencePoint[0] = HYPERVOLUME_REFERENCE_POINT_TIME;
        for (int d = 1; d < solutionSet.getNumObjectives(); d++) {
            referencePoint[d] = HYPERVOLUME_REFERENCE_POINT_RESOURCES;
        }
        return hypervolume(solutionSet, referencePoint);
    }

    /**
     * Estimates the hypervolume of a soulution set given a reference point. The runtime is exponential in the number of
     * objectives. (See E. Zitzler and L. Thiele Multiobjective Evolutionary Algorithms: A Comparative Case Study and
     * the Strength Pareto Approach, IEEE Transactions on Evolutionary Computation, vol. 3, no. 4, pp. 257-271, 1999)
     *
     * @param solutionSet
     *            The solution set that is evaluated
     * @param referencePoint
     *            The reference point for the hypervolume
     *
     * @return
     */
    public static double hypervolume(final SolutionSet solutionSet, final double[] referencePoint) {
        if (referencePoint.length != solutionSet.getNumObjectives()) {
            System.err
            .println("For the hypervolume the reference point has to have the same dimension as the solutions. Will take default reference point.");
            return hypervolume(solutionSet);
        } else {
            // put the solution set into a double array of doubles and shift them according to reference point
            final double[][] solutionSetDoubleArray = new double[solutionSet.getNumSolutions()][solutionSet
                                                                                                .getNumObjectives()];
            for (int sol = 0; sol < solutionSet.getNumSolutions(); sol++) {
                final double[] solutionValues = solutionSet.getSolutions().get(sol).getValues();
                for (int dim = 0; dim < solutionSet.getNumObjectives(); dim++) {
                    solutionSetDoubleArray[sol][dim] = solutionValues[dim] - referencePoint[dim];
                }
            }
            // calculate hypervolume
            final Hypervolume hypervolume = new Hypervolume();
            return hypervolume.calculateHypervolume(solutionSetDoubleArray, solutionSet.getNumSolutions(),
                    solutionSet.getNumObjectives());
        }
    }

    /**
     * Performs some tests on this class
     *
     * @param args
     */
    public static void main(final String[] args) {

        // test set
        final SolutionSet testSolutionSet01 = new SolutionSet("(1,0),(0,1)");
        final SolutionSet testSolutionSet02 = new SolutionSet("(1,1,6,1)");

        // create a scalarization function
        final LinearScalarisation linearScalarisation = new LinearScalarisation();
        final ProductScalarisation productScalarisation = new ProductScalarisation();

        // perform tests
        final double[] avgRew = averageReward(testSolutionSet01, linearScalarisation);
        System.out.println("Average Reward: " + avgRew[0]);
        System.out.println("Standard Deviation: " + avgRew[1]);

        final double addEps = additiveEpsilonIndicator(testSolutionSet01, testSolutionSet02);
        System.out.println("Additive Epsilon Indicator : " + addEps);

        final double multEps = multiplicativeEpsilonIndicator(testSolutionSet01, testSolutionSet02);
        System.out.println("Multiplicative Epsilon Indicator: " + multEps);

        final int oNVG = overallNondominatedVectorGeneration(testSolutionSet01);
        System.out.println("ONVG: " + oNVG);

        final double unif = schottSpacingMetric(testSolutionSet01);
        System.out.println("Uniformity measure: " + unif);

        final double spread = maximumSpread(testSolutionSet01);
        System.out.println("Spread measure: " + spread);

        final double hypervolume = hypervolume(testSolutionSet01);
        System.out.println("Hypervolume: " + hypervolume);

    }
}
