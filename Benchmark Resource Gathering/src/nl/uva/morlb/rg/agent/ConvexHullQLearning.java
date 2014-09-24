package nl.uva.morlb.rg.agent;

import java.util.HashMap;

import nl.uva.morlb.rg.agent.model.QTableEntry;
import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.util.Util;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

public class ConvexHullQLearning implements AgentInterface {

    private final double INITIAL_Q_VALUE = 99.99d;

    private static final double GAMMA = 0.9d;

    private final HashMap<QTableEntry, Double[]> mQTable = new HashMap<>();

    private QTableEntry mLastEntry;
    private boolean[] inventory;

    private static boolean[][] inventoryHack =
        {
        { true, true},
        { true, false},
        { false, true},
        { false, false}
        };

    @Override
    public void agent_init(final String taskSpec) {
        final TaskSpec tSpec = new TaskSpec(taskSpec);

        inventory = new boolean[2];
        for(int i = 0; i < inventory.length; ++i) {
            inventory[i] = false;
        }

        int obsDim = 2;
        int actionDim = tSpec.getDiscreteActionRange(0).getMax() +1;
        int maxX = 4;
        int maxY = 4;

        for(int x = 0; x < maxX; ++x) {
            for(int y = 0; y < maxY; ++y) {

                Location location = new Location(x, y);

                for(boolean[] inventory : inventoryHack) {

                    State state = new State(location, inventory);
                    for(int actionCounter = 0; actionCounter < actionDim; ++actionCounter) {
                        mQTable.put(new QTableEntry(state, DiscreteAction.values()[actionCounter]), new Double[]{INITIAL_Q_VALUE,INITIAL_Q_VALUE,INITIAL_Q_VALUE});
                    }
                }
            }
        }
    }

    boolean odd = true;

    @Override
    public Action agent_start(final Observation observation) {
        State state = generateState(observation);
        DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        for(int i = 1; i < reward.doubleArray.length; ++i) {
            if(reward.doubleArray[i] != 0) {
                inventory[i-1] = true;
            }
        }
        State state = generateState(observation);
        System.out.println(state.hashCode());


        double[] adjacentQValues = new double[3];
        for(int i = 0; i < 5; ++i) {
            QTableEntry g = new QTableEntry(state, DiscreteAction.values()[i]);
            Double[] currentValue = mQTable.get(g);
            for(int j = 0; j < currentValue.length; ++j) {
                adjacentQValues[j] += currentValue[j] / currentValue.length;
            }
        }

        Double[] qValues = mQTable.get(mLastEntry);
        for(int i = 0; i < reward.doubleArray.length; ++i) {
            qValues[i] += reward.doubleArray[i] + GAMMA * (adjacentQValues[i]);
        }

        DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    @Override
    public void agent_end(final Reward reward) {

        for(int y = 3; y >= 0; --y) {
            for(int x = 0; x < 4; ++x) {

                Location location = new Location(x, y);

                State state = new State(location, inventoryHack[3]);
                System.out.print(mQTable.get(new QTableEntry(state, DiscreteAction.values()[2]))[0] +" ");

            }
            System.out.println();
        }

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

    public State generateState(final Observation observation) {
        double[] observationArray = observation.doubleArray;

        Location currentLocation = new Location(observationArray[0], observationArray[1]);
        return new State(currentLocation, inventory);
    }

    public DiscreteAction getRandomAction() {
        return DiscreteAction.values()[Util.RNG.nextInt(5)];
    }

    public void printReward(final Reward reward) {
        for (final double d : reward.doubleArray) {
            System.out.print(d + " ");
        }
        System.out.println();

    }
}
