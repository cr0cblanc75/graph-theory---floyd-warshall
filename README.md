# EFREI Graph Theory Project : Floyd--Warshall Shortest Path (Java)

## Requirements

-   **Java 23**

------------------------------------------------------------------------

## Project Structure

    src/
     |   app/
     |   └── App.java -> with inside the Floyd-Warshall
     ├── structure/
     |   ├── Edge.java
     |   ├── Graph.java
     |   └── Node.java
     ├── graphs/
     ├── logs/
     |   ├── graphs/
     |   └── currentRun.log
     ├── .gitignore
     └── README.md

### Core Design

-   `Graph`
    -   `Map<Integer, Node> nodes`
    -   `Map<Integer, Map<Integer, Edge>> edges`
-   `Node`
    -   `record Node(int id)`
-   `Edge`
    -   `record Edge(Node origin, Node destination, int weight)`

------------------------------------------------------------------------

## Running the Project

Run directly from your IDE by executing:

    src/app/App.java
