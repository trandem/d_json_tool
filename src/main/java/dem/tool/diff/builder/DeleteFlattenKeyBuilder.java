package dem.tool.diff.builder;

import dem.tool.diff.DJsonContext;

import java.util.Map;

import static dem.tool.diff.DiffObjectCommon.buildPrefix;

public class DeleteFlattenKeyBuilder implements DeleteValueBuilder {

    @Override
    public void build(Map<String, Object> dataHub, DJsonContext context) {
        String prefix = buildPrefix(context);
        if (context.getValue().get(0) != null && context.getValue().get(0).isValueNode()){
            dataHub.put(prefix, context.getValue());
        }else{
            dataHub.put(prefix, 1);
        }
    }
}
