package nl.uva.morlb.rg.experiment;

import java.util.Arrays;
import java.util.Random;

import nl.uva.morlb.rg.agent.momcts.MOMCTSAgent;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;
import nl.uva.morlb.rg.experiment.model.SolutionSet;
import nl.uva.morlb.util.Log;

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

        for (int test = 1; test <= 10; ++test) {
            Log.f("\n\n========== TEST " + test + " ==========\n\n");

            RLGlue.RL_init();

            final SolutionSet optimalSolution = OptimalSolutions.getSolution(sProblem.getParameters());

            String solutionSetString = "";
            int episode;
            for (episode = 0; episode < 1000000; ++episode) {
                RLGlue.RL_episode((int) -Judge.HYPERVOLUME_REFERENCE_POINT_TIME);

                solutionSetString = RLGlue.RL_agent_message("getSolutionSet");
                if (!solutionSetString.equals("")) {
                    String metrics = "";

                    final SolutionSet solutionSet = new SolutionSet(solutionSetString);

                    // Scalarisation must be created here for random seed purposes
                    final Scalarisation scalarisation = new LinearScalarisation(
                            sProblem.getParameters().numResourceTypes + 1);
                    // final Scalarisation scalarisation = new MinScalarisation();

                    // Calculate non-reference metrics
                    final double[] avgRew = Judge.averageReward(solutionSet, scalarisation);
                    final int oNVG = Judge.overallNondominatedVectorGeneration(solutionSet);
                    final double unif = Judge.schottSpacingMetric(solutionSet);
                    final double spread = Judge.maximumSpread(solutionSet);
                    final double hypervolume = Judge.hypervolume(solutionSet);
                    final double[] returnValues = RLGlue.RL_return().doubleArray;

                    metrics += avgRew[0] + " " + avgRew[1] + " " + oNVG + " " + unif + " " + spread + " " + hypervolume;

                    // Check if we can use reference set metrics
                    if (optimalSolution != null) {
                        final double addEpsilon = Judge.additiveEpsilonIndicator(solutionSet, optimalSolution);
                        final double multEpsilon = Judge.multiplicativeEpsilonIndicator(solutionSet, optimalSolution);

                        metrics += " " + addEpsilon + " " + multEpsilon;
                    }

                    Log.f(metrics + " " + Arrays.toString(returnValues));
                }

                // Print final results at the end of the test
                if (Boolean.parseBoolean(RLGlue.RL_agent_message("isConverged"))) {
                    break;
                }
            }

            Log.f("\n\n");
            Log.f("Number of episodes: " + episode);
            Log.f("Solution set: " + solutionSetString);
            if (optimalSolution != null) {
                Log.f("Optimal set: " + optimalSolution);
            }
            Log.f("\n\n");

            RLGlue.RL_cleanup();
            sProblem.shuffleResources(sRng);
        }

        System.exit(0);
    }

    public static void main(final String[] args) {
        if (args.length > 0) {
            sProblem = new ResourceGatheringEnv(Parameters.fromString(args, sRng));
        } else {
            sProblem = new ResourceGatheringEnv();
        }

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
                new AgentLoader(new MOMCTSAgent()).run();
            }
        }).start();
    }

}
