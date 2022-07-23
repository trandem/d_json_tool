package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.*;
import static dem.tool.diff.DSampleJson.*;

public class DDiffStruct {


    public static void main(String[] args) {
        Map<String, Object> update = new HashMap<>();
        Map<String, Object> delete = new HashMap<>();
        Map<String, Object> insert = new HashMap<>();

        JsonNode jsonNodeB = DJacksonCommon.jsonTextAsJsonNode(OBJECT_JSON_BEFORE);
        JsonNode jsonNodeA = DJacksonCommon.jsonTextAsJsonNode(OBJECT_JSON_AFTER);


        assert jsonNodeB != null;
        assert jsonNodeA != null;
        extracted("", update, insert, delete, jsonNodeB, jsonNodeA);

        System.out.println("insert : " + insert);
        System.out.println("update : " + update);
        System.out.println("delete : " + delete);

    }


    public static void extracted(String prefixkey,
                                  Map<String, Object> update, Map<String, Object> insert, Map<String, Object> delete,
                                  JsonNode jsonNodeB, JsonNode jsonNodeA) {
        ArrayList<String> afterField = new ArrayList<>();
        ArrayList<String> beforeField = new ArrayList<>();

        jsonNodeA.fieldNames().forEachRemaining(afterField::add);
        jsonNodeB.fieldNames().forEachRemaining(beforeField::add);


        // DFS Here
        List<String> checkedField = new ArrayList<>();
        ArrayDeque<String> stack = new ArrayDeque<>();
        int index = 0;
        stack.push(afterField.get(index));
        checkedField.add(afterField.get(index));

        while (!stack.isEmpty()) {
            String fieldA = stack.pop();
            // push lien ke
            if (++index < afterField.size()) {
                checkedField.add(afterField.get(index));
                stack.push(afterField.get(index));
            }

            JsonNode a = jsonNodeA.get(fieldA);
            JsonNode b = jsonNodeB.get(fieldA);

            if (!a.isNull() && b == null) {
                insert.put(prefixkey + fieldA, a);
                continue;
            }

            if (!a.isValueNode()) {
                if (a.getNodeType() == JsonNodeType.OBJECT) {
                    if (b.isNull()) {
                        insert.put(fieldA, a);
                        continue;
                    }
                    extracted(fieldA + ".", update, insert, delete, b, a);
                } else if (a.getNodeType() == JsonNodeType.ARRAY) {
                    arrDiff(update, insert, delete, fieldA, a, b);
                } else {
                    throw new UnsupportedOperationException("Array is not supported here");
                }
            } else {
                valueDiff(prefixkey + fieldA, update, delete, a, b);
            }
        }

        for (String beforeFieldName : beforeField) {
            if (checkedField.contains(beforeFieldName)) {
                continue;
            }
            delete.put(beforeFieldName, jsonNodeB.get(beforeFieldName));
        }

    }

    private static void arrDiff(Map<String, Object> update, Map<String, Object> insert, Map<String, Object> delete,
                                String fieldA, JsonNode a, JsonNode b) {

        if (!a.isNull() && b.isNull()) {
            insert.put(fieldA, a);
            return;
        }


        ArrayNode afterArr = (ArrayNode) a;
        ArrayNode beforeArr = (ArrayNode) b;

        if (afterArr.isEmpty()) {
            return;
        }

        if (beforeArr.isEmpty()) {
            insert.put(fieldA, afterArr);
            return;
        }

        String nodeKey = null;
        JsonNode afterNode = afterArr.get(0);
        var fieldNames = afterNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (KEY_JSON_SET.contains(fieldName)) {
                nodeKey = fieldName;
                break;
            }
        }

        if (nodeKey == null) {
            throw new UnsupportedOperationException("not contains valid key");
        }

        boolean isNodeContainsValidKey = false;
        JsonNode beforeNode = beforeArr.get(0);
        var beforeFieldNames = beforeNode.fieldNames();
        while (beforeFieldNames.hasNext()) {
            String fieldName = beforeFieldNames.next();
            if (Objects.equals(fieldName, nodeKey)) {
                isNodeContainsValidKey = true;
                break;
            }
        }

        if (!isNodeContainsValidKey) {
            throw new UnsupportedOperationException("not contains valid key");
        }

        for (var node : afterArr) {
            JsonNode keyValue = node.get(nodeKey);
            if (!keyValue.isValueNode()) {
                throw new UnsupportedOperationException("not contains valid key");
            }

            boolean isBeforeArrContainKeyValue = false;
            String value = keyValue.asText();

            for (var beforeNodeCompare : beforeArr) {
                JsonNode beforeKeyValue = beforeNodeCompare.get(nodeKey);
                if (!beforeKeyValue.isValueNode()) {
                    throw new UnsupportedOperationException("not contains valid key");
                }
                if (!value.equals(beforeKeyValue.asText())) {
                    continue;
                }

                isBeforeArrContainKeyValue = true;
                extracted(fieldA + "." + value + ".", update, insert, delete,  beforeNodeCompare, node);
            }

            if (!isBeforeArrContainKeyValue) {
                insert.put(fieldA + "." + value, node);
            }
        }
    }

    private static void valueDiff(String prefixKey, Map<String, Object> update, Map<String, Object> delete, JsonNode a, JsonNode b) {
        if (a == null && !b.isNull()) {
            delete.put(prefixKey, b);
            return;
        }

        assert a != null;
        if (a.isNull() && b.isNull()) {
            // khong doi
            return;
        }
        if (a.isNull() && !b.isNull()) {
            update.put(prefixKey, a);
            return;
        }

        if (!a.isNull() && !b.isNull()) {
            var x = a.asText();
            var y = b.asText();
            if (!x.equals(y)) {
                update.put(prefixKey, a);
            }
        }

        if (!a.isNull() && b.isNull()) {
            update.put(prefixKey, a);
        }

    }

}
