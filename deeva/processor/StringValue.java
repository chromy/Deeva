package deeva.processor;

import com.google.gson.Gson;
import com.sun.jdi.StringReference;

public class StringValue extends ReferenceValue {
    private final String string;

    private StringValue(String type, long unique_id, ObjectType objectType,
                       String string) {
        super(type, unique_id, objectType);
        this.string = string;
    }

    public StringValue(String type, long unique_id, StringReference strVal) {
        this(type, unique_id, ObjectType.STRING, strVal.value());
    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        StringValue strVal = new StringValue("java.lang.String", 123,
                ObjectType.STRING, "This is a test");

        String json = gson.toJson(strVal);
        System.out.println(json);
    }
}
