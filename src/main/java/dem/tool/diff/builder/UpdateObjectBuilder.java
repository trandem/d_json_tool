package dem.tool.diff.builder;

import dem.tool.diff.DJsonContext;

import java.util.Map;

public class UpdateObjectBuilder extends AbstractObjectBuilder {
    @Override
    public void build(Map<String, Object> dataHub, DJsonContext context) {
        var nodePath = String.join(".", context.getPaths());
        if (excludeField.contains(nodePath)) {
            return;
        }

        super.build(dataHub, context);
    }

}
