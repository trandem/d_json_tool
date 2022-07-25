package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.builder.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;


class DDiffJsonTest {

    private DDiffJson diffJson;

    @BeforeEach
    void setUp() {
        diffJson = new DDiffJson();
    }

    @ParameterizedTest
    @CsvSource({"simple_json_sample/simple_before.json,simple_json_sample/simple_after.json,simple_json_sample/expected_simple_output.json",
            "simple_json_sample/simple_before_1.json,simple_json_sample/simple_after_1.json,simple_json_sample/expected_simple_output_1.json"})
    void should_calculate_correctly_for_simple_object(String before, String after, String expected) {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile(before);
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile(after);

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expectOutput = DJacksonCommon.loadFromFileAsString(expected);
        DJacksonCommon.assertFullOutputEvent(expectOutput, output);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_not_have_contract_and_after_have_contract_object")
    void before_not_have_contract_and_after_have_contract_object() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_not_have_contract_field.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_add_contract_object.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_missing_or_null_contract_before_output.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_contract_field_is_null_and_after_have_contract_object")
    void before_contract_null_and_after_have_contract_object() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_contract_null.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_add_contract_object.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_missing_or_null_contract_before_output.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }


    @Test
    @DisplayName("should_calculate_correctly_when_before_have_contract_field_and_after_contract_null")
    void before_have_contract_and_after_contract_null() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_have_contract.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_contract_null.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_missing_or_null_contract_after_output.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_have_contract_field_and_after_contract_missing")
    void before_have_contract_and_after_dont_have_contract() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_have_contract.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_missing_contract.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_missing_or_null_contract_after_output.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_have_contract_field_and_after_contract")
    void before_have_contract_and_after_have_contract() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_before_have_contract.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("object_json_sample/object_after_add_contract_object.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("object_json_sample/expected_before_after_have_contract_output.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void before_have_plant_array_after_plant_null() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_have_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_array_plant_null.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_before_have_plant_array_after_missing_or_null_or_empty_plant.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);

    }

    @Test
    void before_have_plant_array_after_missing_plant() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_have_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_array_missing_plant.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_before_have_plant_array_after_missing_or_null_or_empty_plant.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);

    }

    @Test
    void before_have_plant_array_after_empty_plant() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_have_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_array_empty_plant.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_before_have_plant_array_after_missing_or_null_or_empty_plant.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void should_throw_exception_when_not_register_key() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_have_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_have_array_plant.json");

        assertThrows(RuntimeException.class, () -> diffJson.diffScan(beforeObject, afterObject));
    }

    @Test
    void change_data_of_plant_array() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_have_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_have_array_plant.json");

        diffJson.registerObjectKeyInArray("plantId");
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_change_data_plant_array.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);

    }

    @Test
    void after_have_plant_array_before_plant_null() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_array_plant_null.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_have_array_plant.json");

        diffJson.registerObjectKeyInArray("plantId");
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_after_have_plant_array_before_missing_or_null_or_empty_plant.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void after_have_plant_array_before_missing_plant() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_missing_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_have_array_plant.json");

        diffJson.registerObjectKeyInArray("plantId");
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_after_have_plant_array_before_missing_or_null_or_empty_plant.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void after_have_plant_array_before_empty_plant() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_array_plant_empty.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_have_array_plant.json");

        diffJson.registerObjectKeyInArray("plantId");
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_after_have_plant_array_before_missing_or_null_or_empty_plant.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void change_value_type_array() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_array_value.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_array_value.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        String expected = DJacksonCommon.loadFromFileAsString("array_json_sample/expected_change_value_array.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void complex_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("before_complex.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex.json");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("expected_diff_complex.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void keep_deleted_key_builder_complex_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("before_complex.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex.json");

        DeleteValueBuilder diffValueBuilder = new DeleteBeforeKeyBuilder();
        diffJson.setDeleteBuilder(diffValueBuilder);
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("builder/expected_delete_keep_key_builder.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void array_deleted_key_builder_complex_json() {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("before_complex.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex.json");

        DeleteValueBuilder diffValueBuilder = new DeleteArrayKeyBuilder();
        diffJson.setDeleteBuilder(diffValueBuilder);
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("builder/expected_delete_array_builder.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void before_after_updated_builder_complex_json(){
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("before_complex.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex.json");

        UpdateValueBuilder diffValueBuilder = new UpdateBeforeAfterValueBuilder();
        diffJson.setUpdateBuilder(diffValueBuilder);
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expected = DJacksonCommon.loadFromFileAsString("builder/expected_before_after_update_builder.json");
        DJacksonCommon.assertFullOutputEvent(expected, output);
    }

    @Test
    void tiki_test(){
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("tiki-test/before_sample.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("tiki-test/after_sample.json");

        UpdateValueBuilder diffValueBuilder = new UpdateBeforeAfterValueBuilder();
        DeleteValueBuilder deleteValueBuilder = new DeleteBeforeKeyBuilder();
        diffJson.setUpdateBuilder(diffValueBuilder);
        diffJson.setDeleteBuilder(deleteValueBuilder);
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
        System.out.println(output);
    }


//    @ParameterizedTest
//    @CsvSource({"array_json_sample/array_before_3.json,array_json_sample/array_after_3.json",
//            "array_json_sample/array_before_4.json,array_json_sample/array_after_4.json",
//            "array_json_sample/array_before_5.json,array_json_sample/array_after_5.json"
//    })
//    void should_throw_exception_when_invalid_key(String before,String after){
//        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile(before);
//        JsonNode afterObject = DJacksonCommon.loadJsonFromFile(after);
//
//        diffJson.registerObjectKeyInArray("plantId");
//        Exception exception = assertThrows(RuntimeException.class, () -> {
//            diffJson.diffScan(beforeObject,afterObject);
//        });
//    }
}
