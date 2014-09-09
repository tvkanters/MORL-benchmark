package nl.uva.morlb.environment.rg;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

public class ResourceGatheringEnv implements EnvironmentInterface {

    private final Parameters mParameters;
    private final ResourceGathering mProblem;

    public ResourceGatheringEnv() {
        this(new Parameters());
    }

    public ResourceGatheringEnv(final Parameters parameters) {
        mParameters = new Parameters();

        // Initialise the problem
        mProblem = new ResourceGathering(mParameters);
    }

    @Override
    public String env_init() {

        // Task specification
        final TaskSpecVRLGLUE3 taskSpec = new TaskSpecVRLGLUE3();
        taskSpec.setEpisodic();

        // Specify x and y observations for the agent position and the goal position
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.mapWidth));
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.mapHeight));
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.mapWidth));
        taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.mapHeight));

        // Specify x and y observations for all resources
        for (int i = 0; i < mParameters.numResources; ++i) {
            taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.mapWidth));
            taskSpec.addContinuousObservation(new DoubleRange(0, mParameters.mapHeight));
        }

        // Specify that there will be an integer action [0,4]
        taskSpec.addDiscreteAction(new IntRange(0, 4));

        // Specify the number of objectives as the amount of resource types and time taken
        taskSpec.setNumOfObjectives(mParameters.numResourceTypes + 1);

        // Convert specification object to a string
        final String taskSpecString = taskSpec.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);
        return taskSpecString;
    }

    @Override
    public Observation env_start() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reward_observation_terminal env_step(final Action arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String env_message(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void env_cleanup() {
        // TODO Auto-generated method stub

    }

}
