package nl.uva.morlb.rg.experiment;

import java.util.Arrays;
import java.util.Random;

import nl.uva.morlb.rg.agent.DumbAgent;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.model.Parameters;

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
            RLGlue.RL_init();

            for (int episode = 0; episode < 20; ++episode) {
                runEpisode(100);
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

        System.out.println("Episode " + mEpisodeCount);
        System.out.println("    Reward: " + Arrays.toString(totalReward.doubleArray));

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
                new AgentLoader(new DumbAgent()).run();
            }
        }).start();
    }

}
