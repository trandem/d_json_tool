package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.Map;

public interface DeleteValueBuilder {

    Map<String ,Object> getDataHub();
    void build(Map<String ,Object> datahub,DJsonContext context);
    void build(Map<String ,Object> dataHub, String key, JsonNode afterNode,  JsonNode beforeNode);


}
