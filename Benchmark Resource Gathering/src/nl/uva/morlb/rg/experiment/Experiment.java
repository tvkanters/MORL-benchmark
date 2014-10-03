package nl.uva.morlb.rg.experiment;

import java.util.Arrays;
import java.util.Random;

import nl.uva.morlb.rg.agent.convexhull.ConvexHullQLearning;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.util.AgentLoader;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

/**
 * The experiment that sets the benchmark in motions.
 */
public class Experiment {

    /** The resource gathering problem */
    private static ResourceGatheringEnv sProblem;
    /** The seeded random number generator */
    private final static Random sRng = new Random(62434);

    /**
     * Runs the full experiment.
     */
    public void runExperiment() {

        for (int test = 0; test < 10; ++test) {
            System.out.println("\n\n========== TEST " + test + " ==========\n\n");

            RLGlue.RL_init();

            for (int episode = 0; episode < 10000; ++episode) {
                RLGlue.RL_episode((int) Judge.HYPERVOLUME_REFERENCE_POINT_TIME);

                final String solutionSetString = RLGlue.RL_agent_message("getSolutionSet");
                if (!solutionSetString.equals("")) {
                    final SolutionSet solutionSet = new SolutionSet(solutionSetString);
                    final Scalarisation scalarisation = new LinearScalarisation();

                    // Calculate non-reference metrics
                    final double[] avgRew = Judge.averageReward(solutionSet, scalarisation);
                    final int oNVG = Judge.overallNondominatedVectorGeneration(solutionSet);
                    final double unif = Judge.schottSpacingMetric(solutionSet);
                    final double spread = Judge.maximumSpread(solutionSet);
                    final double hypervolume = Judge.hypervolume(solutionSet);
                    final double[] returnValues = RLGlue.RL_return().doubleArray;

                    System.out.print(avgRew[0] + " " + avgRew[1] + " " + oNVG + " " + unif + " " + spread + " "
                            + hypervolume);

                    // Check if we can use reference set metrics
                    final SolutionSet optimalSolution = OptimalSolutions.getSolution(sProblem.getParameters());
                    if (optimalSolution != null) {
                        final double addEpsilon = Judge.additiveEpsilonIndicator(solutionSet, optimalSolution);
                        final double multEpsilon = Judge.multiplicativeEpsilonIndicator(solutionSet, optimalSolution);

                        System.out.print(" " + addEpsilon + " " + multEpsilon);
                    }

                    System.out.println(" " + Arrays.toString(returnValues));
                }

                if (Boolean.parseBoolean(RLGlue.RL_agent_message("isConverged"))) {
                    System.out.println("\n\nNumber of episodes: " + (episode + 1));
                    System.out.println("Solution set: " + solutionSetString + "\n\n");
                    break;
                }
            }

            RLGlue.RL_cleanup();
            sProblem.shuffleResources(sRng);
        }

        System.exit(0);
    }

    public static void main(final String[] args) {
        // if (args.length > 0) {
        // sProblem = new ResourceGatheringEnv(Parameters.fromString(args, sRng));
        // } else {
        sProblem = new ResourceGatheringEnv();
        // }

        // Start the experiment
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Experiment().runExperiment();
            }
        }).start();

        // Start the environment
        new Thread(new Runnable() {
            @Override
            public void run() {
                new EnvironmentLoader(sProblem).run();
            }
        }).start();

        // Start the agent
        new Thread(new Runnable() {
            @Override
            public void run() {
                new AgentLoader(new ConvexHullQLearning()).run();
            }
        }).start();
    }

}
