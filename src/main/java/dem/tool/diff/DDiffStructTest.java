package dem.tool.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import static dem.tool.diff.DSampleJson.*;

public class DDiffStructTest {

    public static void main(String[] args) {

        JsonNode jsonNodeB = DJacksonCommon.jsonTextAsJsonNode(BEFORE_ARRAY_JSON_OBJ);
        JsonNode jsonNodeA = DJacksonCommon.jsonTextAsJsonNode(AFTER_ARRAY_JSON_OBJ);


        assert jsonNodeB != null;
        assert jsonNodeA != null;

        DDiffJson diffJson = new DDiffJson();
        diffJson.extracted(jsonNodeB,jsonNodeA);

        String jsonOut = DJacksonCommon.toStrJsonObj(diffJson);
        System.out.println(jsonOut);

    }

}
