public class Adder {

    public Adder() {
    }

    public int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println("Adding 1 and 2.");
        Adder a = null;
        System.out.println("1 + 2 = " + a.add(1, 2));
    }
}
