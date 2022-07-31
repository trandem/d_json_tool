package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.Map;

public interface UpdateValueBuilder {

    void build(Map<String, Object> dataHub, DJsonContext context);

    void setInitialJsonNode(JsonNode jsonNode);
}
