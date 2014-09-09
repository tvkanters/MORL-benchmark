package nl.uva.morlb.environment.rg.model;

import java.security.InvalidParameterException;

/**
 * A resource that can be placed in a resource gathering problem.
 */
public class Resource {

    /** The type of resource */
    private final int mType;

    /**
     * Creates a new resource.
     *
     * @param type
     *            The type of resource, must be 0 or higher
     */
    public Resource(final int type) {
        if (type < 0) {
            throw new InvalidParameterException("Resource types must be 0 or higher");
        }
        mType = type;
    }

    /**
     * @return The type of resource
     */
    public int getType() {
        return mType;
    }
}
