package linkedlistdemo;

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
        Scanner sc = new Scanner(System.in);
        System.out.println(
            "How many element you want the linked list to have?");
        int number = sc.nextInt();

        Node next = null;
        for(int i = number;i>0;i--) {
          Node tmp = new Node();
          tmp.setElem(i);
          tmp.setNext(next);
          next = tmp;
        }

        int sum = sum(next);

        System.out.println("The total is " + sum);
    }
}
