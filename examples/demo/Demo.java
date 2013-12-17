package examples.demo;

public class Demo {

    public static int add(int a, int b) {
        int result = a + b;
        return result;
    }

    public static int multiply(int a, int b) {
        int result = 0;
        for (int i=0; i<a; i++) {
           result = result + b; 
        }
        return result;
    }

    public static void main(String[] args) {
        int x = 5;
        int y = 10;
        int z = 13;
        int additionResult = add(x, y);
        int multiplicationResult = multiply(additionResult, z);
        System.out.println(additionResult);
        System.out.println(multiplicationResult);
    }

}
