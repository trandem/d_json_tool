package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import dem.tool.diff.DJacksonCommon;
import dem.tool.diff.DJsonContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static dem.tool.diff.DJsonDiffUtils.getRegisteredKey;

public class InsertObjectBuilder implements InsertValueBuilder {

    private Map<String, Object> inserted = new HashMap<>();
    private Map<String, String> registerKeyMap;
    private JsonNode afterObject;

    @Override
    public Map<String, Object> getDataHub() {
        return inserted;
    }


    @Override
    public void build(Map<String, Object> dataHub, DJsonContext context) {
        this.registerKeyMap = context.getRegisterKeyMap();

        List<String> newPath = new LinkedList<>();
        for (int i = 0; i < context.getPaths().size() - 1; i++) {
            newPath.add(context.getPaths().get(i));
        }
        var fieldName = context.getPaths().get(context.getPaths().size() - 1);

        DJsonContext contextStructDiff = context.duplicate();
        contextStructDiff.setPaths(newPath);
        contextStructDiff.setField(fieldName);

        buildStruct(dataHub, context.getKeys(), fieldName, newPath, context.getValue(), context.isList());
    }

    @Override
    public void setInitialJsonNode(JsonNode jsonNode) {
        this.afterObject = jsonNode;
    }


    private void buildStruct(Map<String, Object> dataHub, Map<String, Object> keys,
                             String fieldName, List<String> paths, JsonNode value, boolean isList) {
        var x = findStruct(keys, dataHub, paths, isList, fieldName);
        if (x instanceof Map) {
            ((Map<String, Object>) x).put(fieldName, value);
        } else if (x instanceof List) {
            ((List) x).add(DJacksonCommon.jsonNodeToMap(value));
        }
    }

    private Object findStruct(Map<String, Object> keys, Map<String, Object> inserted,
                              List<String> paths, boolean isList, String fieldName) {

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
                    continue;
                }

                if (node.getNodeType() == JsonNodeType.ARRAY) {
                    String keyRegister = getRegisteredKey(pathBuilder);
                    String key = registerKeyMap.get(keyRegister);
                    Object data = keys.get(keyRegister);

                    map = createContinueMap(map, path, key, data);
                    node = getContinueJsonNode(node, key, data);
                }

            } else {
                if (node.getNodeType() == JsonNodeType.OBJECT) {
                    map = (Map<String, Object>) stored;
                    continue;
                }

                if (node.getNodeType() == JsonNodeType.ARRAY) {
                    String keyRegister = getRegisteredKey(pathBuilder);
                    String key = registerKeyMap.get(keyRegister);
                    Object data = keys.get(keyRegister);

                    map = getContinueMapInList(map, path, key, data);
                    node = getContinueJsonNode(node, key, data);
                }
            }
        }

        if (isList) {
            Object stored = map.get(fieldName);
            if (stored == null) {
                List<Map<String, Object>> list = new LinkedList<>();
                map.put(fieldName, list);
                return list;
            } else {
                return stored;
            }
        }

        return map;
    }

    private Map<String, Object> getContinueMapInList(Map<String, Object> map, String path, String key, Object data) {
        boolean isFound = false;
        Map<String, Object> continueMap = new HashMap<>();
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

    private Map<String, Object> createContinueMap(Map<String, Object> map, String path, String key, Object data) {
        Map<String, Object> continueMap = new HashMap<>();
        continueMap.put(key, data);

        List<Map<String, Object>> list = new LinkedList<>();
        map.put(path, list);
        list.add(continueMap);
        map = continueMap;
        return map;
    }

    private JsonNode getContinueJsonNode(JsonNode node, String key, Object data) {
        for (JsonNode jsonNode : node) {
            if (jsonNode.get(key).asText().equals(data.toString())) {
                node = jsonNode;
                break;
            }
        }
        return node;
    }
}
