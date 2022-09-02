package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import dem.tool.diff.builder.InsertFlattenKeyValueBuilder;
import dem.tool.diff.builder.UpdateFlattenKeyValueBuilder;

public class TestApp {
    private DDiffJson diffJson;

    void setUp() {
        diffJson = new DDiffJson();
        diffJson.setInsertBuilder(new InsertFlattenKeyValueBuilder());
        diffJson.setUpdateBuilder(new UpdateFlattenKeyValueBuilder());
    }

    public void runTest(String beforePath,String afterPath, String expectedPath){
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile(beforePath);
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile(afterPath);

        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();

        String expectOutput = DJacksonCommon.loadFromFileAsString(expectedPath);
        DJacksonCommon.assertFullOutputEvent(expectOutput, output);
    }

    public void registerKeyInArrayByPath(String path,String key){
        diffJson.registerObjectKeyInArrayByPath(path,key);
    }
}
