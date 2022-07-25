package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class DeleteBeforeKeyBuilder implements DeleteValueBuilder{
    @Override
    public void build(Map<String, Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode) {
        dataHub.put(key,1);
    }
}
