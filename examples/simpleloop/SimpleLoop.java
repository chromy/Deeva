public class SimpleLoop {
    int field_int;
    boolean field_bool;
    short field_short;

    public enum EnumTest {
      SUNDAY, MONDAY, TUESDAY;
    }

    public void testFunc() {
      int a = 456;
      String stf = "This is a test again.";
      System.out.println(stf);
    }

    public static void main(String[] args) {
      int j = 123;
      int k = 234;
      int l = 345;
      boolean m = true;
      String s = "Hello123";
      SimpleLoop sl = new SimpleLoop();
      EnumTest et = EnumTest.SUNDAY;
      String[] hello = {"Felix is cool!", "Felix is way too cool!"};            //

      for (int i=0; i<10; i++) {
        System.out.println("i = " + i);
      }
  }

}
