package nl.uva.morlb.rg.environment;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;
import nl.uva.morlb.rg.environment.model.RewardRange;
import nl.uva.morlb.rg.environment.model.State;
import nl.uva.morlb.util.Log;
import nl.uva.morlb.util.Util;

/**
 * The main resource gathering problem. Controls the states, transitions and rewards based on a set of parameters.
 */
public class ResourceGathering {

    /** The (negative) reward for each time step */
    private static final RewardRange TIME_REWARD = new RewardRange(-1, -1);

    /** The parameters affecting the problem */
    private final Parameters mParameters;
    /** The resources that can be collected by the agent */
    private final List<Resource> mResources;
    /** The goal's location that when reached by the agent indicates a terminal state */
    private final Location mGoal;

    /** The state that the problem starts with */
    private final State mInitialState;
    /** The state that the problem is currently in */
    private State mCurrentState;
    /** The amount of steps taken */
    private int mStepCount = 0;

    /**
     * Creates a new resource gathering problem based on the given parameters.
     *
     * @param parameters
     *            The parameters that define the shape of the problem
     */
    public ResourceGathering(final Parameters parameters) {
        mParameters = parameters;

        mResources = mParameters.resources;
        mGoal = new Location(mParameters.maxX, mParameters.maxY);

        mInitialState = new State(new Location(0, 0), mResources.size());
        reset();
    }

    /**
     * Resets the problem to the initial state. Should be called before running each episode.
     */
    public void reset() {
        mCurrentState = mInitialState;
        mStepCount = 0;
        Log.d("ENV: New state is " + mCurrentState);
    }

    /**
     * Lets the agent perform a certain action to transition to the next state and collect a reward. This method may
     * contain stochasticity. The action must follow the parameter's action space size restrictions.
     *
     * @param action
     *            The action to perform
     *
     * @return The discounted reward resulting from performing the action
     */
    public double[] performAction(final DiscreteAction action) {
        if (action.ordinal() > mParameters.actionMax) {
            throw new InvalidParameterException("Action value exceeds action space");
        }

        // Log possible next states for debugging purposes
        if (Log.D && !mParameters.fullyObservable) {
            Log.d("");
            Log.d("ENV: Performing action " + action);
            Log.d("ENV: Possible results");
            final Map<State, Double> stateProbabilities = getPossibleTransitions(mCurrentState, action);
            for (final State state : stateProbabilities.keySet()) {
                Log.d("    " + state + " - " + stateProbabilities.get(state));
            }
            Log.d("");
        }

        return performAction(action.getLocation());
    }

    /**
     * Lets the agent perform a certain action to transition to the next state and collect a reward. This method may
     * contain stochasticity. The action must follow the parameter's action space size restrictions.
     *
     * @param action
     *            The action to perform
     *
     * @return The discounted reward resulting from performing the action
     */
    public double[] performAction(final Location action) {
        // Determine which failure action to add to the agent's action
        final Location failAction;
        if (Util.RNG.nextDouble() < mParameters.actionFailProb) {
            // Determine which action to modify the requested action with
            if (mParameters.continuousStatesActions) {
                final double xFail = Util.RNG.nextDouble() * mParameters.maxStepSize * 2 - mParameters.maxStepSize;
                final double yFail = Util.RNG.nextDouble() * mParameters.maxStepSize * 2 - mParameters.maxStepSize;
                failAction = new Location(xFail, yFail);
            } else {
                final int failureIndex = Util.RNG.nextInt(mParameters.actionMax - 1) + 1;
                failAction = DiscreteAction.values()[failureIndex].getLocation();
            }
        } else {
            failAction = new Location(0, 0);
        }

        // Determine the next state
        final State nextState = getNextState(mCurrentState, action, failAction);
        Log.d("ENV: New state is " + nextState);

        // Determine the rewards for the transition
        final RewardRange[] rewardRanges = getRewardRanges(mCurrentState, nextState);
        final double[] reward = new double[rewardRanges.length];
        for (int i = 0; i < reward.length; ++i) {
            reward[i] = rewardRanges[i].calculateReward() * Math.pow(mParameters.discountFactor, mStepCount);
        }

        // Make the transition to the next state
        mCurrentState = nextState;
        ++mStepCount;

        return reward;
    }

    /**
     * Determines all possible outcomes given a state and discrete action. The states contain the reward that was
     * achieved through the transition.
     *
     * @param state
     *            The current state
     * @param action
     *            The action performed by the agent
     *
     * @return All possible resulting states mapped to their probabilities
     */
    public Map<State, Double> getPossibleTransitions(final State state, final DiscreteAction action) {
        final Map<State, Double> stateProbabilities = new HashMap<State, Double>();

        // Add a second fail step (handles no failure with the WAIT action)
        final int numSecondActions = (mParameters.actionFailProb > 0 ? mParameters.actionMax : 1);
        for (int actionIndex = 0; actionIndex < numSecondActions; ++actionIndex) {
            final State nextState = getNextState(state, action.getLocation(),
                    DiscreteAction.values()[actionIndex].getLocation());

            // Calculate the probability of transitioning to this state
            double probability;
            if (actionIndex == 0) {
                probability = 1.0 - mParameters.actionFailProb;
            } else {
                probability = mParameters.actionFailProb / (numSecondActions - 1);
            }

            // Add the state and probability to the possible outcomes
            if (stateProbabilities.containsKey(nextState)) {
                // Sum probabilities if one state can be reached through different ways
                probability += stateProbabilities.get(nextState);
            }
            stateProbabilities.put(nextState, probability);
        }

        return stateProbabilities;
    }

    /**
     * Determines the state resulting of performing an agent action and applying the failure action. Does not contain
     * any stochasticity.
     *
     * @param state
     *            The current state
     * @param agentAction
     *            The action chosen by the agent
     * @param failAction
     *            The action added as a failure
     *
     * @return The resulting next state
     */
    private State getNextState(final State state, final Location agentAction, final Location failAction) {
        // Set the agent's new location bound within the problem size
        Location nextAgent = Location.sum(state.getAgent(), Location.sum(agentAction, failAction));
        nextAgent = nextAgent.bound(0, mParameters.maxX, 0, mParameters.maxY);

        // Calculate the reward for this state and picks items up if needed
        final boolean[] pickedUp = state.getPickedUp();
        int numPickedUp = state.getNumPickedUp();
        if (mParameters.finiteHorizon && numPickedUp < mParameters.maxPickedUp) {
            int resourceIndex = 0;
            for (final Resource resource : mResources) {
                if (!pickedUp[resourceIndex] && resource.isCollected(nextAgent)) {
                    pickedUp[resourceIndex] = true;
                    ++numPickedUp;
                    if (numPickedUp == mParameters.maxPickedUp) {
                        break;
                    }
                }
                ++resourceIndex;
            }
        }

        // Add the state and probability to the possible outcomes
        return new State(nextAgent, pickedUp);
    }

    /**
     * Determines the reward ranges that can be given for a state transition for every objective. Does NOT take discount
     * factors into account.
     *
     * @param initialState
     *            The state before transitioning
     * @param resultingState
     *            The state after transitioning
     *
     * @return The reward ranges for every objective
     */
    public RewardRange[] getRewardRanges(final State initialState, final State resultingState) {
        // Initialise the reward ranges and set the time reward
        final RewardRange[] reward = new RewardRange[mParameters.numResourceTypes + 1];
        reward[0] = TIME_REWARD;
        for (int i = 1; i < reward.length; ++i) {
            reward[i] = new RewardRange(0, 0);
        }

        // Sum the reward ranges of every picked up resource in their respective objective
        int resourceIndex = 0;
        int numPickedUp = initialState.getNumPickedUp();
        if (numPickedUp < mParameters.maxPickedUp) {
            for (final Resource resource : mResources) {
                if (!initialState.isPickedUp(resourceIndex) && resource.isCollected(resultingState.getAgent())) {
                    final int rewardIndex = resource.getType() + 1;
                    reward[rewardIndex] = reward[rewardIndex].sum(resource.getReward());
                    ++numPickedUp;
                    if (numPickedUp == mParameters.maxPickedUp) {
                        break;
                    }
                }
                ++resourceIndex;
            }
        }

        return reward;
    }

    /**
     * Checks if the given state is terminal.
     *
     * @param state
     *            The state to check
     *
     * @return True iff the state is terminal
     */
    public boolean isTerminal(final State state) {
        return state.getAgent().equals(mGoal);
    }

    /**
     * @return The list of resources in the game
     */
    public List<Resource> getResources() {
        return Collections.unmodifiableList(mResources);
    }

    /**
     * @return The state that the problem is currently in
     */
    public State getCurrentState() {
        return mCurrentState;
    }

    /**
     * @return The location of the goal
     */
    public Location getGoal() {
        return mGoal;
    }

}
