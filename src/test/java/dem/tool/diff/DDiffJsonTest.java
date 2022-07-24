package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DDiffJsonTest {

    private DDiffJson diffJson;

    @BeforeEach
    void setUp(){
        diffJson = new DDiffJson();
    }

    @Test
    void should_match_field_when_diff_scan_simple_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("simple_json_sample/simple_before.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("simple_json_sample/simple_after.json");

        diffJson.diffScan(beforeObject,afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("simple_json_sample/expected_simple_output.json");

        DJacksonCommon.assertFullOutputEvent(expected,output);
    }

    @Test
    void should_match_field_when_diff_scan_object_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after.json");

        diffJson.diffScan(beforeObject,afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_object_output.json");

        DJacksonCommon.assertFullOutputEvent(expected,output);
    }

    @Test
    void should_match_field_when_diff_scan_object_json_before_is_null() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_2.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_2.json");

        diffJson.diffScan(beforeObject,afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_object_output_2.json");

        DJacksonCommon.assertFullOutputEvent(expected,output);
    }

    @Test
    void should_match_field_when_diff_scan_object_one_more_layer_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_1.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_1.json");

        diffJson.diffScan(beforeObject,afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_object_output_1.json");

        DJacksonCommon.assertFullOutputEvent(expected,output);
    }


    @Test
    void should_match_field_when_diff_scan_array_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/array_before.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/array_after.json");

        diffJson.registerObjectKeyInArray("employId");
        diffJson.diffScan(beforeObject,afterObject);


        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_array_output.json");

        DJacksonCommon.assertFullOutputEvent(expected,output);
    }

    @ParameterizedTest
    @CsvSource({"array_json_sample/array_before_1.json,array_json_sample/array_after_1.json,array_json_sample/expected_array_output_1.json",
            "array_json_sample/array_before_2.json,array_json_sample/array_after_2.json,array_json_sample/expected_array_output_2.json"})
    void should_match_field_when_diff_scan_empty_or_null_before_array_json(String before,String after,String expected) {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile(before);
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile(after);

        diffJson.registerObjectKeyInArray("plantId");
        diffJson.diffScan(beforeObject,afterObject);


        String output = diffJson.toJsonFormatString();

        String expectedOutput = DJacksonCommon.loadFromFileAsString(expected);

        DJacksonCommon.assertFullOutputEvent(expectedOutput,output);
    }

    @Test
    void should_throw_exception_when_not_register_key_array(){
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/array_before.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/array_after.json");

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            diffJson.diffScan(beforeObject,afterObject);
        });
        Assertions.assertEquals("not contains valid key [id, categoryId]",exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"array_json_sample/array_before_3.json,array_json_sample/array_after_3.json",
            "array_json_sample/array_before_4.json,array_json_sample/array_after_4.json",
            "array_json_sample/array_before_5.json,array_json_sample/array_after_5.json"
    })
    void should_throw_exception_when_invalid_key(String before,String after){
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile(before);
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile(after);

        diffJson.registerObjectKeyInArray("plantId");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            diffJson.diffScan(beforeObject,afterObject);
        });
    }
}
