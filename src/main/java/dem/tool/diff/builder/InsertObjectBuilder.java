package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dem.tool.diff.DJsonContext;

import java.util.*;

public class InsertObjectBuilder extends AbstractObjectBuilder {

    @Override
    public void build(Map<String, Object> dataHub, DJsonContext context) {
        var node = clearIgnoreField(context.getValue(),context);
        context.setValue(node);
        super.build(dataHub, context);
    }

    private JsonNode clearIgnoreField(JsonNode node, DJsonContext context) {
        if (node.isValueNode()) {
            return node;
        }
        if (node.isArray()) {
            var arrNode = (ArrayNode) node;
            for (var x : arrNode) {
                if (x.isArray()){
                    throw new RuntimeException("array in array is not support");
                }
                clearIgnoreField(x, context);
            }
            return node;
        }
        var objectNode = (ObjectNode) node;
        var nodePath = String.join(".", context.getPaths());

        Map<String,String> fieldPaths = new HashMap();

        objectNode.fieldNames().forEachRemaining(fieldName -> {
            var value = nodePath.isEmpty()? nodePath: nodePath+".";
            fieldPaths.put(fieldName,value + fieldName);
        });
        for (var entry : fieldPaths.entrySet()) {
            if (excludeField.contains(entry.getValue())) {
                objectNode = objectNode.without(entry.getKey());
                continue;
            }
            if (!objectNode.get(entry.getKey()).isValueNode()) {
                var contextL = context.duplicate();
                contextL.addPath(entry.getKey());
                clearIgnoreField(objectNode.get(entry.getKey()), contextL);
            }
        }
        return objectNode;
    }
}
