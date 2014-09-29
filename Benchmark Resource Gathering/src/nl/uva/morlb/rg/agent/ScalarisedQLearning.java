package nl.uva.morlb.rg.agent;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import nl.uva.morlb.rg.agent.model.QTableEntry;
import nl.uva.morlb.rg.agent.model.State;
import nl.uva.morlb.rg.agent.model.StateValue;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.util.Util;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

public class ScalarisedQLearning implements AgentInterface {

    private final double INITIAL_Q_VALUE = -9.0d;
    private final double INITIAL_V_VALUE = INITIAL_Q_VALUE ;

    private static final double GAMMA = 0.9d;
    private static final double ALPHA = 0.5d;

    private final HashMap<QTableEntry, StateValue> mQTable = new HashMap<>();
    private final HashMap<State, StateValue> mVTable = new HashMap<>();

    private QTableEntry mLastEntry;
    private int[] inventory;

    private TaskSpecVRLGLUE3 mTaskSpec;

    private final double[] SCALAR = new double[] {0.01,0.4,1};

    private static int[][] inventoryHack =
        {
        { 1, 1},
        { 1, 0},
        { 0, 1},
        { 0, 0}
        };

    @Override
    public void agent_init(final String taskSpec) {
        mTaskSpec = new TaskSpecVRLGLUE3(taskSpec);

        inventory = new int[mTaskSpec.getNumOfObjectives() -1];
        for(int i = 0; i < inventory.length; ++i) {
            inventory[i] = 0;
        }

        int actionDim = mTaskSpec.getDiscreteActionRange(0).getMax() +1;
        int maxX = 4;
        int maxY = 4;

        for(int x = 0; x < maxX; ++x) {
            for(int y = 0; y < maxY; ++y) {

                Location location = new Location(x, y);

                for(int[] inventory : inventoryHack) {

                    State state = new State(location, inventory);
                    mVTable.put(state, new StateValue(new double[]{INITIAL_V_VALUE ,INITIAL_V_VALUE ,INITIAL_V_VALUE }));
                    for(int actionCounter = 0; actionCounter < actionDim; ++actionCounter) {
                        mQTable.put(new QTableEntry(state, DiscreteAction.values()[actionCounter]), new StateValue( new double[]{INITIAL_Q_VALUE,INITIAL_Q_VALUE,INITIAL_Q_VALUE}));
                    }
                }
            }
        }
    }

    @Override
    public Action agent_start(final Observation observation) {
        for(int i = 0; i < inventory.length; ++i) {
            inventory[i] = 0;
        }

        State state = generateState(observation);
        DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        //Calculate the current inventory
        for(int i = 1; i < reward.doubleArray.length; ++i) {
            if(reward.doubleArray[i] != 0) {
                inventory[i-1]++;
            }
        }
        State state = generateState(observation);
        StateValue convertedReward = new StateValue(reward.doubleArray);

        //Q-Table step
        double bestActionValue = Double.NEGATIVE_INFINITY;
        StateValue bestActionReward = null;
        for(int i = 0; i < 5; ++i) {
            DiscreteAction currentAction = DiscreteAction.values()[i];

            QTableEntry g = new QTableEntry(state, currentAction);
            StateValue qTableReward = mQTable.get(g);
            double currentValue = qTableReward.scalarise(SCALAR).getSum();

            if(currentValue >= bestActionValue) {
                bestActionValue = currentValue;
                bestActionReward = qTableReward;
            }
        }

        StateValue lastStateValue = mQTable.get(mLastEntry);
        lastStateValue = lastStateValue.add(convertedReward.add(bestActionReward, GAMMA).sub(lastStateValue), ALPHA);
        mQTable.put(mLastEntry, lastStateValue);


        //Define the next action
        DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    @Override
    public void agent_end(final Reward reward) {
        StateValue convertedReward = new StateValue(reward.doubleArray);
        StateValue nextReward = new StateValue(new double[reward.doubleArray.length]);

        StateValue lastStateValue = mQTable.get(mLastEntry);
        lastStateValue = lastStateValue.add(convertedReward.add(nextReward, GAMMA).sub(lastStateValue), ALPHA);
        mQTable.put(mLastEntry, lastStateValue);

    }

    @Override
    public void agent_cleanup() {
        DecimalFormat twoDForm = new DecimalFormat("####.##");
        int[] desiredInventory = inventoryHack[1];

        for(int action = 0; action < 5; ++action) {
            System.out.println(DiscreteAction.values()[action].name());

            for(int y = 3; y >= 0; --y) {
                for(int x = 0; x < 4; ++x) {

                    Location location = new Location(x, y);

                    State state = new State(location, desiredInventory);
                    System.out.print("\t"+twoDForm.format(mQTable.get(new QTableEntry(state, DiscreteAction.values()[action])).scalarise(SCALAR).getSum()) +"\t");

                }
                System.out.println();
            }
            System.out.println();
        }

        //        for(int action = 0; action < 5; ++action) {
        //            System.out.println(DiscreteAction.values()[action].name());

        for(int y = 3; y >= 0; --y) {
            for(int x = 0; x < 4; ++x) {

                Location location = new Location(x, y);
                State state = new State(location, desiredInventory);

                double bestActionValue = Double.NEGATIVE_INFINITY;
                DiscreteAction bestAction = null;
                for(int i = 0; i < 5; ++i) {
                    DiscreteAction currentAction = DiscreteAction.values()[i];
                    double currentActionValue = mQTable.get(new QTableEntry(state, currentAction)).scalarise(SCALAR).getSum();

                    if(currentActionValue > bestActionValue) {
                        bestActionValue = currentActionValue;
                        bestAction = currentAction;
                    }
                }

                System.out.print("\t"+bestAction.name() +"\t");

            }
            System.out.println();
        }
        System.out.println();
        //        }
    }

    @Override
    public String agent_message(final String arg0) {
        return null;
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
        return new State(currentLocation, Arrays.copyOf(inventory, inventory.length));
    }

    /**
     * The next action form our random policy
     * @return The next action
     */
    public DiscreteAction getRandomAction() {
        return DiscreteAction.values()[Util.RNG.nextInt(5)];
    }

    /**
     * Prints the reward
     * @param reward The reward to be printed
     */
    public void printReward(final Reward reward) {
        for (final double d : reward.doubleArray) {
            System.out.print(d + " ");
        }
        System.out.println();
    }
}
