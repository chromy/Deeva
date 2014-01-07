package examples.arguments;

/**
 * Created by felixdesouza on 07/01/2014.
 */
public class Arguments {
    public static void main(String[] args) {
        System.out.println("Printing out arguments");
        for (String arg : args) {
            System.out.println(arg);
        }

        System.out.println("Done Printing arguments");
    }
}
