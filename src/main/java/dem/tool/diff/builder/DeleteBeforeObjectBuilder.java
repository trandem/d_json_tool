package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.HashMap;
import java.util.Map;

import static dem.tool.diff.DJsonDiffUtils.buildPrefix;

public class DeleteBeforeObjectBuilder implements DeleteValueBuilder{
    private Map<String, Object> deleted = new HashMap<>();
    @Override
    public Map<String, Object> getDataHub() {
        return deleted;
    }

    @Override
    public void build(Map<String, Object> datahub, DJsonContext context) {
        String prefix = buildPrefix(context);
        datahub.put(prefix,context.getValue());
    }

    @Override
    public void build(Map<String, Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode) {
        dataHub.put(key,beforeNode);
    }
}
