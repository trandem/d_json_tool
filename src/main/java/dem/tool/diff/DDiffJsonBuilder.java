package dem.tool.diff;

import dem.tool.diff.builder.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DDiffJsonBuilder {

    private InsertValueBuilder insertBuilder;

    private DeleteValueBuilder deleteBuilder;

    private UpdateValueBuilder updateBuilder;

    private final Map<String, String> keyRegisteredByPath = new HashMap<>();

    private final Set<String> excludeField = new HashSet<>();


    public DDiffJsonBuilder insertBuilder(InsertValueBuilder insertBuilder) {
        this.insertBuilder = insertBuilder;
        return this;
    }

    public DDiffJsonBuilder deleteBuilder(DeleteValueBuilder insertBuilder) {
        this.deleteBuilder = insertBuilder;
        return this;
    }


    public DDiffJsonBuilder updateBuilder(UpdateValueBuilder insertBuilder) {
        this.updateBuilder = insertBuilder;
        return this;
    }


    public DDiffJsonBuilder registerObjectKeyInArrayByPath(String path, String key) {
        keyRegisteredByPath.put(path, key);
        return this;
    }

    public DDiffJsonBuilder excludeCompareFieldPath(String fieldPath) {
        excludeField.add(fieldPath);
        return this;
    }

    public DDiffJson build() {
        if (insertBuilder == null) insertBuilder = new InsertObjectBuilder();
        if (updateBuilder == null) updateBuilder = new UpdateObjectBuilder();
        if (deleteBuilder == null) deleteBuilder = new DeleteFlattenKeyBuilder();
        return new DDiffJson(keyRegisteredByPath, excludeField, insertBuilder, deleteBuilder, updateBuilder);
    }
}
