package com.study.kevin.teches;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
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
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechEsRestClientTests {
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
    public void testCreateIndex() throws IOException
    {
        GetIndexRequest getIndexRequest = new GetIndexRequest("twitter");

        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

        if (!exists) {
            CreateIndexRequest request = new CreateIndexRequest("twitter");

            request.source("{\n" +
                    "    \"settings\" : {\n" +
                    "        \"number_of_shards\" : 1,\n" +
                    "        \"number_of_replicas\" : 0\n" +
                    "    },\n" +
                    "    \"mappings\" : {\n" +
                    "        \"properties\" : {\n" +
                    "            \"message\" : { \"type\" : \"text\" }\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"aliases\" : {\n" +
                    "        \"twitter_alias\" : {}\n" +
                    "    }\n" +
                    "}", XContentType.JSON);

            CreateIndexResponse createIndexResponse1 = client.indices().create(request, RequestOptions.DEFAULT);
            assertNotNull(createIndexResponse1);

            assertTrue(createIndexResponse1.isAcknowledged());
            assertEquals("twitter", createIndexResponse1.index());
        }
    }

    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("twitter");
        AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        assertNotNull(acknowledgedResponse);
        assertTrue(acknowledgedResponse.isAcknowledged());
    }

    @Test
    public void testCreateDocument() throws Exception {
        String index = "twitter";
        createDocument(index);

        GetRequest getRequest = new GetRequest(index, "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        assertEquals(index, getResponse.getIndex());
        assertEquals("1", getResponse.getId());
        assertEquals("1", getResponse.getId());


        UpdateRequest updateRequest = new UpdateRequest(index, "1");
        String updateJson = "{" +
                "\"postDate\":\"2013-01-31\"," +
                "\"message\":\"daily update\"" +
                "}";
        updateRequest.doc(updateJson, XContentType.JSON);
        client.update(updateRequest, RequestOptions.DEFAULT);
        GetRequest getRequest2 = new GetRequest(index, "1");
        GetResponse getResponse2 = client.get(getRequest2, RequestOptions.DEFAULT);
        assertEquals(index, getResponse2.getIndex());
        assertEquals("2013-01-31", getResponse2.getSourceAsMap().get("postDate"));
        assertEquals("daily update", getResponse2.getSourceAsMap().get("message"));


        DeleteRequest deleteRequest = new DeleteRequest(index, "1");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);

        assertEquals("1", deleteResponse.getId());
    }

    private String createDocument(String index) throws InterruptedException, IOException {

        IndexRequest request = new IndexRequest(index, "_doc", "1");
        request.timeout(TimeValue.timeValueSeconds(2));

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);

        CountDownLatch latch = new CountDownLatch(1);

        ActionListener listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                System.out.println(indexResponse.getIndex() + " " + indexResponse.getType() + " id:" + indexResponse.getId());
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        };


        client.indexAsync(request, RequestOptions.DEFAULT, listener);

        latch.await(5, TimeUnit.SECONDS);


        GetRequest getRequest1 = new GetRequest(index, "1");
        assertTrue(client.exists(getRequest1, RequestOptions.DEFAULT));
        return index;
    }

    @Test
    public void testSearch() throws InterruptedException, IOException
    {
        String index = "twitter";

        createDocument(index);

        SearchRequest searchRequest = new SearchRequest(index);
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//        searchRequest.source(searchSourceBuilder);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("user", "kimchy"));
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(searchResponse);

    }


}
