//package com.study.kevin.teches;
//
//import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
//import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
//import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
//import org.elasticsearch.action.get.GetRequest;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.TransportAddress;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.net.InetAddress;
//import com.google.gson.JsonObject;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class TechEsApplicationTests {
//
//    private static String host = "localhost";
//    private static int port = 9300; // 端口
//
//    private TransportClient client = null;
//
//    @Before
//    public void setUp() throws Exception {
////        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
////                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
////                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));
//
////        Settings settings = Settings.builder()
////                .put("client.transport.sniff", "true")
////                .build();
////        client = new PreBuiltTransportClient(settings)
////                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
//
//        client = new PreBuiltTransportClient(Settings.EMPTY)
//                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
//    }
//
//    @After
//    public void tearDown() {
//        if (client != null) {
//            client.close();
//        }
//    }
//
//    @Test
//    public void testAdmin()
//    {
//        GetSettingsResponse response = client.admin().indices()
//                .prepareGetSettings("twitter").get();
//        for (ObjectObjectCursor<String, Settings> cursor : response.getIndexToSettings()) {
//            String index = cursor.key;
//            Settings settings = cursor.value;
//            System.out.println("index.number_of_shards:" + settings.getAsInt("index.number_of_shards", null));
//            System.out.println("index.number_of_replicas" + settings.getAsInt("index.number_of_replicas", null));
//        }
//
//        client.admin().indices().prepareCreate("twitter")
//                .setSettings(Settings.builder()
//                        .put("index.number_of_shards", 3)
//                        .put("index.number_of_replicas", 1)
//                ).get();
//
//        String source = "{\n" +
//                "  \"aliases\": {\n" +
//                "    \"dd_twitter\": {}\n" +
//                "  }, \n" +
//                "  \"mappings\" : {\n" +
//                "      \"properties\" : {\n" +
//                "        \"description\" : {\n" +
//                "          \"type\" : \"text\"\n" +
//                "        },\n" +
//                "        \"id\" : {\n" +
//                "          \"type\" : \"integer\"\n" +
//                "        },\n" +
//                "        \"partner\" : {\n" +
//                "          \"type\" : \"short\",\n" +
//                "          \"index\" : false\n" +
//                "        },\n" +
//                "        \"product_code\" : {\n" +
//                "          \"type\" : \"keyword\"\n" +
//                "        },\n" +
//                "        \"title\" : {\n" +
//                "          \"type\" : \"text\"\n" +
//                "        }\n" +
//                "      }\n" +
//                "    },\n" +
//                "    \"settings\" : {\n" +
//                "      \"index\" : {\n" +
//                "        \"number_of_shards\" : \"3\",\n" +
//                "        \"number_of_replicas\" : \"1\"\n" +
//                "      }\n" +
//                "    }\n" +
//                "}";
//
//        PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping("twitter")
//                .setType("_doc")
//                .setSource(source, XContentType.JSON)
//                .get();
//
//        System.out.println("putMappingResponse:" + putMappingResponse.isAcknowledged());
//    }
//
//    @Test
//    public void testIndex() throws Exception {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("name", "java编程思想");
//        jsonObject.addProperty("publishDate", "2012-11-11");
//        jsonObject.addProperty("price", 100);
//
//        String indexName = "book";
//        String typeName = "_doc";
//        String id = "1";
//
//        IndexResponse response = client.prepareIndex(indexName, typeName, id)
//                .setSource(jsonObject.toString(), XContentType.JSON).get();
//
//        assertEquals(indexName, response.getIndex());
//        assertEquals(typeName, response.getType());
//        assertEquals(id, response.getId());
//        System.out.println(response.status());
//
//        //GetRequest getRequest = new GetRequest().index(indexName).type(typeName).id("1");
//    }
//
//
//}
