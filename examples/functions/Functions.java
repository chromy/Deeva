public class Functions {

    public static String add(String a, String b) {
        String result = a + b;
        return result;
    }

    public static String multiply(String a, int n) {
        String result = "";
        for (int i=0; i<n; i++) {
           result = result + a; 
        }
        return result;
    }

    public static int count(String a, char c) {
        int count = 0;
        for (int i=0; i<a.length(); i++) {
           if (a.charAt(i) == c) {
               count++;
           }
        }
        return count;
    }

    public static void main(String[] args) {
        String letterA = "a";
        String letterB = "b";
        String newStr;
        int aCount;
        newStr = add(letterA, letterB);
        newStr = multiply(newStr, 5);
        aCount = count(newStr, 'a');
        System.out.println(newStr);
        System.out.println(aCount);
    }

}
