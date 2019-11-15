package com.study.kevin.teches;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.study.kevin.teches.model.Book;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechEsApplicationTests {

    private static String host = "localhost";
    private static int port = 9300; // 端口

    private TransportClient client = null;

    @Before
    public void setUp() throws Exception {
//        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));

//        Settings settings = Settings.builder()
//                .put("client.transport.sniff", "true")
//                .build();
//        client = new PreBuiltTransportClient(settings)
//                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));

        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
    }

    @After
    public void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    private void createIndex(String index) throws Exception
    {

        IndicesExistsRequest existsRequest = new IndicesExistsRequest();
        existsRequest.indices(index);

        ActionFuture<IndicesExistsResponse> actionFuture = client.admin().indices().exists(existsRequest);

        IndicesExistsResponse indicesExistsResponse = actionFuture.actionGet(1, TimeUnit.MINUTES);

        assertNotNull(indicesExistsResponse);

        if(indicesExistsResponse.isExists())
        {
            return;
        }


        client.admin().indices().prepareCreate(index)
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1)
                ).get();

        String source = "{\n" +
                "  \"aliases\": {\n" +
                "    \"book_alias\": {}\n" +
                "  }, \n" +
                "  \"mappings\": {\n" +
                "        \"properties\": {\n" +
                "            \"id\": {\n" +
                "              \"type\": \"long\"\n" +
                "            },\n" +
                "            \"sn\": {\n" +
                "              \"type\": \"keyword\"\n" +
                "            },\n" +
                "            \"price\": {\n" +
                "              \"type\": \"double\"\n" +
                "            },\n" +
                "            \"count\": {\n" +
                "              \"type\": \"long\"\n" +
                "            },\n" +
                "            \"publishDate\": {\n" +
                "              \"type\": \"date\",\n" +
                "              \"format\": \"yyyy-MM-dd hh:mm:ss\"\n" +
                "              \n" +
                "            },\n" +
                "            \"title\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\": \"ik_max_word\",\n" +
                "              \"search_analyzer\": \"ik_smart\"\n" +
                "            },\n" +
                "            \"description\": {\n" +
                "                \"type\": \"text\",\n" +
                "                \"analyzer\": \"ik_max_word\",\n" +
                "                \"search_analyzer\": \"ik_smart\"\n" +
                "            }\n" +
                "        }\n" +
                "  },\n" +
                "  \"settings\" : {\n" +
                "      \"index\" : {\n" +
                "        \"number_of_shards\" : \"3\",\n" +
                "        \"number_of_replicas\" : \"1\"\n" +
                "      }\n" +
                "    }\n" +
                "}";

        System.out.println(client.admin().indices().preparePutMapping(index)
                .setType("_doc")
                .setSource(source, XContentType.JSON)
                .get());
    }

    @Test
    public void testDocument() throws Exception {

        String indexName = "book";
        String typeName = "_doc";
        String id = "1";
        createIndex(indexName);

        Book book = new Book()
                .setBookId(1)
                .setCount(5)
                .setDescription("中国经济学")
                .setPrice(new BigDecimal("59.90"))
                .setSn("kebi-jklf-luoe-fabg")
                .setTitle("经济学指南")
                .setUpdateDateTime(new Date())
                .setCategories(Lists.newArrayList("金融", "科技", "外贸"))
                ;

//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("name", "java编程思想");
//        jsonObject.addProperty("publishDate", "2012-11-11");
//        jsonObject.addProperty("price", 100);
//        jsonObject.addProperty("id", "1");

        ObjectMapper mapper = new ObjectMapper();


        IndexResponse response = client.prepareIndex(indexName, typeName, String.valueOf(book.getBookId()))
                .setSource(mapper.writeValueAsString(book), XContentType.JSON).get();

        assertEquals(indexName, response.getIndex());
        assertEquals(typeName, response.getType());
        assertEquals(id, response.getId());
        System.out.println(response.status());

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.index(indexName).type(typeName);
        deleteRequest.id(String.valueOf(book.getBookId()));

        CountDownLatch latch = new CountDownLatch(1);
        ActionListener<DeleteResponse> actionListener = new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                System.out.println(deleteResponse.getIndex());
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        };
        client.delete(deleteRequest, actionListener);
        latch.await(5, TimeUnit.SECONDS);
        assertTrue(latch.getCount() == 0);
    }


}
