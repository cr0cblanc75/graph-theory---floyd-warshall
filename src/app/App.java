package app;

import structure.Graph;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    static {
        try {
            Files.createDirectories(Path.of("logs"));

            FileHandler fileHandler = new FileHandler("logs/currentRun.log", true);
            fileHandler.setEncoding("UTF-8");
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleAppFormatter());

            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // disable default console logger
            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize logger", e);
        }
    }

    public static void main(String[] args) {
        boolean continueSession = true; // condition to keep the program running
        System.out.println("\n--------------------------------------------- New Session ---------------------------------------------");
        //try with resources (here, global scanner instance) that loads the graph
        try (Scanner sc = new Scanner(System.in)) {
            Graph g = askingFileToRead(sc);
            displayResults(g);

            // Loop for the session
            while (continueSession) {
                try {
                    System.out.println("\nDo you want to load a new graph ? (y/n) : ");
                    String answer = sc.nextLine().trim().toLowerCase();

                    if (answer.equals("y") || answer.equals("yes")) {
                        g = askingFileToRead(sc);
                        displayResults(g);
                    } else {
                        LOGGER.info("End of session");
                        continueSession = false;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error during session loop", e);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal application error", e);
        }
    }

    /*
     * Method to ask the user which graph file to load
     */
    public static Graph askingFileToRead(Scanner sc) {
        Graph g = null; //g is the scenario's graph to be studied

        while (g == null) {
            try {
                System.out.print("Which graph do you want to load? ");
                String number = sc.nextLine().trim();

                String path = "src/graphs/" + number + ".txt"; //file relative path
                File file = new File(path);

                if (!file.exists()) {
                    LOGGER.warning("File not found: " + path);
                    continue;
                }

                g = loadGraphFromFile(path);
                LOGGER.info("Graph loaded successfully from " + path);

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error while reading graph file", e);
                System.out.println("Error while reading the file. Please try again.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error while loading graph", e);
                System.out.println("Unexpected error. Please try again.");
            }
        }

        return g;
    }

    /*
     * Method to convert a graph write in the .txt, to the Graph-object we will use
     *
     * The .txt file format :
     * First line : 1 if the graph is directed, 0 if undirected
     * Next lines : originId(Node = vertex) "space" destinationId(Node = vertex) "space" weight (Edge = arc)
     */
    public static Graph loadGraphFromFile(String path) throws IOException {
        try (Scanner fileScanner = new Scanner(new File(path))) {
            boolean directed = false; // graph is by default undirected

            if (fileScanner.hasNextLine()) {
                directed = fileScanner.nextLine().trim().equals("1"); //if 1st line is 1, it's a directed graph
            }

            Graph g = new Graph(directed);

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                int origin = Integer.parseInt(parts[0]);
                int destination = Integer.parseInt(parts[1]);
                int weight = Integer.parseInt(parts[2]);

                g.addEdge(origin, destination, weight);
            }

            return g;
        }
    }

    /*
     * Method to display the results (Floyd-Warshall matrix, ...) of a parameter-passed Graph
     */
    public static void displayResults(Graph g) {
        LOGGER.info("Starting Floyd-Warshall computation");
        LOGGER.info("Floyd-Warshall matrix:");
        g.floydWarshall();
        LOGGER.info("Finished Floyd-Warshall computation");
    }

    /*
     * Custom formatter for cleaner log lines
     */
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