package nl.uva.morlb.rg.experiment;

import java.util.Random;

import nl.uva.morlb.rg.agent.momcts.MOMCTSAgent;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.types.Reward;
import org.rlcommunity.rlglue.codec.util.AgentLoader;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

/**
 * The experiment that sets the benchmark in motions.
 */
public class Experiment {

    /** The amount of episodes ran */
    private int mEpisodeCount = 0;
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
                runEpisode(100);

                final String solutionSetString = RLGlue.RL_agent_message("getSolutionSet");
                if (!solutionSetString.equals("")) {
                    final SolutionSet solutionSet = new SolutionSet(solutionSetString);
                    final Scalarisation scalarisation = new LinearScalarisation();

                    final double[] avgRew = Judge.averageReward(solutionSet, scalarisation);
                    final int oNVG = Judge.overallNondominatedVectorGeneration(solutionSet);
                    final double unif = Judge.schottSpacingMetric(solutionSet);
                    final double spread = Judge.maximumSpread(solutionSet);
                    final double hypervolume = Judge.hypervolume(solutionSet);
                    System.out.println(avgRew[0] + " " + avgRew[1] + " " + oNVG + " " + unif + " " + spread + " "
                            + hypervolume);
                }

                if (Boolean.parseBoolean(RLGlue.RL_agent_message("isConverged"))) {
                    break;
                }
            }

            RLGlue.RL_cleanup();
            sProblem.shuffleResources(sRng);
        }

        System.exit(0);
    }

    /**
     * Runs an episode of resource gathering.
     *
     * @param stepLimit
     *            The amount steps before terminating
     */
    private void runEpisode(final int stepLimit) {
        final int terminal = RLGlue.RL_episode(stepLimit);

        final int totalSteps = RLGlue.RL_num_steps();
        final Reward totalReward = RLGlue.RL_return();

        // System.out.println("Episode " + mEpisodeCount);
        // System.out.println("    Reward: " + Arrays.toString(totalReward.doubleArray));

        ++mEpisodeCount;
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
