package app;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import structure.Graph;

public class App {

    public static void main(String[] args) {
        boolean continueSession = true; // condition to keep the program running
        System.out.println("\n--------------------------------------------- New Session ---------------------------------------------");

        try (Scanner sc = new Scanner(System.in);) {

            // Initialization the scenario by loading the first graph
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
                        System.out.println("Okay, end of the session.");
                        continueSession = false;
                    }
                } catch (Exception e) {
                    System.out.println("An unexpected error occurred: " + e.getMessage());
                }
            }
        }
    }

    /*
     * Method to ask the user which graph file to load
     */
    public static Graph askingFileToRead(Scanner sc) {
        try {
            Graph g = null; // g will be our Graph to be tested

            while (g == null) {
                System.out.print("Which graphs do you want to load ? : ");
                String number = sc.nextLine().trim();

                String path = "src/graphs/" + number + ".txt"; // relative path (according the the starting point of the project architecture filre) to the any graph file

                File file = new File(path);
                if (!file.exists()) {
                    System.out.println(">>> File " + path + " not found. Please try again.\n");
                    continue;
                }

                try {
                    g = loadGraphFromFile(path);
                } catch (IOException e) {
                    System.out.println(">>> Error while reading the file : " + e.getMessage());
                    System.out.println(">>> Please try again.");
                }
            }
            return g;

        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while reading input: " + e.getMessage(), e);
        }
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
            boolean directed = false; // setting a default value

            if (fileScanner.hasNextLine()) {
                directed = fileScanner.nextLine().trim().equals("1"); // read the first line to determine if the graph is directed (1) or undirected (0)
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
     * Method to display the results (Floyd-Warshall matrix, ...) of a paramter passed Graph
     */
    public static void displayResults(Graph g) {
        System.out.println("\nFloyd-Warshall matrix:");
        g.floydWarshall();
    }

}
