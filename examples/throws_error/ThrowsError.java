public class ThrowsError {

    public static void throwAnError() {
        System.out.println("...here it comes...");
        throw new Error("This is the error.");
    }

    public static void main(String[] args) {
        System.out.println("I'm going to throw an error...");
        throwAnError();
        System.out.println("This line never gets printed.");
    }
}
