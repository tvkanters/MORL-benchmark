package nl.uva.morlb.rg.experiment;

import java.util.Arrays;

import nl.uva.morlb.rg.environment.ResourceGatheringEnv;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.types.Reward;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

/**
 * The experiment that sets the benchmark in motions.
 */
public class Experiment {

    /** The amount of episodes ran */
    private int mEpisodeCount = 0;

    /**
     * Runs the full experiment.
     */
    public void runExperiment() {
        final String taskSpec = RLGlue.RL_init();

        runEpisode(100);
        runEpisode(100);
        runEpisode(100);
        runEpisode(100);
        runEpisode(100);

        RLGlue.RL_cleanup();
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
                new EnvironmentLoader(new ResourceGatheringEnv()).run();
            }
        }).start();
    }

}
