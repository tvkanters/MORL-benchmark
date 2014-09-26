package nl.uva.morlb.rg.agent.momcts;

import java.util.HashMap;

import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.agent.model.StateValue;
import nl.uva.morlb.rg.environment.model.DiscreteAction;

public class TreeNode {

    private final State mState;

    private final HashMap<DiscreteAction, StateValue> mActionReward = new HashMap<>();

    private final HashMap<DiscreteAction, TreeNode> mChildrens = new HashMap<>();

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

    public int getVisitationCount() {
        return mVisitationCout;
    }

    public void visited() {
        mVisitationCout++;
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

}
