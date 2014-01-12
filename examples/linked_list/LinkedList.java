package examples.linked_list;

public class LinkedList<T> {
    private LinkedList<T> next;
    private T elem;

    public LinkedList(T elem, LinkedList<T> next) {
        this.elem = elem;
        this.next = next;
    }

    public LinkedList<T> next() {
        return next;
    }

    public T elem() {
        return elem;
    }

    public static int sum(LinkedList<Integer> l) {
        if (l == null) {
            return 0;
        }
        return l.elem() + sum(l.next());
    }

    public static void main(String[] args) {
        LinkedList<Integer> lst = new LinkedList<Integer>(1, new LinkedList<Integer>(2, null));
        int total = sum(lst);
        System.out.println(total);
    }
}
