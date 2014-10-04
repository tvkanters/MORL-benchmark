package nl.uva.morlb.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A logging class that lets different levels be toggled.
 */
public class Log {

    /** Whether debug levels should be printed */
    public static final boolean D = false;
    /** Whether informative levels should be printed */
    public static final boolean I = true;
    /** Whether error levels should be printed */
    public static final boolean E = true;
    /** Whether file logging should be printed */
    public static final boolean F = true;

    private static final String PRINT_FILE_NAME = "logs "
            + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".txt";
    private static PrintWriter sPrintOut;

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

    /**
     * Logs a message into a file.
     *
     * @param message
     *            The message to log
     */
    public static void f(final String message) {
        if (F) {
            System.out.println(message);
        }

        try {
            if (sPrintOut == null) {
                final File file = new File(PRINT_FILE_NAME);
                System.out.println(PRINT_FILE_NAME);
                file.createNewFile();
                sPrintOut = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            }
            sPrintOut.println(message);
            sPrintOut.flush();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

}
