package nl.uva.morlb.rg.agent;

import nl.uva.morlb.rg.environment.model.DiscreteAction;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

public class ConvexHullQLearning implements AgentInterface {

	@Override
	public void agent_init(final String taskSpec) {
		TaskSpec tSpec = new TaskSpec(taskSpec);
		System.out.println(taskSpec);
	}

	boolean odd = true;

	@Override
	public Action agent_start(final Observation observation) {
		Action action = new Action(1, 0);

		action.intArray[0] = DiscreteAction.RIGHT.ordinal();

		return action;
	}

	@Override
	public Action agent_step(final Reward reward, final Observation observation) {
		Action action = new Action(1, 0);

		if(odd) {
			action.intArray[0] = DiscreteAction.RIGHT.ordinal();
		} else {
			action.intArray[0] = DiscreteAction.UP.ordinal();
		}
		odd = !odd;

		printReward(reward);

		return action;
	}

	@Override
	public void agent_end(final Reward reward) {
		// TODO Auto-generated method stub

	}

	@Override
	public void agent_cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public String agent_message(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void printReward(final Reward reward) {
		for(double d : reward.doubleArray) {
			System.out.print( d + " ");
		}
		System.out.println();

	}
}
