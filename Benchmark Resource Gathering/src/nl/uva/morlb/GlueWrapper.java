package nl.uva.morlb;

import nl.uva.morlb.rg.agent.momcts.MOMCTSAgent;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.SdpCollection;
import nl.uva.morlb.rg.environment.model.Parameters;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

/**
 * A Glue wrapper that negates the need of RL Glue for testing.
 */
public class GlueWrapper {

    /** The total amount of episodes this test is going to run **/
    private static final int TOTAL_AMOUNT_EPISODES = 100000;

    /** The maximum amount of steps allowed in each episode **/
    private static final int MAX_STEPS_PER_EPISODE = 1000;

    public static void main(final String[] args) {
        // Prepare the environment and agent
        final Parameters parameters = SdpCollection.getLargeProblem();
        ResourceGatheringEnv environment = new ResourceGatheringEnv(parameters);
        final AgentInterface agent = new MOMCTSAgent();

        agent.agent_init(environment.env_init());

        for (int episodeCounter = 0; episodeCounter < TOTAL_AMOUNT_EPISODES; ++episodeCounter) {
            environment = new ResourceGatheringEnv(parameters);

            // Start the episode until a terminal state is reached
            Action performedAction = agent.agent_start(environment.env_start());
            Reward_observation_terminal currentStep = environment.env_step(performedAction);

            int stepCounter = 0;
            while (!currentStep.isTerminal() && stepCounter++ < MAX_STEPS_PER_EPISODE) {
                performedAction = agent.agent_step(currentStep.r, currentStep.o);
                currentStep = environment.env_step(performedAction);
            }

            agent.agent_end(currentStep.r);
        }

        agent.agent_cleanup();
    }

}
