package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DJsonContext;

import java.util.HashMap;
import java.util.Map;

import static dem.tool.diff.DJsonDiffUtils.buildPrefix;

public class InsertPrefixKeyBuilder implements InsertValueBuilder{

    private Map<String, Object> inserted = new HashMap<>();

    @Override
    public Map<String, Object> getDataHub() {
        return inserted;
    }

    @Override
    public void build(Map<String ,Object> dataHub,DJsonContext context) {
        String prefix = buildPrefix(context);
        dataHub.put(prefix,context.getValue());
    }

    @Override
    public void setInitialJsonNode(JsonNode jsonNode) {

    }


}
