package nl.uva.morlb.rg.experiment;

import java.util.Random;

import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;
import nl.uva.morlb.rg.experiment.model.ProductScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;

/**
 * A class that gives scores/ranks to algorithms based on the quality of the provided solution set.
 */
public class Judge {

    /** The approximative solution an algorithm returns which has to be evaluated */
    private final SolutionSet mSolutionSet;
    /** The number of points in the solution set */
    private final int mNumSolutions;
    /** The number of objectives of the values in the solution set */
    private final int mNumObjectives;
    /** The scalarisation function */
    private final Scalarisation mScalarisation;

    /**
     * Creates a Judge which can evaluate a solution set based on a scalarisation function
     * 
     * @param solutionSet
     *            The solution set that is evaluated
     * @param scalarisation
     *            The scalarisation function
     */
    public Judge(final SolutionSet solutionSet, final Scalarisation scalarisation) {
        mSolutionSet = solutionSet;
        mNumSolutions = solutionSet.getNumSolutions();
        mNumObjectives = solutionSet.getNumObjectives();
        mScalarisation = scalarisation;
    }

    /**
     * Estimates the average scalarised value the solution set achieves using random weight samples. The bigger the
     * returned value, the better.
     * 
     * @return the average reward that was estimated for the solution set
     */
    public double averageReward() {
        // the number of weight values we want to test per objective
        final int weightValuesPerObjective = 2;
        // total number of tests that will be performed
        final int totalNumTests = (int) Math.pow((double) weightValuesPerObjective, (double) mNumObjectives);
        // the seed for the random number generator (has to be the same for each test, so that comparison makes sense;
        // better would be a uniform grid over the n-dimensional space with vectors whose values sum to 1, but that is a
        // mathematical problem of its own)
        final long seed = 666;
        // the weight vector
        double[] weights = new double[mNumObjectives];
        Random rand = new Random(seed);
        double rewardSum = 0;
        // perform the tests
        for (int i = 0; i < totalNumTests; i++) {
            // create a random vector with values between 0 and 1 (exclusive)
            double nextRand = 0;
            double sum = 0;
            for (int j = 0; j < mNumObjectives; j++) {
                while (nextRand == 0 || nextRand == 1) {
                    nextRand = rand.nextDouble();
                }
                weights[j] = nextRand;
                sum += nextRand;
                nextRand = 0;
            }
            // normalise so that the weights sum up to one
            for (int k = 0; k < mNumObjectives; k++) {
                weights[k] /= sum;
            }
            // find the point in the solution set for which the scalarised value is maximal
            double maxScalarisedValue = Double.NEGATIVE_INFINITY;
            double scalarisedValue;
            Solution solution;
            for (int l = 0; l < mNumSolutions; l++) {
                solution = mSolutionSet.getSolutions().get(l);
                scalarisedValue = mScalarisation.scalarise(solution.getValues(), weights);
                maxScalarisedValue = Math.max(maxScalarisedValue, scalarisedValue);
            }
            // sum up the maximal scalarised values (the value that this solution would get for the given weights)
            rewardSum += maxScalarisedValue;
        }
        // return the average reward that this solution set received across the performed tests
        return rewardSum / totalNumTests;
    }

    /**
     * Estimates the additive epsilon indicator, i.e. the smallest epsilon which has to be added to the solution set so
     * that it weakly dominates the reference set. (See E. Zitzler, L. Thiele, M. Laumanns, C.M. Fonesca, V. Grunert da
     * Fonseca: Performance Assessment of Multiobjective Optimizers: An Analysis and Review. IEEE Transactions on
     * Evolutionary Computation 7(2), 117-132 (2003))
     * 
     * @param referenceSet
     *            The true Pareto front or a good approximation to which the solution can be compared
     * @return The additive epsilon indicator
     */
    public double additiveEpsilonIndicator(final SolutionSet referenceSet) {
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
            for (int solIndex = 0; solIndex < mSolutionSet.getNumSolutions(); solIndex++) {
                sol = mSolutionSet.getSolutions().get(solIndex);
                // maxEpsilonPerSingleDim is the smallest epsilon for which the current solution sol weakly
                // epsilon-dominates the currect reference point ref
                double maxEpsilonPerSingleDim = Double.NEGATIVE_INFINITY;
                for (int dim = 0; dim < mNumObjectives; dim++) {
                    double distance = ref.getValues()[dim] - sol.getValues()[dim];
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
     * Estimates the multiplicative epsilon indicator, i.e. the smallest epsilon so that to the solution weakly
     * dominates the reference set if multiplied with the factor epsilon. (See E. Zitzler, L. Thiele, M. Laumanns, C.M.
     * Fonesca, V. Grunert da Fonseca: Performance Assessment of Multiobjective Optimizers: An Analysis and Review. IEEE
     * Transactions on Evolutionary Computation 7(2), 117-132 (2003))
     * 
     * @param referenceSet
     *            The true Pareto front or a good approximation to which the solution can be compared
     * @return The multiplicative epsilon indicator
     */
    public double multiplicativeEpsilonIndicator(final SolutionSet referenceSet) {
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
            for (int solIndex = 0; solIndex < mSolutionSet.getNumSolutions(); solIndex++) {
                sol = mSolutionSet.getSolutions().get(solIndex);
                // maxEpsilonPerSingleDim is the smallest epsilon for which the current solution sol weakly
                // epsilon-dominates the currect reference point ref
                double maxEpsilonPerSingleDim = Double.NEGATIVE_INFINITY;
                for (int dim = 0; dim < mNumObjectives; dim++) {
                    double distance = ref.getValues()[dim] / sol.getValues()[dim];
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
     * Overall Nondominated Vector Generation (ONVG) gives an indication of convergence. It is literally just the number
     * of solutions the algorithm found. It should neither be too high nor too low, and is only useful in combination
     * with other quality indicators, or in comparison with the ONVG of a reference set. (See J. R. Schott: Fault
     * tolerant design using single and multicriteria genetic algorithm optimization, M.S. thesis, Dept. Aeronautics
     * Astronautics, Massachusetts Instit. Technology, Cambridge, MA, USA, 1995)
     * 
     * @return The number of solutions the algorithm found
     */
    public int overallNondominatedVectorGeneration() {
        return mNumSolutions;
    }

    /**
     * This metric gives an indication to how uniformly the solution set is distributed. The lower this value, the
     * better. The runtime is quadratic in the number of objectives. (See J. R. Schott: Fault tolerant design using
     * single and multicriteria genetic algorithm optimization, M.S. thesis, Dept. Aeronautics Astronautics,
     * Massachusetts Instit. Technology, Cambridge, MA, USA, 1995)
     * 
     * @return The indicator of how evenly the solutions in the solution set are distributed. A lower value indicates a
     *         better(more uniform) distribution. Returns POSITIVE_INIFINITY if there exists no or only one solution.
     */
    public double schottSpacingMetric() {
        // estimate the minimal distances d_i for each solution i
        double[] minDistances = new double[mNumSolutions];
        int index = 0;
        for (int i = 0; i < mNumSolutions; i++) {
            double minDistance = Double.POSITIVE_INFINITY;
            for (int j = 0; j < mNumSolutions; j++) {
                if (i != j) {
                    double distance = 0;
                    for (int dim = 0; dim < mNumObjectives; dim++) {
                        // sum up the dimension-wise distances between two distinct solutions in the solution set
                        distance += Math.abs(mSolutionSet.getSolutions().get(i).getValues()[dim]
                                - mSolutionSet.getSolutions().get(j).getValues()[dim]);
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
        for (int l = 0; l < mNumSolutions; l++) {
            sum += Math.pow(averageMinDist - minDistances[l], 2);
        }
        // estimate the value of the schott metric (delta)
        double delta;
        if (mNumSolutions > 1) {
            delta = Math.sqrt(sum / (mNumSolutions - 1));
        } else {
            delta = Double.POSITIVE_INFINITY;
        }
        return delta;
    }

    /**
     * Estimates the maximum spread, which is an indicator for how well the solutions are spread. A higher value
     * indicates a better spread of solutions. (E. Zitzler, K. Deb, and L. Thiele: Comparison of multiobjective
     * evolutionary algorithms: Empirical results, Evol. Comput., vol. 8, no. 2, pp. 173-195, Jun. 2000)
     * 
     * @return The maximum spread. A higher value indicates a beter (larger) spread.
     */
    public double maximumSpread() {
        double sum = 0;
        for (int dim = 0; dim < mNumObjectives; dim++) {
            double maxVal = Double.NEGATIVE_INFINITY;
            double minVal = Double.POSITIVE_INFINITY;
            for (int s = 0; s < mNumSolutions; s++) {
                double solutionValue = mSolutionSet.getSolutions().get(s).getValues()[dim];
                maxVal = Math.max(solutionValue, maxVal);
                minVal = Math.min(solutionValue, minVal);
            }
            sum += Math.pow(maxVal - minVal, 2);
        }
        double maxSpread = Math.sqrt(sum);
        return maxSpread;
    }

    /**
     * Performs some tests on this class
     * 
     * @param args
     */
    public static void main(final String[] args) {

        // test set
        final SolutionSet testSolutionSet01 = new SolutionSet("(6,6),(5,5)");
        final SolutionSet testSolutionSet02 = new SolutionSet("(3,4),(10,10)");

        // create a scalarization function
        final LinearScalarisation linearScalarisation = new LinearScalarisation();
        final ProductScalarisation productScalarisation = new ProductScalarisation();

        // create judge with the solution set that is tested
        final Judge testJudge = new Judge(testSolutionSet01, linearScalarisation);

        // perform tests
        double avgRew = testJudge.averageReward();
        System.out.println("Average Reward: " + avgRew);

        double addEps = testJudge.additiveEpsilonIndicator(testSolutionSet02);
        System.out.println("Additive Epsilon Indicator : " + addEps);

        double multEps = testJudge.multiplicativeEpsilonIndicator(testSolutionSet02);
        System.out.println("Multiplicative Epsilon Indicator: " + multEps);

        int oNVG = testJudge.overallNondominatedVectorGeneration();
        System.out.println("ONVG: " + oNVG);

        double unif = testJudge.schottSpacingMetric();
        System.out.println("Uniformity measure: " + unif);

        double spread = testJudge.maximumSpread();
        System.out.println("Spread measure: " + spread);

    }
}
