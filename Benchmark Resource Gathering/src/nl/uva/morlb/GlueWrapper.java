package nl.uva.morlb;

import java.util.LinkedList;
import java.util.List;

import nl.uva.morlb.rg.agent.momcts.MOMCTSAgent;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.SdpCollection;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

/**
 * A Glue wrapper that negates the need of RL Glue for testing.
 */
public class GlueWrapper {

    public static void main(final String[] args) {
        // Prepare the environment and agent

        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2));
        resources.add(new Resource(1, 2, 1));
        resources.add(new Resource(1, 2, 2));

        Parameters parameters = SdpCollection.getSimpleProblem();
        ResourceGatheringEnv environment = new ResourceGatheringEnv(parameters);
        final AgentInterface agent = new MOMCTSAgent();
        //        final AgentInterface agent = new DumbAgent();

        agent.agent_init(environment.env_init());

        for (int episodeCounter = 0; episodeCounter < 10000; ++episodeCounter) {
            environment = new ResourceGatheringEnv(parameters);

            // Start the episode until a terminal state is reached
            Action performedAction = agent.agent_start(environment.env_start());
            Reward_observation_terminal currentStep = environment.env_step(performedAction);

            while (!currentStep.isTerminal()) {
                performedAction = agent.agent_step(currentStep.r, currentStep.o);
                currentStep = environment.env_step(performedAction);
            }

            agent.agent_end(currentStep.r);
        }

        agent.agent_cleanup();
    }

}
