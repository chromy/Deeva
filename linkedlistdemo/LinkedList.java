package linkedlistdemo;

import java.util.Scanner;

public class LinkedList<T> {
    private LinkedList<T> next;
    private T elem;

    public void setElem(T elem) {
        this.elem = elem;
    }
    
    public void setNext(LinkedList<T> next) {
        this.next = next;
    }

    public T elem() {
        return elem;
    }

    private LinkedList<T> next() {
      return next;
    }
    
    public static int sum(LinkedList<Integer> l) {
        if (l == null) {
            return 0;
        }
        return l.elem() + sum(l.next());
    }

    public static void main(String[] args) {

        /* Creating Linked List With a Scanner */
        Scanner sc = new Scanner(System.in);
        System.out.println(
            "How many element you want the linked list to have?");
        int number = sc.nextInt();

        LinkedList<Integer> next = null;
        for(int i = number;i>0;i--) {
          LinkedList<Integer> tmp = new LinkedList<Integer>();
          tmp.setElem(i);
          tmp.setNext(next);
          next = tmp;
        }
        System.out.println("The total is " + sum(next));
    }
}
