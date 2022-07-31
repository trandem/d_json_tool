package dem.tool.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;


class DDiffJsonTest {

    private TestApp testApp;

    @BeforeEach
    void setUp() {
        testApp = new TestApp();
        testApp.setUp();
    }

    @ParameterizedTest
    @CsvSource({"simple_json_sample/simple_before.json,simple_json_sample/simple_after.json,simple_json_sample/expected_simple_output.json",
            "simple_json_sample/simple_before_1.json,simple_json_sample/simple_after_1.json,simple_json_sample/expected_simple_output_1.json"})
    void should_calculate_correctly_for_simple_object(String before, String after, String expected) {
        testApp.runTest(before, after, expected);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_not_have_contract_and_after_have_contract_object")
    void before_not_have_contract_and_after_have_contract_object() {
        var before = "object_json_sample/object_before_not_have_contract_field.json";
        var after = "object_json_sample/object_after_add_contract_object.json";
        var expected = "object_json_sample/expected_missing_or_null_contract_before_output.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_contract_field_is_null_and_after_have_contract_object")
    void before_contract_null_and_after_have_contract_object() {
        var before = "object_json_sample/object_before_contract_null.json";
        var after = "object_json_sample/object_after_add_contract_object.json";
        var expected = "object_json_sample/expected_missing_or_null_contract_before_output.json";
        testApp.runTest(before, after, expected);
    }


    @Test
    @DisplayName("should_calculate_correctly_when_before_have_contract_field_and_after_contract_null")
    void before_have_contract_and_after_contract_null() {
        var before = "object_json_sample/object_before_have_contract.json";
        var after = "object_json_sample/object_after_contract_null.json";
        var expected = "object_json_sample/expected_missing_or_null_contract_after_output.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_have_contract_field_and_after_contract_missing")
    void before_have_contract_and_after_dont_have_contract() {
        var before = "object_json_sample/object_before_have_contract.json";
        var after = "object_json_sample/object_after_missing_contract.json";
        var expected = "object_json_sample/expected_missing_or_null_contract_after_output.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    @DisplayName("should_calculate_correctly_when_before_have_contract_field_and_after_contract")
    void before_have_contract_and_after_have_contract() {
        var before = "object_json_sample/object_before_have_contract.json";
        var after = "object_json_sample/object_after_add_contract_object.json";
        var expected = "object_json_sample/expected_before_after_have_contract_output.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void before_have_plant_array_after_plant_null() {
        var before = "array_json_sample/before_have_array_plant.json";
        var after = "array_json_sample/after_array_plant_null.json";
        var expected = "array_json_sample/expected_before_have_plant_array_after_missing_or_null_or_empty_plant.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void before_have_plant_array_after_missing_plant() {
        var before = "array_json_sample/before_have_array_plant.json";
        var after = "array_json_sample/after_array_missing_plant.json";
        var expected = "array_json_sample/expected_before_have_plant_array_after_missing_or_null_or_empty_plant.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void before_have_plant_array_after_empty_plant() {
        var before = "array_json_sample/before_have_array_plant.json";
        var after = "array_json_sample/after_array_empty_plant.json";
        var expected = "array_json_sample/expected_before_have_plant_array_after_missing_or_null_or_empty_plant.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void should_throw_exception_when_not_register_key() {
        assertThrows(RuntimeException.class, () -> testApp.runTest("array_json_sample/before_have_array_plant.json",
                "array_json_sample/after_have_array_plant.json", null));
    }

    @Test
    void change_data_of_plant_array() {
        testApp.registerKeyInArrayByPath("plants", "plantId");
        var before = "array_json_sample/before_have_array_plant.json";
        var after = "array_json_sample/after_have_array_plant.json";
        var expected = "array_json_sample/expected_change_data_plant_array.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void after_have_plant_array_before_plant_null() {
        testApp.registerKeyInArrayByPath("plants", "plantId");
        var before = "array_json_sample/before_array_plant_null.json";
        var after = "array_json_sample/after_have_array_plant.json";
        var expected = "array_json_sample/expected_after_have_plant_array_before_missing_or_null_or_empty_plant.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void after_have_plant_array_before_missing_plant() {
        testApp.registerKeyInArrayByPath("plants", "plantId");
        var before = "array_json_sample/before_missing_array_plant.json";
        var after = "array_json_sample/after_have_array_plant.json";
        var expected = "array_json_sample/expected_after_have_plant_array_before_missing_or_null_or_empty_plant.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void after_have_plant_array_before_empty_plant() {
        testApp.registerKeyInArrayByPath("plants", "plantId");
        var before = "array_json_sample/before_array_plant_empty.json";
        var after = "array_json_sample/after_have_array_plant.json";
        var expected = "array_json_sample/expected_after_have_plant_array_before_missing_or_null_or_empty_plant.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void change_value_type_array() {
        testApp.registerKeyInArrayByPath("plants", "plantId");
        var before = "array_json_sample/before_array_value.json";
        var after = "array_json_sample/after_array_value.json";
        var expected = "array_json_sample/expected_change_value_array.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void complex_json() {
        testApp.registerKeyInArrayByPath("outerArr", "id");
        testApp.registerKeyInArrayByPath("outerArr.info.innerArr", "id");
        var before = "before_complex.json";
        var after = "after_complex.json";
        var expected = "expected_diff_complex.json";
        testApp.runTest(before, after, expected);
    }

    @Test
    void keep_deleted_key_builder_complex_json() {
        testApp.registerKeyInArrayByPath("outerArr", "id");
        testApp.registerKeyInArrayByPath("outerArr.info.innerArr", "id");
        var before = "before_complex.json";
        var after = "after_complex.json";
        var expected = "builder/expected_delete_keep_key_builder.json";
        testApp.runTest(before, after, expected);
    }
}
