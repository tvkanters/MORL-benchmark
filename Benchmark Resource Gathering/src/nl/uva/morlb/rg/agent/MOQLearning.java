package nl.uva.morlb.rg.agent;

import java.util.Arrays;
import java.util.HashMap;

import nl.uva.morlb.rg.agent.model.BenchmarkReward;
import nl.uva.morlb.rg.agent.model.QTableEntry;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.State;
import nl.uva.morlb.util.Util;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;

public class MOQLearning implements AgentInterface {

    private final double INITIAL_Q_VALUE = -9.0d;

    private static final double GAMMA = 0.9d;
    private static final double ALPHA = 0.5d;

    private HashMap<QTableEntry, BenchmarkReward>[] mQTable;

    private int mCurrentObjective = 0;
    private QTableEntry mLastEntry;
    private boolean[] mInventory;

    private TaskSpecVRLGLUE3 mTaskSpec;

    private static boolean[][] inventoryHack = { { true, true }, { true, false }, { false, true }, { false, false } };

    @Override
    public void agent_init(final String taskSpec) {
        mTaskSpec = new TaskSpecVRLGLUE3(taskSpec);
        mQTable = new HashMap[mTaskSpec.getNumOfObjectives()];

        mInventory = new boolean[mTaskSpec.getNumOfObjectives() - 1];

        final int actionDim = mTaskSpec.getDiscreteActionRange(0).getMax() + 1;
        final int maxX = 4;
        final int maxY = 4;

        for (int objective = 0; objective < mTaskSpec.getNumOfObjectives(); ++objective) {
            mQTable[objective] = new HashMap<QTableEntry, BenchmarkReward>();
            for (int x = 0; x < maxX; ++x) {
                for (int y = 0; y < maxY; ++y) {

                    final Location location = new Location(x, y);

                    for (final boolean[] inventory : inventoryHack) {

                        final State state = new State(location, inventory);
                        for (int actionCounter = 0; actionCounter < actionDim; ++actionCounter) {
                            mQTable[objective].put(new QTableEntry(state, DiscreteAction.values()[actionCounter]),
                                    new BenchmarkReward(new double[] { INITIAL_Q_VALUE, INITIAL_Q_VALUE,
                                            INITIAL_Q_VALUE }));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Action agent_start(final Observation observation) {
        for (int i = 0; i < mInventory.length; ++i) {
            mInventory[i] = false;
        }

        final State state = generateState(observation);
        final DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    @Override
    public Action agent_step(final Reward reward, final Observation observation) {
        // Calculate the current inventory
        for (int i = 1; i < reward.doubleArray.length; ++i) {
            if (reward.doubleArray[i] != 0) {
                mInventory[i - 1] = true;
            }
        }
        final State state = generateState(observation);
        final BenchmarkReward convertedReward = new BenchmarkReward(reward.doubleArray);

        // Q-Table step
        double bestActionValue = Double.NEGATIVE_INFINITY;
        BenchmarkReward bestActionReward = null;
        for (int i = 0; i < 5; ++i) {
            final DiscreteAction currentAction = DiscreteAction.values()[i];

            final QTableEntry g = new QTableEntry(state, currentAction);
            final BenchmarkReward qTableReward = getCurrentQTable().get(g);
            final double currentValue = qTableReward.scalarise(getCurrentScalar()).getSum();

            if (currentValue >= bestActionValue) {
                bestActionValue = currentValue;
                bestActionReward = qTableReward;
            }
        }

        BenchmarkReward lastStateValue = getCurrentQTable().get(mLastEntry);
        lastStateValue = lastStateValue.add(convertedReward.add(bestActionReward, GAMMA).sub(lastStateValue), ALPHA);
        getCurrentQTable().put(mLastEntry, lastStateValue);

        // Define the next action
        final DiscreteAction action = getRandomAction();
        mLastEntry = new QTableEntry(state, action);

        return action.convertToRLGlueAction();
    }

    @Override
    public void agent_end(final Reward reward) {
        final BenchmarkReward convertedReward = new BenchmarkReward(reward.doubleArray);
        final BenchmarkReward nextReward = new BenchmarkReward(new double[reward.doubleArray.length]);

        BenchmarkReward lastStateValue = getCurrentQTable().get(mLastEntry);
        lastStateValue = lastStateValue.add(convertedReward.add(nextReward, GAMMA).sub(lastStateValue), ALPHA);
        getCurrentQTable().put(mLastEntry, lastStateValue);

        if (++mCurrentObjective == mTaskSpec.getNumOfObjectives()) {
            mCurrentObjective = 0;
        }
    }

    @Override
    public void agent_cleanup() {

        // Just used to print the found policies
        final boolean[] desiredInventory = inventoryHack[1];
        mCurrentObjective = 0;

        while (mCurrentObjective != mTaskSpec.getNumOfObjectives()) {
            System.out.println("Objective: " + mCurrentObjective);
            for (int y = 3; y >= 0; --y) {
                for (int x = 0; x < 4; ++x) {

                    final Location location = new Location(x, y);
                    final State state = new State(location, desiredInventory);

                    double bestActionValue = Double.NEGATIVE_INFINITY;
                    DiscreteAction bestAction = null;
                    for (int i = 0; i < 5; ++i) {
                        final DiscreteAction currentAction = DiscreteAction.values()[i];
                        final double currentActionValue = getCurrentQTable().get(new QTableEntry(state, currentAction))
                                .scalarise(getCurrentScalar()).getSum();

                        if (currentActionValue > bestActionValue) {
                            bestActionValue = currentActionValue;
                            bestAction = currentAction;
                        }
                    }

                    System.out.print("\t" + bestAction.name() + "\t");

                }
                System.out.println();
            }
            mCurrentObjective++;
        }
    }

    @Override
    public String agent_message(final String arg0) {
        return null;
    }

    /**
     * Get the current Q-Table for the active objective
     *
     * @return The current Q-Table
     */
    private HashMap<QTableEntry, BenchmarkReward> getCurrentQTable() {
        return mQTable[mCurrentObjective];
    }

    /**
     * Get the scalar for the current objective to maximise
     *
     * @return The scalar for the objective to maximise
     */
    private double[] getCurrentScalar() {
        final double[] scalar = new double[mTaskSpec.getNumOfObjectives()];
        for (int i = 0; i < scalar.length; ++i) {
            if (i == mCurrentObjective) {
                scalar[i] = 1000;
            } else {
                scalar[i] = 0.01;
            }
        }

        return scalar;
    }

    /**
     * Generate the current state from the observation and the current inventory
     *
     * @param observation
     *            The current observation
     * @return The current state
     */
    public State generateState(final Observation observation) {
        final double[] observationArray = observation.doubleArray;

        final Location currentLocation = new Location(observationArray[0], observationArray[1]);
        return new State(currentLocation, Arrays.copyOf(mInventory, mInventory.length));
    }

    /**
     * The next action form our random policy
     *
     * @return The next action
     */
    public DiscreteAction getRandomAction() {
        return DiscreteAction.values()[Util.RNG.nextInt(5)];
    }

    /**
     * Prints the reward
     *
     * @param reward
     *            The reward to be printed
     */
    public void printReward(final Reward reward) {
        for (final double d : reward.doubleArray) {
            System.out.print(d + " ");
        }
        System.out.println();

    }
}