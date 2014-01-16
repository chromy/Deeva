package examples.linkedlistdemo;

import java.util.Scanner;

public class LinkedList {

    public static int sum(Node head) {
        if (head == null) {
            return 0;
        }
        return head.getElem() + sum(head.getNext());
    }

    public static void main(String[] args) {

        /* Creating Linked List With a Scanner */
        /*Scanner sc = new Scanner(System.in);
        System.out.println(
            "How many element you want the linked list to have?");
        int number = sc.nextInt();*/
        int x = 3;
        int y = 5;
        Node next = null;
        for(int i = 3;i>0;i--) {
          Node tmp = new Node();
          tmp.setElem(i);
          tmp.setNext(next);
          next = tmp;
        }
        System.out.println("The total is " + sum(next));
    }
}
