package dem.tool.test;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.DDiffJson;
import dem.tool.diff.DJacksonCommon;
import dem.tool.diff.builder.UpdateAfterValueBuilder;
import dem.tool.diff.builder.UpdateBeforeAfterValueBuilder;
import dem.tool.diff.builder.UpdateValueBuilder;

public class TestDiff {
    public static void main(String[] args) {
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("before_complex.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_complex.json");

        DDiffJson diffJson = new DDiffJson();

        UpdateValueBuilder diffValueBuilder = new UpdateAfterValueBuilder();
        diffJson.setUpdateBuilder(diffValueBuilder);
        diffJson.registerObjectKeyInArrayByPath("superAttributes","attributeCode");
        diffJson.registerObjectKeyInArrayByPath("outerArr.info.innerArr","id");

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        System.out.println(output);

    }
}
