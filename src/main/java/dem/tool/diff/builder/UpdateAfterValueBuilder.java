package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.HashMap;
import java.util.Map;

import static dem.tool.diff.DJsonDiffUtils.buildPrefix;

public class UpdateAfterValueBuilder implements UpdateValueBuilder{
    private Map<String, Object> updated = new HashMap<>();
    @Override
    public Map<String, Object> getDataHub() {
        return updated;
    }

    @Override
    public void build(Map<String ,Object> dataHub,DJsonContext context) {
        String prefix = buildPrefix(context);
        dataHub.put(prefix,context.getValue());
    }

    @Override
    public void build(Map<String, Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode) {
        dataHub.put(key,afterNode);
    }
}
