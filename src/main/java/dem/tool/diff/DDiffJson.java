package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private final Map<String, String> keyRegisteredByPath;
    private final Set<String> excludeField;

    @Setter
    private InsertValueBuilder insertBuilder = new InsertObjectBuilder();
    @Setter
    private DeleteValueBuilder deleteBuilder = new DeleteFlattenKeyBuilder();
    @Setter
    private UpdateValueBuilder updateBuilder = new UpdateObjectBuilder();

    public DDiffJson() {
        updated = new LinkedHashMap<>();
        deleted = new LinkedHashMap<>();
        inserted = new LinkedHashMap<>();
        keyRegisteredByPath = new HashMap<>();
        excludeField = new HashSet<>();
    }

    public String toJsonFormatString() {
        return DJacksonCommon.toStrJsonObj(this);
    }

    public void registerObjectKeyInArrayByPath(String path, String key) {
        keyRegisteredByPath.put(path, key);
    }

    public void excludeCompareFieldPath(String fieldPath) {
        excludeField.add(fieldPath);
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
        // check ignore keypath same with id
        DJsonContext context = new DJsonContext();
        context.setRegisterKeyMap(keyRegisteredByPath);
        insertBuilder.setInitialJsonNode(afterNode);
        insertBuilder.setIgnorePaths(excludeField);
        updateBuilder.setInitialJsonNode(afterNode);
        updateBuilder.setIgnorePaths(excludeField);
        diffScan(context, beforeNode, afterNode);
    }

    private void diffScan(DJsonContext contextLoop, JsonNode beforeNode, JsonNode afterNode) {
        ArrayList<String> afterField = new ArrayList<>();
        ArrayList<String> beforeField = new ArrayList<>();

        afterNode.fieldNames().forEachRemaining(afterField::add);
        beforeNode.fieldNames().forEachRemaining(beforeField::add);

        Set<String> checkedField = new HashSet<>();

        if (afterField.size() == 0 && beforeField.size() == 0) {
            return;
        } else if (afterField.size() == 0) {
            for (String beforeFieldName : beforeField) {
                var context = contextLoop.duplicate();
                context.addPath(beforeFieldName);
                context.setValue(beforeNode.get(beforeFieldName));
                deleteBuilder.build(deleted, context);
            }
            return;
        }

        // DFS Here

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
                var contextInsert = contextLoop.duplicate();
                contextInsert.addPath(afterNodeFieldName);
                contextInsert.setValue(afterNodeFieldValue);
                insertBuilder.build(inserted, contextInsert);
                continue;
            }

            if (!afterNodeFieldValue.isValueNode()) {

                if (afterNodeFieldValue.getNodeType() == JsonNodeType.OBJECT) {
                    if (beforeNodeFieldValue.isNull()) {
                        var contextInsert = contextLoop.duplicate();
                        contextInsert.addPath(afterNodeFieldName);
                        contextInsert.setValue(afterNodeFieldValue);
                        insertBuilder.build(inserted, contextInsert);
                        continue;
                    }
                    var contextDiff = contextLoop.duplicate();
                    contextDiff.addPath(afterNodeFieldName);
                    diffScan(contextDiff, beforeNodeFieldValue, afterNodeFieldValue);

                } else if (afterNodeFieldValue.getNodeType() == JsonNodeType.ARRAY) {
                    var contextDiff = contextLoop.duplicate();
                    contextDiff.addPath(afterNodeFieldName);

                    arrDiff(contextDiff, afterNodeFieldValue, beforeNodeFieldValue);
                } else {
                    throw new UnsupportedOperationException(afterNodeFieldValue.getNodeType() + " is not supported here");
                }
            } else {
                var contextDiff = contextLoop.duplicate();
                contextDiff.addPath(afterNodeFieldName);

                valueDiff(contextDiff, afterNodeFieldValue, beforeNodeFieldValue);
            }
        }

        for (String beforeFieldName : beforeField) {
            if (checkedField.contains(beforeFieldName)) {
                continue;
            }
            var contextD = contextLoop.duplicate();
            contextD.addPath(beforeFieldName);
            contextD.setValue(beforeNode.get(beforeFieldName));
            deleteBuilder.build(deleted, contextD);
        }

    }

    private void arrDiff(DJsonContext contextDiff, JsonNode afterArrayNode, JsonNode beforeArrayNode) {

        // must check to avoid null pointer casting beforeArrayNode
        if (!afterArrayNode.isNull() && beforeArrayNode.isNull()) {
            var contextI = contextDiff.duplicate();
            contextI.setValue(afterArrayNode);
            insertBuilder.build(inserted, contextI);
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
                var contextD = contextDiff.duplicate();
                contextD.setValue(beforeArr);
                deleteBuilder.build(deleted, contextD);
            } else if (beforeNode.getNodeType() == JsonNodeType.ARRAY) {
                //TODO: implement array in array later
                throw new UnsupportedOperationException(" array of array value is not supported");
            } else if (beforeNode.getNodeType() == JsonNodeType.OBJECT) {
                var contextD = contextDiff.duplicate();
                contextD.setValue(beforeArr);
                deleteBuilder.build(deleted, contextD);
                return;
            } else {
                throw new UnsupportedOperationException(afterArrayNode.getNodeType() + " is not supported here");
            }
            return;
        }

        JsonNode afterNode = afterArr.get(0);
        if (afterNode.isValueNode()) {
            valueNodeInArrDiff(contextDiff.duplicate(), afterArr, beforeArr);
        } else if (afterNode.getNodeType() == JsonNodeType.ARRAY) {
            //TODO: implement array in array later
            throw new UnsupportedOperationException(" array of array value is not supported");
        } else if (afterNode.getNodeType() == JsonNodeType.OBJECT) {
            objectNodeInArrayDiff(contextDiff.duplicate(), afterArr, beforeArr, afterNode);
        } else {
            throw new UnsupportedOperationException(afterNode.getNodeType() + " is not supported here");
        }
    }

    private void objectNodeInArrayDiff(DJsonContext contextDiff, ArrayNode afterArr, ArrayNode beforeArr, JsonNode afterNode) {
        String keyName = findByMapPath(afterNode, contextDiff.getPaths());

        if (beforeArr.isEmpty()) {
            var contextI = contextDiff.duplicate();
            contextI.setValue(afterArr);
            insertBuilder.build(inserted, contextI);
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
                var contextScan = contextDiff.duplicate();
                var keyPath = String.join(".", contextScan.getPaths());
                contextScan.addKey(keyPath, keyTextValue);
                diffScan(contextScan, beforeObjectJson, afterObjectJson);
            }

            if (!existedAfterKeyFlag) {
                var contextI = contextDiff.duplicate();
                contextI.setList(true);
                contextI.setValue(afterObjectJson);
                var keyPath = String.join(".", contextI.getPaths());
                contextI.addKey(keyPath, keyTextValue);
                insertBuilder.build(inserted, contextI);
            }
            checkedKey.add(keyTextValue);
        }

        findDeletedObjectInArray(contextDiff.duplicate(), beforeArr, keyName, checkedKey);
    }

    private void valueNodeInArrDiff(DJsonContext contextDiff, ArrayNode afterArr, ArrayNode beforeArr) {
        Set<Object> valueAfter = new HashSet<>();
        Set<Object> valueBefore = new HashSet<>();

        afterArr.forEach(valueAfter::add);
        beforeArr.forEach(valueBefore::add);

        List<Object> valueInserted = valueAfter.stream().filter(x -> !valueBefore.contains(x)).collect(Collectors.toList());
        List<Object> valueDeleted = valueBefore.stream().filter(x -> !valueAfter.contains(x)).collect(Collectors.toList());

        if (!valueInserted.isEmpty()) {
            var node = DJacksonCommon.jsonTextAsJsonNode(DJacksonCommon.toStrJsonObj(valueInserted));
            var contextI = contextDiff.duplicate();
            contextI.setValue(node);
            insertBuilder.build(inserted, contextI);
        }
        if (!valueDeleted.isEmpty()) {
            var node = DJacksonCommon.jsonTextAsJsonNode(DJacksonCommon.toStrJsonObj(valueDeleted));
            var contextD = contextDiff.duplicate();
            contextD.setValue(node);
            deleteBuilder.build(deleted, contextD);
        }
    }

    private void findDeletedObjectInArray(DJsonContext context, ArrayNode beforeArr, String keyName, Set<String> checkedKey) {
        for (var jsonObject : beforeArr) {
            JsonNode keyValue = jsonObject.get(keyName);
            if (!checkedKey.contains(keyValue.asText())) {
                var contextD = context.duplicate();
                contextD.setValue(jsonObject);
                var keyPath = String.join(".", contextD.getPaths());
                contextD.addKey(keyPath, keyValue.asText());
                deleteBuilder.build(deleted, contextD);
            }
        }
    }

    private String findByMapPath(JsonNode afterNode, List<String> paths) {
        String path = String.join(".", paths);
        String objectKey = keyRegisteredByPath.get(path);
        if (objectKey == null) {
            return null;
        }
        var fieldNames = afterNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (objectKey.equals(fieldName)) {
                return objectKey;

            }
        }
        throw new InvalidParameterException("not contains valid key " + keyRegisteredByPath);
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
            throw new UnsupportedOperationException("not contains valid key " + objectKey);
        }
    }

    private void valueDiff(DJsonContext contextDiff, JsonNode afterValueNode, JsonNode beforeValueNode) {

        if (afterValueNode.isNull() && beforeValueNode == null) {
            // no diff
            return;
        }

        if (afterValueNode.isNull() && beforeValueNode.isNull()) {
            // have same value
            return;
        }

        if (afterValueNode.isNull() && !beforeValueNode.isNull()) {
            if (beforeValueNode.getNodeType() == JsonNodeType.ARRAY) {
                var contextD = contextDiff.duplicate();
                contextD.setValue(beforeValueNode);
                deleteBuilder.build(deleted, contextD);
            } else if (beforeValueNode.getNodeType() == JsonNodeType.OBJECT) {
                var contextD = contextDiff.duplicate();
                contextD.setValue(beforeValueNode);
                deleteBuilder.build(deleted, contextD);
            } else {
                // value is change
                var contextUpdate = contextDiff.duplicate();
                contextUpdate.setValue(afterValueNode);
                updateBuilder.build(updated, contextUpdate);
            }
            return;
        }

        if (!afterValueNode.isNull() && !beforeValueNode.isNull()) {
            var afterValue = afterValueNode.asText();
            var beforeValue = beforeValueNode.asText();
            if (!afterValue.equals(beforeValue)) {
                var contextUpdate = contextDiff.duplicate();
                contextUpdate.setValue(afterValueNode);
                updateBuilder.build(updated, contextUpdate);
            }
            return;
        }

        if (!afterValueNode.isNull() && beforeValueNode.isNull()) {
            var contextUpdate = contextDiff.duplicate();
            contextUpdate.setValue(afterValueNode);
            updateBuilder.build(updated, contextUpdate);
        }
    }
}
