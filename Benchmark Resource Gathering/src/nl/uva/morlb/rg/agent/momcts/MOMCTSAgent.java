package nl.uva.morlb.rg.agent.momcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.util.Util;

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

    /*
     * State specific values
     */
    /** The current inventory **/
    private boolean[] mInventory;

    @Override
    public void agent_init(final String taskSpec) {
        mTaskSpec = new TaskSpecVRLGLUE3(taskSpec);

        List<DiscreteAction> availableActions = new ArrayList<>();
        for(int action = mTaskSpec.getDiscreteActionRange(0).getMin(); action <= mTaskSpec.getDiscreteActionRange(0).getMax(); action++) {
            availableActions.add(DiscreteAction.values()[action]);
        }
        mInventory = new boolean[mTaskSpec.getNumOfObjectives() -1];
    }

    @Override
    public Action agent_start(final Observation observation) {
        resetInventory();

        if(!mSearchTree.isInitialised()) {
            State currentState = generateState(observation);
            mSearchTree.initialise(currentState);
        }


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


    /**
     * Generate the current state from the observation and the current inventory
     * 
     * @param observation The current observation
     * @return The current state
     */
    public State generateState(final Observation observation) {
        double[] observationArray = observation.doubleArray;

        Location currentLocation = new Location(observationArray[0], observationArray[1]);
        return new State(currentLocation, Arrays.copyOf(mInventory, mInventory.length));
    }

    /**
     * The next action form our random policy
     * @return The next action
     */
    public DiscreteAction getRandomAction() {
        return DiscreteAction.values()[Util.RNG.nextInt(5)];
    }

    /**
     * Resets the inventory to empty
     */
    private void resetInventory() {
        for(int i = 0; i < mInventory.length; ++i) {
            mInventory[i] = false;
        }
    }

}
