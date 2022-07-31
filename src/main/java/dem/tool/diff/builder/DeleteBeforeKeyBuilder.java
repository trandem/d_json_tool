package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.HashMap;
import java.util.Map;

import static dem.tool.diff.DJsonDiffUtils.buildPrefix;

public class DeleteBeforeKeyBuilder implements DeleteValueBuilder {
    private Map<String, Object> deleted = new HashMap<>();


    @Override
    public void build(Map<String, Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode) {
        dataHub.put(key, 1);
    }

    @Override
    public Map<String, Object> getDataHub() {
        return deleted;
    }

    @Override
    public void build(Map<String ,Object> dataHub ,DJsonContext context) {
        String prefix = buildPrefix(context);
        dataHub.put(prefix, 1);
    }
}
