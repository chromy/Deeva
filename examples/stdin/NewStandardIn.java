package examples.stdin;

import java.util.Scanner;

public class NewStandardIn {
    public static void main(String[] args) {
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

        System.out.println("Done with Scanner! :D");

        System.out.println("Goodbye, I think we're done here");
    }
}
