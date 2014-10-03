package nl.uva.morlb;

import java.util.Random;

import nl.uva.morlb.rg.agent.momcts.MOMCTSAgent;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.SdpCollection;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.experiment.Judge;
import nl.uva.morlb.rg.experiment.model.LinearScalarisation;
import nl.uva.morlb.rg.experiment.model.Scalarisation;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

/**
 * Don tuch tha stuff ya !
 * @author philipp
 *
 */
public class PhilippsGlueWrapper {

    private static final int EPISODE_COUNT = 1000000;
    private static final int STEP_COUNT = 100;
    private final static Random sRng = new Random(62434);

    public static void main(final String[] args) {

        // Prepare the environment and agent
        final Parameters parameters = SdpCollection.getLargeProblem();//Parameters.fromString(args, sRng);
        ResourceGatheringEnv environment = new ResourceGatheringEnv(parameters);
        final AgentInterface agent = new MOMCTSAgent();

        for (int test = 0; test < 1; ++test) {
            System.out.println("\n\n========== TEST " + test + " ==========\n\n");

            agent.agent_init(environment.env_init());
            for (int episodeCounter = 0; episodeCounter < EPISODE_COUNT; ++episodeCounter) {
                environment = new ResourceGatheringEnv(parameters);

                // Start the episode until a terminal state is reached
                Action performedAction = agent.agent_start(environment.env_start());
                Reward_observation_terminal currentStep = environment.env_step(performedAction);

                if(episodeCounter % (EPISODE_COUNT/100) == 0) {
                    System.out.println(episodeCounter / (EPISODE_COUNT/100));
                }

                int stepCounter = 0;
                while (!currentStep.isTerminal() && stepCounter++ < STEP_COUNT) {
                    performedAction = agent.agent_step(currentStep.r, currentStep.o);
                    currentStep = environment.env_step(performedAction);
                }

                if(episodeCounter % (EPISODE_COUNT/100) == 0) {
                    System.out.println("Step counter: " +stepCounter);
                }

                agent.agent_end(currentStep.r);

                if (Boolean.parseBoolean(agent.agent_message("isConverged"))) {
                    System.out.println("\n\nNumber of episodes: " + (stepCounter) + "\n\n");
                    break;
                }
            }

            final String solutionSetString = agent.agent_message("getSolutionSet");
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

            agent.agent_cleanup();
            environment.shuffleResources(sRng);
        }
    }
}
