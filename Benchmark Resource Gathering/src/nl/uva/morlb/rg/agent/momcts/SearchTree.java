package nl.uva.morlb.rg.agent.momcts;

import java.util.HashMap;
import java.util.List;

import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.environment.model.DiscreteAction;

public class SearchTree {

    private boolean mInitialised = false;

    /** The root node of the tree **/
    private TreeNode mRootNode;

    /** The current evaluated node **/
    private TreeNode mCurrentNode;

    /** The action which got chosen in the tree building phase **/
    private DiscreteAction mActionForTreeBuilding;

    /** A map for quick lookup if states where already visited **/
    private final HashMap<State, TreeNode> mEntries = new HashMap<>();

    /**
     * Initialise the search tree with the root node
     *
     * @param initialState The root node
     */
    public void initialise(final State initialState) {
        mRootNode = mCurrentNode = new TreeNode(initialState);
        mEntries.put(initialState, mRootNode);
        mInitialised = true;
    }

    /**
     * Resets the search tree to start from the root node again
     */
    public void reset() {
        mCurrentNode = mRootNode;
    }

    /**
     * Was the search tree already initialised
     * @return True if already initialised, false if not
     */
    public boolean isInitialised() {
        return mInitialised;
    }

    /**
     * Determines if the current node is a leaf node
     * @return True if the current node is a leaf node
     */
    public boolean isLeafNode() {
        return mCurrentNode.isLeaf();
    }

    /**
     * Save the action used for tree building
     * @param treeBuildingAction The tree building action
     */
    public void saveTreeBuildingAction(final DiscreteAction treeBuildingAction) {
        mActionForTreeBuilding = treeBuildingAction;
    }

    /**
     * Completes the tree building step in appending the state and action to the current tree node and updating the current node to the new one
     * @param currentState The resulting state from the tree building step
     */
    public void completeTreeBuilding(final State currentState) {
        TreeNode treeNode = mEntries.get(currentState);

        if(treeNode != null) {
            //we visited that state already
            mCurrentNode.addChild(mActionForTreeBuilding, treeNode);
        } else {
            treeNode = new TreeNode(currentState);
            mEntries.put(currentState, treeNode);

            mCurrentNode.addChild(mActionForTreeBuilding, treeNode);
        }

        performActionOnCurrentNode(mActionForTreeBuilding);

        mActionForTreeBuilding = null;
    }

    /**
     * Performs the given action on the current tree node and changes the current node to the resulting one.
     * @param action The action to perform on the current node
     */
    public void performActionOnCurrentNode(final DiscreteAction action) {
        mCurrentNode.increaseVisitationCount();
        mCurrentNode = mCurrentNode.getNextNodeForAction(action);
    }

    public String info() {
        String result = "Entries: " +mEntries.size();
        for(TreeNode t : mEntries.values()) {
            System.out.println(t);
        }

        return result;
    }

    /**
     * Get all already performed actions on the current node
     * @return A list of all already performed actions on the current node
     */
    public List<DiscreteAction> getPerformedActionsForCurrentNode() {
        return mCurrentNode.getListOfPerformedActions();
    }

    /**
     * Get the current active node
     * @return The current active node
     */
    public TreeNode getCurrentNode() {
        return mCurrentNode;
    }

    /**
     * Get the root node
     * @return The root node
     */
    public TreeNode getRootNode() {
        return mRootNode;
    }

    /**
     * Get the tree node for a given state
     * @param state The given state
     * @return The tree node for a given state
     */
    public TreeNode getNodeForState(final State state) {
        return mEntries.get(state);
    }
}
