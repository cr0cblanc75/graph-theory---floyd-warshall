package structure;

public record Node(int id) {

    /*
     * What is a "record" ?
        * New type of Java16
        * Immuable -> Once a node is created it cannot be modified (no more setter, only getter)
        * Auto-generated : equals, hashCode, toString, getters adn setters (id() in this case)
    
     * hasCode is used to return a unique element that represnet the object. Here, simply the id of the node.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { // if same reference then
            return true;
        }
        if (!(o instanceof Node(int tempId))) { // start by checkin if the object comparing is a Node
            return false;
        }
        return id == tempId; // if same identification id then True
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Node(ID = " + id + ")";
    }
}
