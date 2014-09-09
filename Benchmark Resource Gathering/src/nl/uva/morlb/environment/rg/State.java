package nl.uva.morlb.environment.rg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class State {

    private Location mGoal;
    private Location mAgent;
    private final Map<Location, Resource> mResources = new HashMap<>();
    private final List<Resource> mInventory = new LinkedList<>();

    public State() {

    }

}
