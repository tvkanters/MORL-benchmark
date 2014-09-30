package nl.uva.morlb.rg.agent.momcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nl.uva.morlb.rg.agent.model.BenchmarkReward;
import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.environment.model.DiscreteAction;

public class TreeNode {

    /** The state representing the current node **/
    private final State mState;

    /** The reward given this state and an action **/
    private final HashMap<DiscreteAction, BenchmarkReward> mActionReward = new HashMap<>();

    /** The number of times a given action was taken in this state **/
    private final HashMap<DiscreteAction, Integer> mActionCounter = new HashMap<>();

    /** The resulting tree nodes given this state and an action **/
    private final HashMap<DiscreteAction, TreeNode> mChildrens = new HashMap<>();

    /** The last used action in this node, to recreate the treewalk **/
    private DiscreteAction mLastUsedAction = null;

    /** The visitation count n_s of this tree node **/
    private int mVisitationCout = 0;

    public TreeNode(final State state) {
        mState = state;
    }

    /**
     * Add a child node
     *
     * @param action The action resulting in that child
     * @param treeNode The node resulting from that action
     */
    public void addChild(final DiscreteAction action, final TreeNode treeNode) {
        mChildrens.put(action, treeNode);
    }

    /**
     * Checks if this TreeNode has the same contents as the given one.
     *
     * @param other
     *            The TreeNode to compare
     *
     * @return True iff the contents are the same
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TreeNode)) {
            return false;
        }

        final TreeNode treeNode = (TreeNode) other;
        return mState.equals(treeNode.mState);
    }

    /**
     * Hashes the TreeNode based on the contents.
     *
     * @return The hash code for the TreeNode
     */
    @Override
    public int hashCode() {
        return mState.hashCode();
    }

    /**
     * Evaluates if this node is a leaf
     *
     * @return True if it is a leaf, false if not
     */
    public boolean isLeaf() {
        return mChildrens.isEmpty();
    }

    /**
     * Get the resulting tree node for the specific action
     * @param action The action to take in this node
     * @return The resulting tree node
     */
    public TreeNode getNextNodeForAction(final DiscreteAction action) {
        return mChildrens.get(action);
    }

    /**
     * Generate a list of all already performed actions on this node
     * @return A list of all already performed actions on this node
     */
    public List<DiscreteAction> getListOfPerformedActions() {
        return new ArrayList<DiscreteAction>(mChildrens.keySet());
    }

    /**
     * Increases the visitation counter by 1
     */
    public void increaseVisitationCount() {
        mVisitationCout++;
    }

    /**
     * Get the visitation count of this tree node
     * @return The visitation count of this tree node
     */
    public int getVisitationCount() {
        return mVisitationCout;
    }

    /**
     * Get the amount of children under this node
     * @return The amount of children of this node
     */
    public double getAmountOfChildren() {
        return mChildrens.size();
    }

    @Override
    public String toString() {
        String result = mState.toString() + " Children: " +mChildrens.size();
        for(DiscreteAction childAction : mChildrens.keySet()) {
            result += " " +childAction.name();
        }

        return result;
    }

    /**
     * Increases the action counter for the specific action by 1 and sets it as last used action
     * @param action The given action to increase the counter for
     */
    public void increaseActionCounterFor(final DiscreteAction action) {
        Integer actionCounter = mActionCounter.get(action);
        if(actionCounter == null) {
            actionCounter = 0;
        }

        mActionCounter.put(action, ++actionCounter);
        mLastUsedAction = action;
    }

    /**
     * Get the last used action in this tree node
     * @return The last used action or null if there wheren't any
     */
    public DiscreteAction getLastUsedAction() {
        return mLastUsedAction;
    }

    /**
     * Evaluate if this node has a last used action
     * @return True if this node has a last used action
     */
    public boolean hasLastUsedAction() {
        return mLastUsedAction != null;
    }

    /**
     * Get the reward for a given action
     * @param takenAction The action taken
     * @return The reward for the given action
     */
    public BenchmarkReward getRewardForAction(final DiscreteAction takenAction) {
        if(!mActionReward.containsKey(takenAction)) {
            return new BenchmarkReward(Arrays.copyOf(MOMCTSAgent.sInitialReward, MOMCTSAgent.sInitialReward.length));
        } else {
            return mActionReward.get(mLastUsedAction);
        }
    }

    /**
     * Get the number of times the given action was taken in this node
     * @param takenAction The taken action
     * @return The number of times this action was taken
     */
    public int getNumOfTimesActionWasTaken(final DiscreteAction takenAction) {
        if(!mActionCounter.containsKey(takenAction)) {
            return 0;
        }

        return mActionCounter.get(takenAction);
    }

    /**
     * Set the reward for taking a specific action in this state
     * @param action The taken action
     * @param newReward The resulting reward
     */
    public void setRewardForAction(final DiscreteAction action, final BenchmarkReward reward) {
        mActionReward.put(action, reward);
    }
}
