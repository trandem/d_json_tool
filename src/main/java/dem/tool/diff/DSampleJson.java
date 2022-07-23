package dem.tool.diff;

import java.util.Set;

public class DSampleJson {

    public static String SIMPLE_BEFORE_JSON = "{\n" +
            "    \"fruit\": \"Apple\",\n" +
            "    \"size\": 12.3,\n" +
            "    \"lol\": 13,\n" +
            "    \"color\": \"Red\"\n" +
            "}\n";

    public static String SIMPLE_AFTER_JSON = "{\n" +
            "    \"fruit\": \"Apple\",\n" +
            "    \"color\": null,\n" +
            "    \"size\": 12.3,\n" +
            "    \"name\" : \"1324\"\n" +
            "}\n";


    public static String OBJECT_JSON_BEFORE = "{ \n" +
            "    \"employee\":\n" +
            "    {\n" +
            "        \"id\": \"1212\",\n" +
            "        \"fullName\":\"John Miles\",\n" +
            "        \"age\": 35\n" +
            "    },\n" +
            "    \"dem\" : 16\n" +
            "}";
    public static String OBJECT_JSON_AFTER = "{ \n" +
            "    \"employee\":\n" +
            "    {\n" +
            "        \"id\": \"1212\",\n" +
            "        \"fullName\":\"John Miles\",\n" +
            "        \"age\": 35,\n" +
            "        \"contact\":\n" +
            "        {\n" +
            "            \"email\": \"john@xyz.com\",\n" +
            "            \"phone\": \"9999999\"\n" +
            "        }\n" +
            "    }\n" +
            "}";


    public static Set<String> KEY_JSON_SET = Set.of("id", "categoryId");

    public static String BEFORE_ARRAY_JSON_OBJ = "{\n" +
            "  \"employees\": {\n" +
            "    \"employee\": [\n" +
            "      {\n" +
            "        \"id\": \"1\",\n" +
            "        \"firstName\": \"Tom\",\n" +
            "        \"lastName\": \"Cruise\",\n" +
            "        \"photo\": \"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"2\",\n" +
            "        \"firstName\": \"Maria\",\n" +
            "        \"lastName\": \"Sharapova\",\n" +
            "        \"photo\": \"https://pbs.twimg.com/profile_images/786423002820784128/cjLHfMMJ_400x400.jpg\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"3\",\n" +
            "        \"firstName\": \"James\",\n" +
            "        \"lastName\": \"Bond\",\n" +
            "        \"photo\": \"https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"number\":[\"1\",\"2\",\"3\",\"4\"]\n" +
            "}";

    public static String AFTER_ARRAY_JSON_OBJ = "{\n" +
            "  \"employees\": {\n" +
            "    \"employee\": [\n" +
            "      {\n" +
            "        \"id\": \"1\",\n" +
            "        \"firstName\": \"Tom2\",\n" +
            "        \"lastName\": \"Cruise\",\n" +
            "        \"photo\": \"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"4\",\n" +
            "        \"firstName\": \"James\",\n" +
            "        \"lastName\": \"Bond\",\n" +
            "        \"photo\": \"https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"number\":[\"1\",\"5\",\"6\"],\n" +
            "  \"demtv\":333\n" +
            "}";


}
