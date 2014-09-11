package nl.uva.morlb.rg.agent;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

public class ConvexHullQLearning implements AgentInterface {

	@Override
	public void agent_init(String taskSpec) {
		TaskSpec tSpec = new TaskSpec(taskSpec);

	}

	@Override
	public Action agent_start(Observation observation) {
		
		return null;
	}

	@Override
	public Action agent_step(Reward reward, Observation observation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void agent_end(Reward reward) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void agent_cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public String agent_message(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
