package nl.uva.morlb.rg.agent.momcts;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

/**
 * Multi-Objective Monte-Carlo Tree Search
 */
public class MOMCTSAgent implements AgentInterface {

    private final int NUMBER_OF_TREE_WALKS = 100;

    private final SearchTree mSearchTree = new SearchTree();

    private TaskSpecVRLGLUE3 mTaskSpec;

    @Override
    public void agent_init(final String taskSpec) {
        mTaskSpec = new TaskSpecVRLGLUE3(taskSpec);

    }

    @Override
    public Action agent_start(final Observation arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Action agent_step(final Reward arg0, final Observation arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void agent_end(final Reward arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public String agent_message(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void agent_cleanup() {
        // TODO Auto-generated method stub

    }

}
