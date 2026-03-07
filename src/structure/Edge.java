package structure;

import java.util.Objects;

public record Edge(Node origin, Node destination, int weight) {

    /*
     * An edge is defined by :
        * An origin node
        * A destination node
        * A weight (or cost) of the edge, here an Int
     */

    public Edge(Node origin, Node destination, int weight) {
        this.origin = Objects.requireNonNull(origin);
        this.destination = Objects.requireNonNull(destination);
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Edge(Origin ID = " + this.origin.id() + ", Destination ID = " + this.destination.id() + ", Weight = " + this.weight + ")";
    }
}
