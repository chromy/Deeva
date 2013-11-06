public class ThrowsException {

    public static void throwAnException() {
        System.out.println("...here it comes...");
        throw new Error("This is the error.");
    }

    public static void main(String[] args) {
        System.out.println("I'm going to throw an exception...");
        throwAnException();
        System.out.println("This line never gets printed.");
    }
}
