package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.*;

@SuppressWarnings("unchecked")
public class DiffObjectCommon {

    public static String buildPrefix(DJsonContext context) {
        List<String> paths = context.getPaths();
        Map<String, Object> valueKeyMap = context.getKeys();
        Map<String, String> registerKeyMap = context.getRegisterKeyMap();
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder prefixBuilder = new StringBuilder();
        for (var field : paths) {
            keyBuilder.append(field).append(".");
            String key = getRegisteredKey(keyBuilder);
            String valueKey = registerKeyMap.get(key);
            prefixBuilder.append(field).append(".");
            if (valueKey != null) {
                var value = valueKeyMap.get(key);
                if (value != null) {
                    prefixBuilder.append(valueKey).append(".").append(valueKeyMap.get(key).toString()).append(".");
                }
            }
        }

        return getRegisteredKey(prefixBuilder);
    }

    public static String getRegisteredKey(StringBuilder pathBuilder) {
        String keyBuilder = pathBuilder.toString();
        return keyBuilder.substring(0, keyBuilder.length() - 1);
    }


    public static void buildStruct(Map<String, Object> dataHub, DJsonContext contextStructDiff,JsonNode initialNode) {
        var x = findStruct(contextStructDiff, dataHub, initialNode);
        if (x instanceof Map) {
            ((Map<String, Object>) x).put(contextStructDiff.getField(), contextStructDiff.getValue());
        } else if (x instanceof List) {
            ((List<Map<String, Object>>) x).add(DJacksonCommon.jsonNodeToMap(contextStructDiff.getValue()));
        }
    }

    private static Object findStruct(DJsonContext contextStructDiff, Map<String, Object> inserted,JsonNode initialNode) {

        JsonNode node = initialNode;
        Map<String, Object> dataHub = inserted;
        StringBuilder pathBuilder = new StringBuilder();
        Map<String,Object> keys = contextStructDiff.getKeys();
        Map<String,String > registerKeyMap = contextStructDiff.getRegisterKeyMap();

        for (String path : contextStructDiff.getPaths()) {
            pathBuilder.append(path).append(".");
            node = node.get(path);
            Object stored = dataHub.get(path);
            if (stored == null) {
                if (node.getNodeType() == JsonNodeType.OBJECT) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    dataHub.put(path, data);
                    dataHub = data;
                    continue;
                }

                if (node.getNodeType() == JsonNodeType.ARRAY) {
                    String keyRegister = getRegisteredKey(pathBuilder);
                    String key = registerKeyMap.get(keyRegister);
                    Object data = keys.get(keyRegister);

                    dataHub = createContinueMap(dataHub, path, key, data);
                    node = getContinueJsonNode(node, key, data);
                }

            } else {
                if (node.getNodeType() == JsonNodeType.OBJECT) {
                    dataHub = (Map<String, Object>) stored;
                    continue;
                }

                if (node.getNodeType() == JsonNodeType.ARRAY) {
                    String keyRegister = getRegisteredKey(pathBuilder);
                    String key = registerKeyMap.get(keyRegister);
                    Object data = keys.get(keyRegister);

                    dataHub = getContinueMapInList(dataHub, path, key, data);
                    node = getContinueJsonNode(node, key, data);
                }
            }
        }

        if (contextStructDiff.isList()) {
            Object stored = dataHub.get(contextStructDiff.getField());
            if (stored == null) {
                List<Map<String, Object>> list = new LinkedList<>();
                dataHub.put(contextStructDiff.getField(), list);
                return list;
            } else {
                return stored;
            }
        }

        return dataHub;
    }

    private static Map<String, Object> getContinueMapInList(Map<String, Object> map, String path, String key, Object data) {
        boolean isFound = false;
        Map<String, Object> continueMap = new LinkedHashMap<>();
        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(path);
        for (var m : list) {
            if (m.get(key).toString().equals(data.toString())) {
                isFound = true;
                continueMap = m;
                break;
            }
        }
        if (!isFound) {
            continueMap.put(key, data);
            list.add(continueMap);
        }

        map = continueMap;
        return map;
    }

    private static Map<String, Object> createContinueMap(Map<String, Object> map, String path, String key, Object data) {
        Map<String, Object> continueMap = new LinkedHashMap<>();
        continueMap.put(key, data);

        List<Map<String, Object>> list = new LinkedList<>();
        map.put(path, list);
        list.add(continueMap);
        map = continueMap;
        return map;
    }

    private static JsonNode getContinueJsonNode(JsonNode node, String key, Object data) {
        for (JsonNode jsonNode : node) {
            if (jsonNode.get(key).asText().equals(data.toString())) {
                node = jsonNode;
                break;
            }
        }
        return node;
    }
}
