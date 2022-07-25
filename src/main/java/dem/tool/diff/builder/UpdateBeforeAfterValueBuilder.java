package dem.tool.diff.builder;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

public class UpdateBeforeAfterValueBuilder implements UpdateValueBuilder {
    @Override
    public void build(Map<String, Object> dataHub, String key, JsonNode afterNode, JsonNode beforeNode) {
        dataHub.put(key, new BeforeAfterValue(beforeNode,afterNode));
    }

    @Data
    @AllArgsConstructor
    public static class BeforeAfterValue {
        private Object before;
        private Object after;
    }
}
