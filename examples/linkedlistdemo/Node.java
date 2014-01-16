package examples.linkedlistdemo;

public class Node {
    private Node next;
    private int elem;

    public void setElem(int elem) {
        this.elem = elem;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public int getElem() {
        return elem;
    }

    public Node getNext() {
      return next;
    }

}
