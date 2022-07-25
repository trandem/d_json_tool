package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import dem.tool.diff.builder.*;
import lombok.Getter;
import lombok.Setter;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;


public class DDiffJson {
    @Getter
    private final Map<String, Object> updated;
    @Getter
    private final Map<String, Object> deleted;
    @Getter
    private final Map<String, Object> inserted;

    private final Set<String> registeredArrayKeys;

    @Setter
    private InsertValueBuilder insertBuilder = new InsertDefaultValueBuilder();
    @Setter
    private DeleteValueBuilder deleteBuilder = new DeleteBeforeObjectBuilder();
    @Setter
    private UpdateValueBuilder updateBuilder = new UpdateAfterValueBuilder();

    public DDiffJson() {
        updated = new HashMap<>();
        deleted = new HashMap<>();
        inserted = new HashMap<>();
        registeredArrayKeys = new HashSet<>(List.of("id", "categoryId"));
    }

    public String toJsonFormatString() {
        return DJacksonCommon.toStrJsonObj(this);
    }

    /**
     * Register new keys
     * <p>
     * Example
     * <p>
     * {
     * "employee": [
     * {
     * "employeeId": "1",
     * "firstName": "Tom",
     * }
     * ]
     * }
     * <p>
     * keys = employeeId
     *
     * @param keys need to be scanned in json array object
     */
    public void registerObjectKeyInArray(String... keys) {
        registeredArrayKeys.addAll(Set.of(keys));
    }

    /**
     * Calculate :
     * <p>
     * - updated : fields is not object (objectNode, arrayNode) change value
     * <p>
     * - inserted : new fields, new values in array, new object
     * <p>
     * - deleted : object, array are null, fields are not appeared in afterNode
     * <p>
     * We use DFS algorithm to deep scan 2 json object
     *
     * @param beforeNode json of object before running logic
     * @param afterNode  json of object after running logic
     */
    public void diffScan(JsonNode beforeNode, JsonNode afterNode) {
        diffScan("", beforeNode, afterNode);
    }

    private void diffScan(String prefixKey, JsonNode beforeNode, JsonNode afterNode) {
        ArrayList<String> afterField = new ArrayList<>();
        ArrayList<String> beforeField = new ArrayList<>();

        afterNode.fieldNames().forEachRemaining(afterField::add);
        beforeNode.fieldNames().forEachRemaining(beforeField::add);

        // DFS Here
        List<String> checkedField = new ArrayList<>();
        ArrayDeque<String> stack = new ArrayDeque<>();

        int index = 0;
        stack.push(afterField.get(index));
        checkedField.add(afterField.get(index));

        while (!stack.isEmpty()) {
            String afterNodeFieldName = stack.pop();

            // push next after field to stack
            if (++index < afterField.size()) {
                checkedField.add(afterField.get(index));
                stack.push(afterField.get(index));
            }

            JsonNode afterNodeFieldValue = afterNode.get(afterNodeFieldName);
            JsonNode beforeNodeFieldValue = beforeNode.get(afterNodeFieldName);

            // inserted when after json have a new field
            if (!afterNodeFieldValue.isNull() && beforeNodeFieldValue == null) {
                insertBuilder.build(inserted, prefixKey + afterNodeFieldName, afterNodeFieldValue, beforeNodeFieldValue);
                continue;
            }

            if (!afterNodeFieldValue.isValueNode()) {

                if (afterNodeFieldValue.getNodeType() == JsonNodeType.OBJECT) {
                    if (beforeNodeFieldValue.isNull()) {
                        insertBuilder.build(inserted, prefixKey + afterNodeFieldName, afterNodeFieldValue, beforeNodeFieldValue);
                        continue;
                    }
                    diffScan(prefixKey + afterNodeFieldName + ".", beforeNodeFieldValue, afterNodeFieldValue);

                } else if (afterNodeFieldValue.getNodeType() == JsonNodeType.ARRAY) {
                    arrDiff(prefixKey + afterNodeFieldName, afterNodeFieldValue, beforeNodeFieldValue);
                } else {
                    throw new UnsupportedOperationException(afterNodeFieldValue.getNodeType() + " is not supported here");
                }
            } else {
                valueDiff(prefixKey + afterNodeFieldName, afterNodeFieldValue, beforeNodeFieldValue);
            }
        }

        for (String beforeFieldName : beforeField) {
            if (checkedField.contains(beforeFieldName)) {
                continue;
            }
            deleteBuilder.build(deleted, prefixKey + beforeFieldName, null, beforeNode.get(beforeFieldName));
        }

    }

    private void arrDiff(String prefixKey, JsonNode afterArrayNode, JsonNode beforeArrayNode) {

        // must check to avoid null pointer casting beforeArrayNode
        if (!afterArrayNode.isNull() && beforeArrayNode.isNull()) {
            insertBuilder.build(inserted, prefixKey, afterArrayNode, beforeArrayNode);
            return;
        }

        ArrayNode afterArr = (ArrayNode) afterArrayNode;
        ArrayNode beforeArr = (ArrayNode) beforeArrayNode;

        if (afterArr.isEmpty() && beforeArr.isEmpty()) {
            // have same
            return;
        }

        if (afterArr.isEmpty() && !beforeArr.isEmpty()) {
            JsonNode beforeNode = beforeArr.get(0);
            if (beforeNode.isValueNode()) {
                deleteBuilder.build(deleted, prefixKey, null, beforeArr);
            } else if (beforeNode.getNodeType() == JsonNodeType.ARRAY) {
                //TODO: implement array in array later
                throw new UnsupportedOperationException(" array of array value is not supported");
            } else if (beforeNode.getNodeType() == JsonNodeType.OBJECT) {
                deleteBuilder.build(deleted, prefixKey, null, beforeArr);
                return;
            } else {
                throw new UnsupportedOperationException(afterArrayNode.getNodeType() + " is not supported here");
            }
            return;
        }

        JsonNode afterNode = afterArr.get(0);
        if (afterNode.isValueNode()) {
            valueNodeInArrDiff(prefixKey, afterArr, beforeArr);
        } else if (afterNode.getNodeType() == JsonNodeType.ARRAY) {
            //TODO: implement array in array later
            throw new UnsupportedOperationException(" array of array value is not supported");
        } else if (afterNode.getNodeType() == JsonNodeType.OBJECT) {
            objectNodeInArrayDiff(prefixKey, afterArr, beforeArr, afterNode);
        } else {
            throw new UnsupportedOperationException(afterArrayNode.getNodeType() + " is not supported here");
        }
    }

    private void objectNodeInArrayDiff(String prefixKey, ArrayNode afterArr, ArrayNode beforeArr, JsonNode afterNode) {
        String keyName = findAfterObjectKey(afterNode);

        if (beforeArr.isEmpty()) {
            insertBuilder.build(inserted, prefixKey, afterArr, beforeArr);
            return;
        }

        isBeforeObjectHaveSameKey(beforeArr, keyName);

        Set<String> checkedKey = new HashSet<>();
        for (var afterObjectJson : afterArr) {
            JsonNode afterKeyValue = afterObjectJson.get(keyName);
            if (!afterKeyValue.isValueNode()) {
                throw new UnsupportedOperationException("value of key must is value node");
            }

            boolean existedAfterKeyFlag = false;
            String keyTextValue = afterKeyValue.asText();

            for (var beforeObjectJson : beforeArr) {
                JsonNode beforeKeyValue = beforeObjectJson.get(keyName);

                if (!beforeKeyValue.isValueNode()) {
                    throw new UnsupportedOperationException("not contains valid key");
                }
                if (!keyTextValue.equals(beforeKeyValue.asText())) {
                    continue;
                }

                existedAfterKeyFlag = true;
                diffScan(prefixKey + "." + keyName + "." + keyTextValue + ".", beforeObjectJson, afterObjectJson);
            }

            if (!existedAfterKeyFlag) {
                insertBuilder.build(inserted, prefixKey + "." + keyName + "." + keyTextValue, afterObjectJson, null);
            }
            checkedKey.add(keyTextValue);
        }

        findDeletedObjectInArray(prefixKey, beforeArr, keyName, checkedKey);
    }

    private void valueNodeInArrDiff(String prefixKey, ArrayNode afterArr, ArrayNode beforeArr) {
        Set<Object> valueAfter = new HashSet<>();
        Set<Object> valueBefore = new HashSet<>();

        afterArr.forEach(x -> valueAfter.add(x.asText()));
        beforeArr.forEach(x -> valueBefore.add(x.asText()));

        List<Object> valueInserted = valueAfter.stream().filter(x -> !valueBefore.contains(x)).collect(Collectors.toList());
        List<Object> valueDeleted = valueBefore.stream().filter(x -> !valueAfter.contains(x)).collect(Collectors.toList());

        if (!valueInserted.isEmpty()) {
            var node = DJacksonCommon.jsonTextAsJsonNode(DJacksonCommon.toStrJsonObj(valueInserted));
            insertBuilder.build(inserted, prefixKey, node, null);
        }
        if (!valueDeleted.isEmpty()) {
            var node = DJacksonCommon.jsonTextAsJsonNode(DJacksonCommon.toStrJsonObj(valueDeleted));
            deleteBuilder.build(deleted, prefixKey, null, node);
        }
    }

    private void findDeletedObjectInArray(String prefixKey, ArrayNode beforeArr, String keyName, Set<String> checkedKey) {
        for (var jsonObject : beforeArr) {
            JsonNode keyValue = jsonObject.get(keyName);
            if (!checkedKey.contains(keyValue.asText())) {
                deleteBuilder.build(deleted, prefixKey + "." + keyName + "." + keyValue.asText(), null, jsonObject);
            }
        }
    }

    private String findAfterObjectKey(JsonNode afterNode) {
        String objectKey = null;
        var fieldNames = afterNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (registeredArrayKeys.contains(fieldName)) {
                objectKey = fieldName;
                break;
            }
        }

        if (objectKey == null) {
            throw new InvalidParameterException("not contains valid key " + registeredArrayKeys.toString());
        }
        return objectKey;
    }

    private void isBeforeObjectHaveSameKey(ArrayNode beforeArr, String objectKey) {
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

    private void valueDiff(String prefixKey, JsonNode afterValueNode, JsonNode beforeValueNode) {

        if (afterValueNode.isNull() && beforeValueNode.isNull()) {
            // have same value
            return;
        }

        if (afterValueNode.isNull() && !beforeValueNode.isNull()) {
            if (beforeValueNode.getNodeType() == JsonNodeType.ARRAY) {
                deleteBuilder.build(deleted, prefixKey, null, beforeValueNode);
            } else if (beforeValueNode.getNodeType() == JsonNodeType.OBJECT) {
                deleteBuilder.build(deleted, prefixKey, null, beforeValueNode);
            } else {
                // value is change
                updateBuilder.build(updated, prefixKey, afterValueNode, beforeValueNode);
            }
            return;
        }

        if (!afterValueNode.isNull() && !beforeValueNode.isNull()) {
            var afterValue = afterValueNode.asText();
            var beforeValue = beforeValueNode.asText();
            if (!afterValue.equals(beforeValue)) {
                updateBuilder.build(updated, prefixKey, afterValueNode, beforeValueNode);
            }
            return;
        }

        if (!afterValueNode.isNull() && beforeValueNode.isNull()) {
            updateBuilder.build(updated, prefixKey, afterValueNode, beforeValueNode);
        }
    }
}
