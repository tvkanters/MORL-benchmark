package nl.uva.morlb;

import nl.uva.morlb.rg.agent.ConvexHullQLearning;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.SdpCollection;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

public class GlueWrapper {

	public static void main(final String[] args) {

		ResourceGatheringEnv environment = new ResourceGatheringEnv(SdpCollection.getSimpleProblem());
		ConvexHullQLearning agent = new ConvexHullQLearning();

		agent.agent_init(environment.env_init());
		Action performedAction = agent.agent_start(environment.env_start());
		Reward_observation_terminal currentStep =  environment.env_step(performedAction);

		while(!currentStep.isTerminal()) {

			performedAction = agent.agent_step(currentStep.r, currentStep.o);
			currentStep = environment.env_step(performedAction);
		}

		agent.agent_end(currentStep.r);

	}
}
