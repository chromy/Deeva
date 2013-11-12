package multiple_classes;

public class Book {
    private double DEFAULT_PRICE;
    public Book() {
	DEFAULT_PRICE = 10.99;
    }

    public double getPrice() {
	return DEFAULT_PRICE;
    }
    
}
