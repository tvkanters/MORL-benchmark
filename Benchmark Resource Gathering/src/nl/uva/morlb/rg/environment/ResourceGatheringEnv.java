package nl.uva.morlb.rg.environment;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;
import nl.uva.morlb.rg.environment.model.RewardRange;
import nl.uva.morlb.rg.environment.model.State;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

/**
 * The Glue wrapper for the environment.
 */
public class ResourceGatheringEnv implements EnvironmentInterface {

    /** The parameters affecting the problem */
    private final Parameters mParameters;
    /** The main resource gathering problem handling the states, transitions and rewards based on a set of parameters */
    private final ResourceGathering mProblem;
    /** The amount of observation dimensions sent each step */
    private final int mNumObservations;
    /** The amount of dimensions in the rewards given, reflects the amount of objectives */
    private final int mNumRewards;

    /**
     * Creates a new resource gathering problem with a default parameter set.
     */
    public ResourceGatheringEnv() {
        this(SdpCollection.getSimpleProblem());
    }

    /**
     * Creates a new resource gathering problem with a given parameter set.
     *
     * @param parameters
     *            The parameters affecting the problem
     */
    public ResourceGatheringEnv(final Parameters parameters) {
        mParameters = parameters;
        mNumObservations = 4 + mParameters.numResources * 3;
        mNumRewards = 1 + mParameters.numResourceTypes;

        // Initialise the problem
        mProblem = new ResourceGathering(mParameters);
    }

    /**
     * Shuffles the locations of the resources to random locations.
     *
     * @param rng
     *            The random number generator to determine the new positions with
     */
    public void shuffleResources(final Random rng) {
        mParameters.shuffleResources(rng);
    }

    /**
     * Called when preparing the problem.
     */
    @Override
    public String env_init() {
        // Task specification
        final TaskSpecVRLGLUE3 taskSpec = new TaskSpecVRLGLUE3();

        // Specify x and y observations for the agent position and the goal position
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.maxX));
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.maxY));
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.maxX));
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.maxY));

        // Specify content of each coordinate
        for (int i = 0; i < mParameters.numResources; ++i) {
            taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.maxX));
            taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.maxY));
            taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.numResourceTypes));
        }

        // Specify the number of objectives as the amount of resource types and time taken
        taskSpec.setNumOfObjectives(mParameters.numResourceTypes + 1);

        // Specify the action space
        if (mParameters.continuousStatesActions) {
            taskSpec.addContinuousAction(new DoubleRange(-mParameters.maxStepSize, mParameters.maxStepSize));
            taskSpec.addContinuousAction(new DoubleRange(-mParameters.maxStepSize, mParameters.maxStepSize));
        } else {
            taskSpec.addDiscreteAction(new IntRange((mParameters.finiteHorizon ? 1 : 0), mParameters.actionMax));
        }

        // Set the (in)finite horizon property
        if (mParameters.finiteHorizon) {
            taskSpec.setEpisodic();
        } else {
            taskSpec.setContinuing();
            taskSpec.setDiscountFactor(mParameters.discountFactor);
        }

        // Convert specification object to a string
        final String taskSpecString = taskSpec.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);
        return taskSpecString;
    }

    /**
     * Called when an episode starts.
     */
    @Override
    public Observation env_start() {
        mProblem.reset();
        return getObservation(mProblem.getCurrentState());
    }

    /**
     * Performs an action in the problem and determines the resulting reward and state
     *
     * @param action
     *            The action that the agent wants to perform
     *
     * @return The resulting reward, observation and whether or not the state is now terminal
     */
    @Override
    public Reward_observation_terminal env_step(final Action action) {
        final double[] rewardValues;
        if (mParameters.continuousStatesActions) {
            final double x = action.getDouble(0);
            final double y = action.getDouble(1);
            if (Math.abs(x) > mParameters.maxStepSize || Math.abs(y) > mParameters.maxStepSize) {
                throw new InvalidParameterException("Actions may not exceed " + mParameters.maxStepSize);
            }
            rewardValues = mProblem.performAction(new Location(x, y));
        } else {
            rewardValues = mProblem.performAction(DiscreteAction.values()[action.getInt(0)]);
        }

        final State newState = mProblem.getCurrentState();

        final Reward_observation_terminal rewObsTer = new Reward_observation_terminal();

        // Get the reward given
        final Reward reward = new Reward(0, mNumRewards, 0);
        reward.doubleArray = rewardValues;
        rewObsTer.setReward(reward);

        // Get the observation of the new state that the action transitioned to
        rewObsTer.setObservation(getObservation(newState));

        // Check if the resulting state is terminal
        rewObsTer.setTerminal(mProblem.isTerminal(newState));

        return rewObsTer;
    }

    /**
     * Resets the problem to the initial state.
     */
    @Override
    public void env_cleanup() {
        mProblem.reset();
    }

    /**
     * Handles Glue messages.
     *
     * @param message
     *            The message to handle
     */
    @Override
    public String env_message(final String message) {
        System.err.println(message);
        final String[] arguments = message.split(" ");
        switch (arguments[0]) {
            case "getPossibleTransitions":
                // Parse the message
                final State state = State.fromString(arguments[1]);
                final DiscreteAction action = DiscreteAction.values()[Integer.parseInt(arguments[2])];

                // Return the possible transactions
                String resultTrans = "";
                final Map<State, Double> possibleTransations = getPossibleTransitions(state, action);
                for (final State possibleState : possibleTransations.keySet()) {
                    resultTrans += possibleState + " " + possibleTransations.get(possibleState) + "\n";
                }

                return resultTrans;

            case "getRewardRanges":
                // Return the reward ranges for transitioning between two states
                String resultRange = "";
                final RewardRange[] rewards = getRewardRanges(State.fromString(arguments[1]),
                        State.fromString(arguments[2]));
                for (final RewardRange rewardRange : rewards) {
                    resultRange += rewardRange + "\n";
                }

                return resultRange;
        }

        throw new InvalidParameterException("Unknown message: " + message);
    }

    /**
     * Converts a state to an observation to be given to an agent.
     *
     * @param state
     *            The state to convert to an observation
     *
     * @return The observation representation of the state
     */
    private Observation getObservation(final State state) {
        final Observation observation = new Observation(0, mNumObservations, 0);
        int i = 0;

        // Specify the location of the agent
        final Location agent = state.getAgent();
        observation.setDouble(i++, agent.x);
        observation.setDouble(i++, agent.y);

        // Specify the location of the goal
        final Location goal = mProblem.getGoal();
        final boolean showGoal = (Location.distance(agent, goal) <= mParameters.viewDistance);
        observation.setDouble(i++, (showGoal ? goal.x : Double.NaN));
        observation.setDouble(i++, (showGoal ? goal.y : Double.NaN));

        // Specify the locations of the resources
        final List<Resource> resources = mProblem.getResources();
        int resourceIndex = 0;
        for (final Resource resource : resources) {
            // Determine what resource information to show in the observation
            final boolean showResource = (!state.isPickedUp(resourceIndex++) && Location.distance(agent,
                    resource.getLocation()) <= mParameters.viewDistance);

            // Add the available resource information
            final Location location = resource.getLocation();
            observation.setDouble(i++, (showResource ? location.x : Double.NaN));
            observation.setDouble(i++, (showResource ? location.y : Double.NaN));
            observation.setDouble(i++, (showResource ? resource.getType() : Double.NaN));
        }

        // Make locations relative to agent in case of partial observability
        if (!mParameters.fullyObservable) {
            boolean setX = true;
            for (i = 0; i < mNumObservations; ++i) {
                if (i < 4 || (i - 3) % 3 != 0) {
                    observation.setDouble(i, observation.getDouble(i) - (setX ? agent.x : agent.y));
                    setX = !setX;
                }
            }
        }

        return observation;
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
        return mProblem.getPossibleTransitions(state, action);
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
        return mProblem.getRewardRanges(initialState, resultingState);
    }

    /**
     * @return The state that the problem is currently in
     */
    public State getCurrentState() {
        return mProblem.getCurrentState();
    }

    /**
     * Determines if this state is terminal
     * @param state The state to check
     * @return True if the state is terminal, false if not
     */
    public boolean isTerminal(final State state) {
        return mProblem.isTerminal(state);
    }

}
