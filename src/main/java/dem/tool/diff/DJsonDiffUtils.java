package dem.tool.diff;

import java.util.List;
import java.util.Map;

public class DJsonDiffUtils {

    public static String buildPrefix(DJsonContext context) {
        List<String> paths = context.getPaths();
        Map<String, Object> valueKeyMap = context.getKeys();
        Map<String, String> registerKeyMap = context.getRegisterKeyMap();
        StringBuilder keyBuilder = new StringBuilder("");
        StringBuilder prefixBuilder = new StringBuilder("");
        for (var field : paths) {
            keyBuilder.append(field).append(".");
            String key = getRegisteredKey(keyBuilder);
            String valueKey = registerKeyMap.get(key);
            prefixBuilder.append(field).append(".");
            if (valueKey != null) {
                var value = valueKeyMap.get(key);
                if (value ==null){

                }else {
                    prefixBuilder.append(valueKey).append(".").append(valueKeyMap.get(key).toString()).append(".");
                }
            }
        }

        return getRegisteredKey(prefixBuilder);
    }

    public static String getRegisteredKey(StringBuilder pathBuilder) {
        String keyBuilder = pathBuilder.toString();
        return keyBuilder.substring(0, keyBuilder.length() - 1);
    }
}
