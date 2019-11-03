package com.study.kevin.teches;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.study.kevin.teches.model.Product;
import org.apache.http.HttpHost;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONStringer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechEsRestClientProductTests {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static String host = "localhost";
    private static int port = 9300; // 端口

    private RestHighLevelClient client = null;

    @Before
    public void setUp() throws Exception {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"),
                        new HttpHost("localhost", 9202, "http")
                ));
    }

    @After
    public void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void testLowLevelRestClient() throws Exception
    {
        String index = "product";
        createIndex(index);

        Request request = new Request("GET", "/twitter/_search?pretty");
        String queryString = "{\n" +
                "  \"query\": {\"match_all\": {}}\n" +
                "}";
        request.setJsonEntity(queryString);

        try (RestClient restClient = client.getLowLevelClient())
        {
            Response response = restClient.performRequest(request);
            assertNotNull(response);

            assertTrue(200 == response.getStatusLine().getStatusCode());
            logger.info("Response {}", EntityUtils.toString(response.getEntity()));
        }
    }

    @Test
    public void testCreateDocument() throws IOException
    {
        String indexName = "product";
        createIndex(indexName);

        Product product = new Product()
                .productId(1)
                .productCode("BJ_GU_GONG")
                .setDescription("故宫是中国明清时期的皇家园林")
                .title("北京故宫")
                .setUpdateDateTime(new Date());
                ;

        Response response = createDocument(product);

        System.out.println(EntityUtils.toString(response.getEntity()));


        Request getRequest = new Request("GET", "/product/_doc/1");
        Response getResponse = client.getLowLevelClient().performRequest(getRequest);

        assertNotNull(getResponse);
        assertTrue(getResponse.getStatusLine().getStatusCode() == 200);

        System.out.println(EntityUtils.toString(getResponse.getEntity()));
    }

    private Response createDocument(Product product) throws IOException {
        String jsonString = JSON.toJSONString(product);

        System.out.println(jsonString);

        Request request = new Request("PUT", "/product/_doc/"+String.valueOf(product.getProductId()));

        request.setJsonEntity(jsonString);

        Response response = client.getLowLevelClient().performRequest(request);
        assertNotNull(response);
        assertTrue(response.getStatusLine().getStatusCode() == 201);
        return response;
    }

    private void createIndex(String index) throws IOException
    {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);

        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

        if (!exists) {
            CreateIndexRequest request = new CreateIndexRequest(index);
            request.source("{\n" +
                    "  \"aliases\": {\n" +
                    "    \"dd_product\": {}\n" +
                    "  }, \n" +
                    "  \"mappings\" : {\n" +
                    "      \"properties\" : {\n" +
                    "        \"id\" : {\n" +
                    "          \"type\" : \"integer\"\n" +
                    "        },\n" +
                    "        \"partner\" : {\n" +
                    "          \"type\" : \"short\",\n" +
                    "          \"index\" : false\n" +
                    "        },\n" +
                    "        \"product_code\" : {\n" +
                    "          \"type\" : \"keyword\"\n" +
                    "        },\n" +
                    "        \"title\" : {\n" +
                    "          \"type\": \"text\",\n" +
                    "          \"analyzer\": \"ik_max_word\",\n" +
                    "          \"search_analyzer\": \"ik_smart\"\n" +
                    "        },\n" +
                    "        \"description\" : {\n" +
                    "          \"type\" : \"text\",\n" +
                    "          \"analyzer\": \"ik_max_word\",\n" +
                    "          \"search_analyzer\": \"ik_smart\"\n" +
                    "        },\n" +
                    "        \"updatedDateTime\":\n" +
                    "        {\n" +
                    "          \"type\": \"date\",\n" +
                    "          \"format\": \"strict_date_optional_time||epoch_millis\",\n" +
                    "          \"index\": false\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"settings\" : {\n" +
                    "      \"index\" : {\n" +
                    "        \"number_of_shards\" : \"1\",\n" +
                    "        \"number_of_replicas\" : \"1\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "}", XContentType.JSON);

            CreateIndexResponse createIndexResponse1 = client.indices().create(request, RequestOptions.DEFAULT);
            assertNotNull(createIndexResponse1);

            assertTrue(createIndexResponse1.isAcknowledged());
            assertEquals(index, createIndexResponse1.index());
        }
    }

    @Test
    public void testSearch() throws InterruptedException, IOException
    {
        String index = "dd_product";

        Product product = new Product()
                .productId(2)
                .productCode("GreatWall")
                .setDescription("长城是北京最有名的建筑之一，是中华民族精神的象征")
                .title("长城")
                .setUpdateDateTime(new Date());
        ;

        createDocument(product);

        String query =  "{\n" +
                "  \"query\": {\"match\": {\n" +
                "    \"description\": \"北京\"\n" +
                "  }\n" +
                "  },\n" +
                "  \"highlight\" : {\n" +
                "        \"pre_tags\" : [\"<tag1>\", \"<tag2>\"],\n" +
                "        \"post_tags\" : [\"</tag1>\", \"</tag2>\"],\n" +
                "        \"fields\" : {\n" +
                "            \"content\" : {}\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Request getRequest = new Request("GET", "/" + index + "/_search");
        Response getResponse = client.getLowLevelClient().performRequest(getRequest);

        assertNotNull(getResponse);

        System.out.println(getResponse);


    }


}
