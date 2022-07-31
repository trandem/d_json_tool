//package dem.tool.diff.builder;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//public class DeleteArrayKeyBuilder implements DeleteValueBuilder{
//    private final static String KEY = "keys";
//
//    @Override
//    public void build(Map<String, Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode) {
//        if (!dataHub.containsKey(KEY)){
//            dataHub.put(KEY, new LinkedList<String>());
//        }
//        ((List<String>)dataHub.get(KEY)).add(key);
//    }
//}
