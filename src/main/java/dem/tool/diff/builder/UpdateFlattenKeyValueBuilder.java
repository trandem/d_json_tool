package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.Map;
import java.util.Set;

import static dem.tool.diff.DiffObjectCommon.buildPrefix;

public class UpdateFlattenKeyValueBuilder implements UpdateValueBuilder{

    @Override
    public void build(Map<String ,Object> dataHub,DJsonContext context) {
        String prefix = buildPrefix(context);
        dataHub.put(prefix,context.getValue());
    }

    @Override
    public void setInitialJsonNode(JsonNode jsonNode) {

    }

    @Override
    public void setIgnorePaths(Set<String> ignorePaths) {

    }
}
