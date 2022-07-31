package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.Map;

public interface InsertValueBuilder{

    Map<String ,Object> getDataHub();

    void build(Map<String ,Object> dataHub,DJsonContext context);

    void setInitialJsonNode(JsonNode jsonNode);
}
