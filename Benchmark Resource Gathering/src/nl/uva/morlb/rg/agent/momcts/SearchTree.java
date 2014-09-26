package nl.uva.morlb.rg.agent.momcts;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.uva.morlb.rg.agent.model.State;

public class SearchTree {

    private boolean mInitialised = false;

    private final int mLevelCounter = 1;

    private final List<Set<State>> mTree = new LinkedList<Set<State>>();

    /**
     * Initialise the search tree with the root node
     * 
     * @param initialState The root node
     */
    public void initialise(final State initialState) {
        Set<State> initialSet = new HashSet<>();
        initialSet.add(initialState);
        mTree.add(initialSet);

        mInitialised = true;
    }



    /**
     * Was the search tree already initialised
     * @return True if already initialised, false if not
     */
    public boolean isInitialised() {
        return mInitialised;
    }



}
