package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DJsonContext {
    private Map<String, Object> keys = new HashMap<>();
    private List<String> paths = new LinkedList<>();
    private String field = "";
    private JsonNode value = null;
    private boolean isList;
    private Map<String, String> registerKeyMap;

    public DJsonContext duplicate() {
        return DJsonContext.builder()
                .keys(new HashMap<>(keys))
                .paths(new LinkedList<>(paths))
                .field(field)
                .isList(isList)
                .registerKeyMap(registerKeyMap)
                .build();
    }

    public void addKey(String path, Object value) {
        keys.put(path, value);
    }

    public void addPath(String fieldName) {
        paths.add(fieldName);
    }

}
