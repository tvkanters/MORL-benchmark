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
        //        resources.add(new Resource(0, 1, 2));
        //        resources.add(new Resource(1, 2, 1));

        return new Parameters(3, 3, resources, false, 1, 0, Parameters.FULLY_OBSERVABLE);
    }

}
