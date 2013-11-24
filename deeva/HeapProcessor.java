package deeva;

import com.sun.jdi.*;

import java.util.List;
import java.util.HashMap;

class HeapProcessor {
    public static HashMap<String, ? extends Object> process(StringReference stringRef) {
	HashMap<String, Object> strRefMeta = new HashMap<String, Object>();
	
	strRefMeta.put("name", "");
	strRefMeta.put("value", stringRef.value());
	strRefMeta.put("object_type", "string");
	strRefMeta.put("unique_id", stringRef.uniqueID());
	strRefMeta.put("type", stringRef.type().name());
	return strRefMeta;
    }

    public static HashMap<String, ? extends Object> process(ObjectReference objRef) {
	HashMap<String, Object> objRefMeta = new HashMap<String, Object>();
	HashMap<String, Object> fields = new HashMap<String, Object>();
	
	return objRefMeta;
    }

    public static HashMap<String, ? extends Object> process(ArrayReference arrRef) {
	HashMap<String, Object> arrRefMeta = new HashMap<String, Object>();
	return arrRefMeta;
    }
}
