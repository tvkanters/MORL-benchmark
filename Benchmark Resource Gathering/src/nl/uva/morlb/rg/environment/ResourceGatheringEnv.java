package nl.uva.morlb.rg.environment;

import java.util.List;

import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Location;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

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
     * Called when preparing the problem.
     */
    @Override
    public String env_init() {
        // Task specification
        final TaskSpecVRLGLUE3 taskSpec = new TaskSpecVRLGLUE3();
        taskSpec.setEpisodic();

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

        // Specify the action space size
        taskSpec.addDiscreteAction(new IntRange(0, (mParameters.actionsExpanded ? 8 : 4)));

        // Specify the number of objectives as the amount of resource types and time taken
        taskSpec.setNumOfObjectives(mParameters.numResourceTypes + 1);

        // Specify the map dimensions
        taskSpec.setExtra(mParameters.maxX + "," + mParameters.maxY);

        // Set the discount factor
        taskSpec.setDiscountFactor(mParameters.discountFactor);

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
        return getObservation();
    }

    /**
     * Performs an action in the problem and determines the resulting reward and state
     * 
     * @param action
     *            The action that the agent wants to perform
     * 
     * @return The resulting reward, observation and whether or not the problem is now terminal
     */
    @Override
    public Reward_observation_terminal env_step(final Action action) {
        final Reward_observation_terminal rewObsTer = new Reward_observation_terminal();

        // Perform the action and collect the reward
        final Reward reward = new Reward(0, mNumRewards, 0);
        reward.doubleArray = mProblem.performAction(DiscreteAction.values()[action.getInt(0)]);
        rewObsTer.setReward(reward);

        // Get the observation of the new state that the action transitioned to
        rewObsTer.setObservation(getObservation());

        // Check if the resulting state is terminal
        rewObsTer.setTerminal(mProblem.isTerminal());

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
     * Handles Glue messages, not implemented yet
     * 
     * @param message
     *            The message to handle
     */
    @Override
    public String env_message(final String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return The observation representation of the current problem state
     */
    private Observation getObservation() {
        final Observation observation = new Observation(0, mNumObservations, 0);
        int i = 0;

        // Specify the location of the agent
        final Location agent = mProblem.getAgent();
        observation.setDouble(i++, agent.getX());
        observation.setDouble(i++, agent.getY());

        // Specify the location of the goal
        final Location goal = mProblem.getGoal();
        observation.setDouble(i++, goal.getX());
        observation.setDouble(i++, goal.getY());

        // Specify the locations of the resources
        final List<Resource> resources = mProblem.getResources();
        for (final Resource resource : resources) {
            if (resource.isPickedUp()) {
                observation.setDouble(i++, -1);
                observation.setDouble(i++, -1);
            } else {
                final Location location = resource.getLocation();
                observation.setDouble(i++, location.getX());
                observation.setDouble(i++, location.getY());
            }
            observation.setInt(i++, resource.getType());
        }

        return observation;
    }

    public static void main(final String[] args) {
        new EnvironmentLoader(new ResourceGatheringEnv()).run();
    }

}
