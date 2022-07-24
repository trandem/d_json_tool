package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.Getter;

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
     * - updatedMap : fields change value
     * <p>
     * - insertedMap : new fields or new values in array
     * <p>
     * - deletedMap : fields are deleted.
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
                inserted.put(prefixKey + afterNodeFieldName, afterNodeFieldValue);
                continue;
            }

            if (!afterNodeFieldValue.isValueNode()) {
                if (afterNodeFieldValue.getNodeType() == JsonNodeType.OBJECT) {
                    if (beforeNodeFieldValue.isNull()) {
                        updated.put(prefixKey + afterNodeFieldName, afterNodeFieldValue);
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
            deleted.put(prefixKey + beforeFieldName, beforeNode.get(beforeFieldName));
        }

    }

    private void arrDiff(String prefixKey, JsonNode afterArrayNode, JsonNode beforeArrayNode) {
        // must check to avoid null pointer casting beforeArrayNode
        if (!afterArrayNode.isNull() && beforeArrayNode.isNull()) {
            updated.put(prefixKey, afterArrayNode);
            return;
        }

        ArrayNode afterArr = (ArrayNode) afterArrayNode;
        ArrayNode beforeArr = (ArrayNode) beforeArrayNode;

        if (afterArr.isEmpty()) {
            return;
        }

        if (beforeArr.isEmpty()) {
            updated.put(prefixKey, afterArr);
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

            if (!valueInserted.isEmpty()) {
                inserted.put(prefixKey, valueInserted);
            }
            if (!valueDeleted.isEmpty()) {
                deleted.put(prefixKey, valueDeleted);
            }

        } else if (afterNode.getNodeType() == JsonNodeType.ARRAY) {
            arrDiff(prefixKey, afterArr, beforeArr);
        } else if (afterNode.getNodeType() == JsonNodeType.OBJECT) {
            String keyName = findAfterObjectKey(afterNode);

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
                    inserted.put(prefixKey + "." + keyName + "." + keyTextValue, afterObjectJson);
                }
                checkedKey.add(keyTextValue);
            }

            findDeletedObjectInArray(prefixKey, beforeArr, keyName, checkedKey);

        } else {
            throw new UnsupportedOperationException(afterArrayNode.getNodeType() + " is not supported here");
        }

    }

    private void findDeletedObjectInArray(String prefixKey, ArrayNode beforeArr, String keyName, Set<String> checkedKey) {
        for (var jsonObject : beforeArr) {
            JsonNode keyValue = jsonObject.get(keyName);
            if (!checkedKey.contains(keyValue.asText())) {
                deleted.put(prefixKey + "." + keyName + "." + keyValue.asText(), jsonObject);
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
        if (afterValueNode == null && !beforeValueNode.isNull()) {
            deleted.put(prefixKey, beforeValueNode);
            return;
        }

        assert afterValueNode != null;
        if (afterValueNode.isNull() && beforeValueNode.isNull()) {
            // have same value
            return;
        }
        if (afterValueNode.isNull() && !beforeValueNode.isNull()) {
            updated.put(prefixKey, afterValueNode);
            return;
        }

        if (!afterValueNode.isNull() && !beforeValueNode.isNull()) {
            var afterValue = afterValueNode.asText();
            var beforeValue = beforeValueNode.asText();
            if (!afterValue.equals(beforeValue)) {
                updated.put(prefixKey, afterValueNode);
            }
        }

        if (!afterValueNode.isNull() && beforeValueNode.isNull()) {
            updated.put(prefixKey, afterValueNode);
        }
    }
}
