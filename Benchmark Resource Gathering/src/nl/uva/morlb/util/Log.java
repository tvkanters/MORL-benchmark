package nl.uva.morlb.util;

/**
 * A logging class that lets different levels be toggled.
 */
public class Log {

    /** Whether debug levels should be printed */
    public static final boolean D = true;
    /** Whether informative levels should be printed */
    public static final boolean I = true;
    /** Whether error levels should be printed */
    public static final boolean E = true;

    /**
     * Logs a debug level message.
     *
     * @param message
     *            The message to log
     */
    public static void d(final String message) {
        if (D) {
            System.out.println(message);
        }
    }

    /**
     * Logs an informative level message.
     *
     * @param message
     *            The message to log
     */
    public static void i(final String message) {
        if (I) {
            System.out.println(message);
        }
    }

    /**
     * Logs an error level message.
     *
     * @param message
     *            The message to log
     */
    public static void e(final String message) {
        if (E) {
            System.out.println(message);
        }
    }

}
