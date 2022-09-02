package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.Map;
import java.util.Set;

public interface InsertValueBuilder{

    void build(Map<String ,Object> dataHub,DJsonContext context);

    void setInitialJsonNode(JsonNode jsonNode);

    void setIgnorePaths(Set<String > ignorePaths);
}
