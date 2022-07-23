package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

public class DJacksonCommon {

    public static final ObjectMapper COMMON_OBJECT_MAPPER = new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(SNAKE_CASE);

    public static JsonNode loadJsonFromFile(String pathPrefix, String fileName) {
        String contentAsString = loadFromFileAsString(pathPrefix + "/" + fileName);
        try {
            return COMMON_OBJECT_MAPPER.readValue(contentAsString, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loadFromFileAsString(String prefix, String filename) {
        return loadFromFileAsString(prefix + "/" + filename);
    }

    public static String loadFromFileAsString(String filename) {
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(filename)) {
            assert inputStream != null;
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode jsonTextAsJsonNode(String json) {
        try {
            return COMMON_OBJECT_MAPPER.readValue(json, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void assertFullOutputEvent(String expected, String actual) {
        try {
            JSONAssert.assertEquals(Objects.requireNonNull(jsonTextAsJsonNode(expected)).toString(),
                    Objects.requireNonNull(jsonTextAsJsonNode(actual)).toString(), JSONCompareMode.STRICT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static <T> T buildToObj(Object payload, Class<T> clazz) {
        return COMMON_OBJECT_MAPPER.convertValue(payload, clazz);
    }

    public static <T> T buildToObj(String jsonData, Class<T> clazz) {
        return buildToObj(jsonData, clazz, null);
    }
    public static <T> T buildToObj(String jsonArrData, Class<T> clazz, T defaultValue) {
        try {
            return COMMON_OBJECT_MAPPER.readValue(jsonArrData, clazz);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return defaultValue;
    }

    public static <T> String toStrJsonObj(T input) {
        try {
            return COMMON_OBJECT_MAPPER.writeValueAsString(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "{}";
    }

}
