package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import static dem.tool.diff.DSampleJson.*;

public class DDiffStructTest {


    public static void main(String[] args) {
        Map<String, Object> update = new HashMap<>();
        Map<String, Object> delete = new HashMap<>();
        Map<String, Object> insert = new HashMap<>();

        JsonNode jsonNodeB = DJacksonCommon.jsonTextAsJsonNode(BEFORE_ARRAY_JSON_OBJ);
        JsonNode jsonNodeA = DJacksonCommon.jsonTextAsJsonNode(AFTER_ARRAY_JSON_OBJ);


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
                    throw new UnsupportedOperationException(a.getNodeType() + " is not supported here");
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
                                String prefixKey, JsonNode a, JsonNode b) {

        if (!a.isNull() && b.isNull()) {
            insert.put(prefixKey, a);
            return;
        }

        ArrayNode afterArr = (ArrayNode) a;
        ArrayNode beforeArr = (ArrayNode) b;

        if (afterArr.isEmpty()) {
            return;
        }

        if (beforeArr.isEmpty()) {
            insert.put(prefixKey, afterArr);
            return;
        }

        JsonNode afterNode = afterArr.get(0);
        if (afterNode.isValueNode()) {
            Set<String> valueAfter = new HashSet<>();
            Set<String> valueBefore = new HashSet<>();

            afterArr.forEach(x -> valueAfter.add(x.asText()));
            beforeArr.forEach(x -> valueBefore.add(x.asText()));

            List<String> valueInserted = valueAfter.stream().filter(x -> !valueBefore.contains(x)).collect(Collectors.toList());
            List<String> valueDeleted = valueBefore.stream().filter(x -> !valueAfter.contains(x)).collect(Collectors.toList());

            insert.put(prefixKey,valueInserted);
            delete.put(prefixKey,valueDeleted);
        } else if (afterNode.getNodeType() == JsonNodeType.ARRAY) {

            arrDiff(update, insert, delete, prefixKey, afterArr, beforeArr);

        } else if (afterNode.getNodeType() == JsonNodeType.OBJECT) {

            String objectKey = findAfterObjectKey(afterNode);

            isBeforeObjectHaveSameKey(beforeArr, objectKey);

            Set<String> checkedKey = new HashSet<>();
            for (var objectJson : afterArr) {

                JsonNode keyOfObject = objectJson.get(objectKey);
                if (!keyOfObject.isValueNode()) {
                    throw new UnsupportedOperationException("value of key must is value node");
                }

                boolean isBeforeArrContainObjectJson = false;
                String value = keyOfObject.asText();

                for (var objectJsonBefore : beforeArr) {

                    JsonNode keyBeforeObject = objectJsonBefore.get(objectKey);

                    if (!keyBeforeObject.isValueNode()) {
                        throw new UnsupportedOperationException("not contains valid key");
                    }
                    if (!value.equals(keyBeforeObject.asText())) {
                        continue;
                    }

                    isBeforeArrContainObjectJson = true;
                    extracted(prefixKey + "." + objectKey + "." + value + ".", update, insert, delete, objectJsonBefore, objectJson);
                }

                if (!isBeforeArrContainObjectJson) {
                    insert.put(prefixKey + "." + objectKey + "." + value, objectJson);
                }
                checkedKey.add(value);
            }

            for (var objectJsonBefore : beforeArr) {

                JsonNode keyBeforeObject = objectJsonBefore.get(objectKey);
                if (!checkedKey.contains(keyBeforeObject.asText())) {
                    delete.put(prefixKey + "." + objectKey + "." + keyBeforeObject.asText(), keyBeforeObject);
                }
            }

        } else {
            throw new UnsupportedOperationException(a.getNodeType() + " is not supported here");
        }

    }

    private static String findAfterObjectKey(JsonNode afterNode) {
        String objectKey = null;
        var fieldNames = afterNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (KEY_JSON_SET.contains(fieldName)) {
                objectKey = fieldName;
                break;
            }
        }

        if (objectKey == null) {
            throw new InvalidParameterException("not contains valid key " + KEY_JSON_SET.toString());
        }
        return objectKey;
    }

    private static void isBeforeObjectHaveSameKey(ArrayNode beforeArr, String objectKey) {
        boolean isNodeContainsValidKey = false;
        JsonNode beforeNode = beforeArr.get(0);
        var beforeFieldNames = beforeNode.fieldNames();
        while (beforeFieldNames.hasNext()) {
            String fieldName = beforeFieldNames.next();
            if (Objects.equals(fieldName, objectKey)) {
                isNodeContainsValidKey = true;
                break;
            }
        }

        if (!isNodeContainsValidKey) {
            throw new UnsupportedOperationException("not contains valid key");
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
