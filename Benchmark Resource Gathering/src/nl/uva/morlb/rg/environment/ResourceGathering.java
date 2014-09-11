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
import nl.uva.morlb.rg.environment.model.State;
import nl.uva.morlb.util.Util;

/**
 * The main resource gathering problem. Controls the states, transitions and rewards based on a set of parameters.
 */
public class ResourceGathering {

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
    }

    /**
     * Lets the agent perform a certain action to transition to the next state and collect a reward. This method may
     * contain stochasticity. The action must follow the parameter's action space size restrictions.
     *
     * @param action
     *            The action to perform
     *
     * @return The state resulting from performing the action
     */
    public State performAction(final DiscreteAction action) {
        if (!mParameters.actionsExpanded && action.ordinal() > 4) {
            throw new InvalidParameterException("Action value cannot exceed 4 with a non-expanded action space");
        }

        // Determine which failure action to add to the agent's action
        final DiscreteAction failAction;
        if (Util.RNG.nextDouble() < mParameters.actionFailProb) {
            // Determine which action from 1 to 8 is the failure that altered the next state outcome
            final int failureIndex = Util.RNG.nextInt(mParameters.actionsExpanded ? 7 : 3) + 1;
            failAction = DiscreteAction.values()[failureIndex];
        } else {
            failAction = DiscreteAction.WAIT;
        }

        mCurrentState = getNextState(mCurrentState, action, failAction);
        return mCurrentState;
    }

    /**
     * Determines all possible outcomes given an state and action. The states contain the reward that was achieved
     * through the transition.
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
        final int numSecondActions = (mParameters.actionFailProb > 0 ? (mParameters.actionsExpanded ? 8 : 4) : 1);
        for (int actionIndex = 0; actionIndex < numSecondActions; ++actionIndex) {
            final State nextState = getNextState(state, action, DiscreteAction.values()[actionIndex]);

            // Calculate the probability of transitioning to this state
            double probability;
            if (actionIndex == 0) {
                probability = 1.0 - mParameters.actionFailProb;
            } else {
                probability = mParameters.actionFailProb / numSecondActions;
            }

            // Add the state and probability to the possible outcomes
            if (stateProbabilities.containsKey(nextState)) {
                // Sum probabilities if one state can be reached through different ways
                probability += stateProbabilities.get(nextState);
            }
            stateProbabilities.put(state, probability);
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
    private State getNextState(final State state, final DiscreteAction agentAction, final DiscreteAction failAction) {
        // Set the agent's new location bound within the problem size
        Location nextAgent = state.getAgent().sum(agentAction.getLocation()).sum(failAction.getLocation());
        nextAgent = nextAgent.bound(0, mParameters.maxX, 0, mParameters.maxY);

        // Calculate the reward for this state and picks items up if needed
        final double[] reward = new double[mParameters.numResourceTypes + 1];
        reward[0] = -1;
        int resourceIndex = 0;
        final boolean[] pickedUp = state.getPickedUp();
        for (final Resource resource : mResources) {
            if (!pickedUp[resourceIndex] && nextAgent.equals(resource.getLocation())) {
                reward[resource.getType() + 1] += resource.calculateReward();

                if (mParameters.finiteHorizon) {
                    pickedUp[resourceIndex] = true;
                }
            }
            ++resourceIndex;
        }

        // Add the state and probability to the possible outcomes
        return new State(nextAgent, pickedUp, reward, nextAgent.equals(mGoal));
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
