package examples.stdin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

class StandardIn {
    public static void main(String[] args) {

        /* With a Buffered Reader */
        BufferedReader br
                = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Please enter your name: ");
            String name = br.readLine();
            System.out.println("Hi " + name + ", now enter your age: ");
            String age = br.readLine();
            System.out.println("So you, " + name + " are " + age + " years old");
        } catch (Exception e) {

        }

        System.out.println("Done with BufferedReader! :D");

        /* With a Scanner */
        Scanner sc = new Scanner(System.in);

        System.out.println("Please enter a line");
        String line = sc.nextLine();
        System.out.println("This was your line: ");
        System.out.println(line);
        System.out.println();

        System.out.println("Please enter your favourite sport: ");
        String sport = sc.next();
        System.out.println("So you like " + sport + ", huh?");
        System.out.println();

        System.out.println("Please enter your favourite number: ");
        int number = sc.nextInt();
        System.out.println("Your favourite number is: " + number);

        System.out.println("Done wtih Scanner! :D");

        System.out.println("Goodbye, I think we're done here");
    }
}
