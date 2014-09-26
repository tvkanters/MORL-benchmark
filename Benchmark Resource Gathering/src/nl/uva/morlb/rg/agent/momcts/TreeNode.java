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

}
