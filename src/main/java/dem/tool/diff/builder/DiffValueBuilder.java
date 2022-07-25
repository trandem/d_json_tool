package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface DiffValueBuilder {

    void build(Map<String ,Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode);
}
