package logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class LoggingConfig {

    private static final String LOG_FILE_PATH = "logs/currentRun.log";
    private static final String GRAPH_LOGS_DIR = "logs/graphs";
    private static FileHandler sharedFileHandler;
    private static final Map<String, FileHandler> graphFileHandlers = new HashMap<>();
    private static final Map<String, FileHandler> activeGraphHandlersByLogger = new HashMap<>();

    private LoggingConfig() {
    }

    public static synchronized void configureLogger(Logger logger) {
        try {
            Files.createDirectories(Path.of("logs"));

            if (sharedFileHandler == null) {
                sharedFileHandler = new FileHandler(LOG_FILE_PATH, true);
                sharedFileHandler.setEncoding("UTF-8");
                sharedFileHandler.setLevel(Level.ALL);
                sharedFileHandler.setFormatter(new SimpleAppFormatter());
            }

            boolean alreadyAttached = false;
            for (Handler handler : logger.getHandlers()) {
                if (handler == sharedFileHandler) {
                    alreadyAttached = true;
                    break;
                }
            }

            if (!alreadyAttached) {
                logger.addHandler(sharedFileHandler);
            }

            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize logger", e);
        }
    }

    public static synchronized void configureGraphLogger(Logger logger, String graphKey) {
        try {
            configureLogger(logger);
            Files.createDirectories(Path.of(GRAPH_LOGS_DIR));

            String safeGraphKey = sanitizeGraphKey(graphKey);
            FileHandler graphHandler = graphFileHandlers.get(safeGraphKey);

            if (graphHandler == null) {
                String graphLogPath = GRAPH_LOGS_DIR + "/graph-" + safeGraphKey + ".log";
                graphHandler = new FileHandler(graphLogPath, true);
                graphHandler.setEncoding("UTF-8");
                graphHandler.setLevel(Level.ALL);
                graphHandler.setFormatter(new SimpleAppFormatter());
                graphFileHandlers.put(safeGraphKey, graphHandler);
            }

            String loggerName = logger.getName();
            FileHandler previousGraphHandler = activeGraphHandlersByLogger.get(loggerName);
            if (previousGraphHandler != null && previousGraphHandler != graphHandler) {
                logger.removeHandler(previousGraphHandler);
            }

            boolean alreadyAttached = false;
            for (Handler handler : logger.getHandlers()) {
                if (handler == graphHandler) {
                    alreadyAttached = true;
                    break;
                }
            }

            if (!alreadyAttached) {
                logger.addHandler(graphHandler);
            }

            activeGraphHandlersByLogger.put(loggerName, graphHandler);

        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize graph logger", e);
        }
    }

    private static String sanitizeGraphKey(String graphKey) {
        String value = (graphKey == null || graphKey.isBlank()) ? "unknown" : graphKey;
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static class SimpleAppFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            sb.append("[")
                    .append(record.getLevel())
                    .append("] ")
                    .append(formatMessage(record))
                    .append(System.lineSeparator());

            if (record.getThrown() != null) {
                Throwable t = record.getThrown();
                sb.append(t).append(System.lineSeparator());
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append("    at ").append(ste).append(System.lineSeparator());
                }
            }

            return sb.toString();
        }
    }
}

