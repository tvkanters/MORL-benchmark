package nl.uva.morlb.rg.environment;

import java.util.LinkedList;
import java.util.List;

import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.Resource;

/**
 * A collection of sequential decision problems (SDPs) that can be used as resource gathering parameters.
 */
public class SdpCollection {

    /**
     * Creates a small, discrete problem with 3 objectives.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getSimpleProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2));
        resources.add(new Resource(1, 2, 1));

        return new Parameters(3, 3, resources, Parameters.ACTIONS_LIMITED, 1, 0, Parameters.FULLY_OBSERVABLE,
                Parameters.STATES_DISCRETE);
    }

    /**
     * Creates a large, discrete problem with 3 objectives.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getLargeProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 3, 2));
        resources.add(new Resource(1, 2, 5));
        resources.add(new Resource(1, 4, 9));
        resources.add(new Resource(0, 8, 4));

        return new Parameters(9, 9, resources, Parameters.ACTIONS_LIMITED, 1, 0, Parameters.FULLY_OBSERVABLE,
                Parameters.STATES_DISCRETE);
    }

    /**
     * Creates a medium, discrete problem with 3 objectives and 9 actions.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getManyActionsProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2));
        resources.add(new Resource(1, 3, 1));
        resources.add(new Resource(1, 0, 4));

        return new Parameters(5, 5, resources, Parameters.ACTIONS_EXPANDED, 1, 0, Parameters.FULLY_OBSERVABLE,
                Parameters.STATES_DISCRETE);
    }

    /**
     * Creates a small, continuous problem with 3 objectives.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getSmallContinuousProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2));
        resources.add(new Resource(1, 2, 1));

        return new Parameters(3, 3, resources, Parameters.ACTIONS_LIMITED, 1, 0, Parameters.FULLY_OBSERVABLE,
                Parameters.STATES_CONTINUOUS);
    }

    /**
     * Creates a medium, discrete partially observable problem with 3 objectives.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getPartiallyObservableProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2));
        resources.add(new Resource(1, 3, 1));
        resources.add(new Resource(1, 0, 4));

        return new Parameters(5, 5, resources, Parameters.ACTIONS_LIMITED, 1, 0, 1, Parameters.STATES_DISCRETE);
    }

    /**
     * Creates a medium, discrete problem with 3 objectives and stochasticity in transitions and rewards.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getStochasticProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2, 0.5, 2.0));
        resources.add(new Resource(1, 3, 1, 1.0, 2.5));
        resources.add(new Resource(1, 0, 4, 0.3, 1.0));

        return new Parameters(5, 5, resources, Parameters.ACTIONS_LIMITED, 1, 0.4, 1, Parameters.STATES_DISCRETE);
    }

    /**
     * Creates a small, discrete problem with 3 objectives and an infinite horizon.
     *
     * @return The parameters to pass to the resource gathering problem
     */
    public static Parameters getInfiniteHorizonProblem() {
        final List<Resource> resources = new LinkedList<>();
        resources.add(new Resource(0, 1, 2));
        resources.add(new Resource(1, 2, 1));

        return new Parameters(3, 3, resources, Parameters.ACTIONS_LIMITED, 0.8, 0, Parameters.FULLY_OBSERVABLE,
                Parameters.STATES_DISCRETE);
    }

}
