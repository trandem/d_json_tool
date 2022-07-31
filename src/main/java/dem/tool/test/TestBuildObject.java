//package dem.tool.test;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.JsonNodeType;
//import com.fasterxml.jackson.databind.node.TextNode;
//import dem.tool.diff.DJacksonCommon;
//
//import java.util.*;
//
//public class TestBuildObject {
//    private static final Map<String, Object> inserted = new HashMap<>();
//    private static Map<String, String> registerKeyMap = new HashMap<>();
//    static JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex_test_builder.json");
//
//    public static void buildPrefix(Map<String, Object> valueKeyMap, List<String> paths) {
//        StringBuilder keyBuilder = new StringBuilder("");
//        StringBuilder prefixBuilder = new StringBuilder("");
//        for (var field : paths) {
//            keyBuilder.append(field).append(".");
//            String key = getRegisteredKey(keyBuilder);
//            String valueKey = registerKeyMap.get(key);
//            prefixBuilder.append(field).append(".");
//            if (valueKey != null) {
//                prefixBuilder.append(valueKey).append(".").append(valueKeyMap.get(key).toString()).append(".");
//            }
//        }
//
//        System.out.println(getRegisteredKey(prefixBuilder));
//
//    }
//
//    public static void main(String[] args) {
//        registerKeyMap.put("arr", "id");
//        registerKeyMap.put("arr.iii.add", "id");
//        Map<String, Object> keys = new HashMap<>();
//        keys.put("arr", 1);
//        keys.put("arr.iii.add", 2);
//
//        List<String> paths = new ArrayList<>();
//        paths.add("arr");
//        paths.add("iii");
//        paths.add("add");
//        buildPrefix(keys,paths);
//        //JsonNode value1 = new TextNode("dota");
//        JsonNode value1 = DJacksonCommon.jsonTextAsJsonNode("{\n" +
//                "  \"id\": 3,\n" +
//                "  \"dd\": \"demtv\"\n" +
//                "}");
//
//
//        buildStruct(keys, "lol1", paths, new TextNode("data"), false);
//        paths.clear();
//        paths.add("arr");
//        buildStruct(keys, "lol", paths, value1, true);
//
//
//        System.out.println(DJacksonCommon.toStrJsonObj(inserted));
//    }
//
//    public static void buildStruct(Map<String, Object> keys, String fieldName, List<String> paths, JsonNode node, boolean isList) {
//        var x = findStruct(keys, inserted, paths, isList);
//        if (x instanceof Map) {
//            ((Map<String, Object>) x).put(fieldName, node);
//        } else if (x instanceof List) {
//            ((List) x).add(node);
//        }
//    }
//
//    private static Object findStruct(Map<String, Object> keys, Map<String, Object> inserted, List<String> paths, boolean isList) {
//
//        JsonNode node = afterObject;
//        Map<String, Object> map = inserted;
//        StringBuilder pathBuilder = new StringBuilder("");
//
//        int size = isList ? paths.size() - 1 : paths.size();
//
//        for (int i = 0; i < size; i++) {
//            String path = paths.get(i);
//            pathBuilder.append(path).append(".");
//            node = node.get(path);
//            Object stored = map.get(path);
//            if (stored == null) {
//                if (node.getNodeType() == JsonNodeType.OBJECT) {
//                    Map<String, Object> data = new HashMap<>();
//                    map.put(path, data);
//                    map = data;
//                    continue;
//                }
//
//                if (node.getNodeType() == JsonNodeType.ARRAY) {
//                    String keyRegister = getRegisteredKey(pathBuilder);
//                    String key = registerKeyMap.get(keyRegister);
//                    Object data = keys.get(keyRegister);
//
//                    map = createContinueMap(map, path, key, data);
//                    node = getContinueJsonNode(node, key, data);
//                }
//
//            } else {
//                if (node.getNodeType() == JsonNodeType.OBJECT) {
//                    map = (Map<String, Object>) stored;
//                    continue;
//                }
//
//                if (node.getNodeType() == JsonNodeType.ARRAY) {
//                    String keyRegister = getRegisteredKey(pathBuilder);
//                    String key = registerKeyMap.get(keyRegister);
//                    Object data = keys.get(keyRegister);
//
//                    map = getContinueMapInList(map, path, key, data);
//                    node = getContinueJsonNode(node, key, data);
//                }
//            }
//        }
//
//        if (isList) {
//            Object stored = map.get(paths.get(size));
//            if (stored == null) {
//                List<Map<String, Object>> list = new LinkedList<>();
//                map.put(paths.get(size), list);
//                return list;
//            } else {
//                return stored;
//            }
//        }
//
//        return map;
//    }
//
//    private static Map<String, Object> getContinueMapInList(Map<String, Object> map, String path, String key, Object data) {
//        boolean isFound = false;
//        Map<String, Object> continueMap = new HashMap<>();
//        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(path);
//        for (var m : list) {
//            if (m.get(key).toString().equals(data.toString())) {
//                isFound = true;
//                continueMap = m;
//                break;
//            }
//        }
//        if (!isFound) {
//            list.add(continueMap);
//        }
//        map = continueMap;
//        return map;
//    }
//
//    private static Map<String, Object> createContinueMap(Map<String, Object> map, String path, String key, Object data) {
//        Map<String, Object> continueMap = new HashMap<>();
//        continueMap.put(key, data);
//
//        List<Map<String, Object>> list = new LinkedList<>();
//        map.put(path, list);
//        list.add(continueMap);
//        map = continueMap;
//        return map;
//    }
//
//    private static JsonNode getContinueJsonNode(JsonNode node, String key, Object data) {
//        for (JsonNode jsonNode : node) {
//            if (jsonNode.get(key).asText().equals(data.toString())) {
//                node = jsonNode;
//                break;
//            }
//        }
//        return node;
//    }
//
//    private static String getRegisteredKey(StringBuilder pathBuilder) {
//        String keyBuilder = pathBuilder.toString();
//        return keyBuilder.substring(0, keyBuilder.length() - 1);
//    }
//
//}
