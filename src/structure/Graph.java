package structure;

import java.util.*;

/**
 * Graph structure designed to support multiple independent graphs at runtime.
 * optimized to iterate through as Floyd-Warshall algorithm needs it ofc
 * edges.get(u.getId()).get(v.getId()) -> Edge from node u to node v
 */
public final class Graph {

    private final Map<Integer, Node> nodes = new HashMap<>();               // id -> Node
    private final Map<Integer, Map<Integer, Edge>> edges = new HashMap<>(); // originId -> (destId -> Edge)
    private final boolean directed;
    // we have final map, has we want to be sure that the map storing the nodes or the edges stays in the same object without any dereferencement

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public Node addNode(int id) {
        return this.nodes.computeIfAbsent(id, Node::new); // computeIfAbsent -> native function of Map<>, return a new object if Absent
        // Node::new is a method reference, equivalent to id -> new Node(id)
        // understand it as an auto constructor for the record struct 
        //     (see explantation of record struc in Node.java)
    }

    /**
     * Adds or replaces an originId -> (destId -> Edge) with given weight. For
     * undirected graphs, also adds/replaces destId -> (originId -> Edge) with
     * same weight. If multiple edges for same pair are given over time, this
     * keeps the minimum weight.
     */
    public void addEdge(int originId, int destinationId, int weight) {
        //Here you can add a condition for forbidding edge that loop

        Node origin = addNode(originId);
        Node destination = addNode(destinationId);

        putMinEdge(origin, destination, weight);

        if (!this.directed) {
            putMinEdge(destination, origin, weight); // if undirected, add the reverse edge as well (same weight)
        }
    }

    private void putMinEdge(Node origin, Node destination, int weight) {
        Map<Integer, Edge> out = this.edges.computeIfAbsent(origin.id(), _ -> new HashMap<>());
        Edge existing = out.get(destination.id());

        if (existing == null || weight < existing.weight()) {
            out.put(destination.id(), new Edge(origin, destination, weight));
        }
    }

    public Edge getEdge(int originId, int destinationId) {
        Map<Integer, Edge> out = this.edges.get(originId);
        return out == null ? null : out.get(destinationId);
    }

    // OptionalInt is a native function, here used to avoid using the return "null"
    // print it using : 
    // OptionalInt weight = g.getWeight(1, 2);
    // if(weight.isPresent()){System.out.println(weight.getAsInt());}
    public OptionalInt getWeight(int originId, int destinationId) {
        Edge e = getEdge(originId, destinationId);
        return e == null ? OptionalInt.empty() : OptionalInt.of(e.weight());
    }

    // Collections.unmodifiableMap -> native function of Map< >, return an unmodifiable view of the map (read-only)
    public Map<Integer, Edge> outgoingFrom(int originId) {
        Map<Integer, Edge> out = edges.get(originId);
        return out == null ? Map.of() : Collections.unmodifiableMap(out);
    }

    public boolean isDirected() {
        return this.directed;
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(this.nodes.values());
    }

    public Map<Integer, Node> getNodesById() {
        return Collections.unmodifiableMap(this.nodes);
    }

    public Map<Integer, Map<Integer, Edge>> getEdges() {
        return Collections.unmodifiableMap(this.edges);
    }

    /**
     * originId -> (destId -> smallest weight from originId to destId)
     */
    public void floydWarshall() {

        final int INF = Integer.MAX_VALUE / 2; // avoid value overflow, and memory overflow if we do INF + INF when processing the Floyd-Warshall algo

        // Step 1: Initialize distance matrix
        Map<Integer, Map<Integer, Integer>> L = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> P = new HashMap<>();

        // Double loop to set the diagonal to 0, and the rest to INF. As the shortest path from node X to node X is 0, and else we don't know.
        for (Node u : nodes.values()) {
            Map<Integer, Integer> rowDist = new HashMap<>(); // temp Map for L
            Map<Integer, Integer> rowPred = new HashMap<>(); // temp Map for P

            for (Node v : nodes.values()) {
                if (u.equals(v)) {
                    rowDist.put(v.id(), 0);    // from node X to node X, the shortest path is 0
                    rowPred.put(v.id(), null); // no predecessor for the same node, as we are already there
                } else {
                    rowDist.put(v.id(), INF);         // esle we don't know the shortest path 
                    rowPred.put(v.id(), null); // else we don't know
                }
            }
            L.put(u.id(), rowDist);
            P.put(u.id(), rowPred);
        }

        // Step 2: Insert the weights of known edges
        for (var entry : edges.entrySet()) {
            // entrySet() -> native function of Map<>, return a set of edges<Key, Value> (in our case : edges<originId, (destId -> Edge)>)
            // you also have entry.getKey() & entry.getValue() 
            int u = entry.getKey();
            for (Edge e : entry.getValue().values()) {
                L.get(u).put(e.destination().id(), e.weight());
                P.get(u).put(e.destination().id(), u);
            }
        }
        System.out.println("---- Initial distance matrix ----");
        printDistanceMatrix(L, false);
        System.out.println();
        System.out.println("---- Initial predecessor matrix ----");
        printDistanceMatrix(P, true);
        System.out.println();

        // Step 3: Floyd-Warshall core
        /*
         * i = origin node
         * j = destination node
         * k = intermediate node
         * ik = shortest path from i to k (found so far)
         * kj = shortest path from k to j (found so far)
         * ij = actual distance weight
         * 
         * The algo print each step of the process
         * The initial and final distance matrix.
         */
        for (Node k : nodes.values()) {
            System.out.println("\nIntermediate node : " + k.id());
            for (Node i : nodes.values()) {
                //System.out.println("for origin node : " + i.id());
                for (Node j : nodes.values()) {

                    int ik = L.get(i.id()).get(k.id()); // computing the differents routes
                    int kj = L.get(k.id()).get(j.id());
                    int ij = L.get(i.id()).get(j.id());

                    if (ik != INF && kj != INF && ik + kj < ij) { // if they are not INF, and passing by k is faster
                        L.get(i.id()).put(j.id(), ik + kj);    // then add it to the distance matrix
                        P.get(i.id()).put(j.id(), P.get(k.id()).get(j.id()));
                    }
                }
            }
            printDistanceMatrix(L, false);
            System.out.println();
            printDistanceMatrix(P, true);
            System.out.println();
        }

        // Step 4: Print the final distance matrix
        System.out.println("---- Final distance matrix ----");
        printDistanceMatrix(L, false);
        System.out.println();

        System.out.println("---- Final predecessor matrix ----");
        printDistanceMatrix(P, true);
        System.out.println();

        // Step 5: Detect negative cycles
        for (Node n : nodes.values()) {
            if (L.get(n.id()).get(n.id()) < 0) {
                System.out.println(">>> Warning: Negative cycle detected involving node " + n.id());
            }
        }

    }

    /**
     * PRINTING PART
     */
    private String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }

        int padding = width - text.length();
        int padStart = padding / 2;
        int padEnd = padding - padStart;

        return " ".repeat(padStart) + text + " ".repeat(padEnd);
    }

    // Pretty prints of Floyd-Warshall distance matrix with centered values and visual separation (| and -)
    public void printDistanceMatrix(Map<Integer, Map<Integer, Integer>> M, boolean isMPredecessor) {

        final int INF = Integer.MAX_VALUE / 2;

        List<Integer> ids = new ArrayList<>(nodes.keySet());
        Collections.sort(ids);

        int cellWidth = 10;

        // Header
        System.out.print(" ".repeat(cellWidth));
        System.out.print("|");
        for (int id : ids) {
            System.out.print(center(String.valueOf(id), cellWidth));
        }
        System.out.println();

        // Separator
        System.out.print("-".repeat(cellWidth));
        System.out.print("|");
        System.out.println("-".repeat(cellWidth * ids.size()));

        // Rows
        for (int i : ids) {

            // Row header
            System.out.print(center(String.valueOf(i), cellWidth));
            System.out.print("|");

            // Row values
            for (int j : ids) {
                Integer value = M.get(i).get(j);
                String toPrint;

                if (isMPredecessor) {
                    toPrint = (value == null) ? "-" : String.valueOf(value);
                } else {
                    toPrint = (value >= INF) ? "INF" : String.valueOf(value);
                }

                System.out.print(center(toPrint, cellWidth));
            }

            System.out.println();
        }
    }
}
