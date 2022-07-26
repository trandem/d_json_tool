package dem.tool.diff.builder;

import dem.tool.diff.DJsonContext;

import java.util.Map;

import static dem.tool.diff.DiffObjectCommon.buildPrefix;

public class DeleteBeforeObjectBuilder implements DeleteValueBuilder {

    @Override
    public void build(Map<String, Object> dataHub, DJsonContext context) {
        String prefix = buildPrefix(context);
        dataHub.put(prefix, context.getValue());
    }
}
