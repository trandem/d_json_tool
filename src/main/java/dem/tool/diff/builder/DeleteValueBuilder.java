package dem.tool.diff.builder;

import dem.tool.diff.DJsonContext;

import java.util.Map;

public interface DeleteValueBuilder {

    void build(Map<String, Object> dataHub, DJsonContext context);

}
