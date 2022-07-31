package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;
import dem.tool.diff.DiffObjectCommon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AbstractObjectBuilder implements UpdateValueBuilder,InsertValueBuilder{
    private JsonNode afterObject;

    @Override
    public void build(Map<String, Object> dataHub, DJsonContext context) {
        List<String> newPath = new LinkedList<>();
        for (int i = 0; i < context.getPaths().size() - 1; i++) {
            newPath.add(context.getPaths().get(i));
        }
        var fieldName = context.getPaths().get(context.getPaths().size() - 1);

        DJsonContext contextStructDiff = context.duplicate();
        contextStructDiff.setPaths(newPath);
        contextStructDiff.setField(fieldName);
        contextStructDiff.setValue(context.getValue());

        DiffObjectCommon.buildStruct(dataHub, contextStructDiff, afterObject);
    }

    @Override
    public void setInitialJsonNode(JsonNode jsonNode) {
        this.afterObject = jsonNode;
    }
}
