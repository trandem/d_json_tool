package dem.tool.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;
import dem.tool.diff.DJacksonCommon;

import java.util.*;

public class TestBuildObject {
    private static final Map<String, Object> inserted = new HashMap<>();
    private static Map<String, String> registerKeyMap = new HashMap<>();
    static JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex.json");

    public static void main(String[] args) {
        registerKeyMap.put("arr", "id");
        Map<String, Object> keys = new HashMap<>();
        keys.put("arr", 2);

        List<String> paths = new ArrayList<>();

        //JsonNode value1 = new TextNode("dota");
        JsonNode value1 = DJacksonCommon.jsonTextAsJsonNode("{}");
        paths.add("arr");
        paths.add("iii");
        buildStruct(keys, "lol", paths, value1);

        keys.clear();
        keys.put("arr", 1);
        buildStruct(keys, "lol1", paths, new TextNode("data"));

        System.out.println(inserted);
    }

    public static void buildStruct(Map<String, Object> keys, String fieldName, List<String> paths, JsonNode node) {
        Map<String, Object> x = findStruct(keys, inserted, paths);
        x.put(fieldName, node);

    }

    private static Map<String, Object> findStruct(Map<String, Object> keys, Map<String, Object> inserted, List<String> paths) {

        JsonNode node = afterObject;
        Map<String, Object> map = inserted;
        StringBuilder pathBuilder = new StringBuilder("");
        for (String path : paths) {
            pathBuilder.append(path).append(".");
            node = node.get(path);
            Object stored = map.get(path);
            if (stored == null) {
                if (node.getNodeType() == JsonNodeType.OBJECT) {
                    Map<String, Object> data = new HashMap<>();
                    map.put(path, data);
                    map = data;
                }

                if (node.getNodeType() == JsonNodeType.ARRAY) {

                    String keyRegister = pathBuilder.deleteCharAt(pathBuilder.length() - 1).toString();
                    String key = registerKeyMap.get(keyRegister);
                    Object data = keys.get(keyRegister);
                    List<Map<String, Object>> list = new LinkedList<>();
                    Map<String, Object> x = new HashMap<>();
                    if (data == null) {
                        return map;
                    }
                    x.put(key, data);

                    map.put(path, list);
                    list.add(x);
                    map = x;

                    for (JsonNode node1 : node) {
                        if (node1.get(key).asText().equals(data.toString())) {
                            node = node1;
                            break;
                        }
                    }

                }

            } else {
                if (node.getNodeType() == JsonNodeType.OBJECT) {
                    map = (Map<String, Object>) stored;
                }

                if (node.getNodeType() == JsonNodeType.ARRAY) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(path);

                    String keyRegister = pathBuilder.deleteCharAt(pathBuilder.length() - 1).toString();
                    String key = registerKeyMap.get(keyRegister);
                    Object data = keys.get(keyRegister);
                    if (data == null) {
                        return map;
                    }
                    boolean isFound = false;
                    Map<String, Object> foundMap = new HashMap<>();
                    for (var x : list) {
                        if (x.get(key).toString().equals(data.toString())) {
                            isFound = true;
                            foundMap = x;
                            break;
                        }
                    }

                    if (!isFound) {
                        list.add(foundMap);
                    }

                    map = foundMap;

                    for (JsonNode node1 : node) {
                        if (node1.get(key).asText().equals(data.toString())) {
                            node = node1;
                            break;
                        }
                    }

                }

            }
        }
        return map;
    }

}
