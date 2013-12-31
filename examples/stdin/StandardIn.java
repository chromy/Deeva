package examples.stdin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

class StandardIn {
    public static void main(String[] args) {
        BufferedReader br
                = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc = new Scanner(System.in);

        System.out.println("Please enter your name: ");
        String name = sc.next();
        System.out.println("Hi " + name + ", now enter your age: ");
        int age = sc.nextInt();
        System.out.println("So you, " + name + " are " + age + " years old");

        System.out.println("Goodbye, I think we're done here");
    }
}
