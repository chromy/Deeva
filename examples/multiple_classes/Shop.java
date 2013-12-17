package examples.multiple_classes;

public class Shop {
    public static void main(String args[]) {
	Book book = new Book();
	double price = book.getPrice();
	System.out.println("The book has a price of: "+price);
    }
}
