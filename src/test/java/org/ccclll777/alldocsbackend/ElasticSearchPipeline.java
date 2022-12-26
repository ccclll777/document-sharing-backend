package org.ccclll777.alldocsbackend;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileInputStream;
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ElasticSearchPipeline {
    @Autowired
    private RestHighLevelClient client;

    /**
     * 创建attachment管道
     * @throws Exception
     */
    @Test
    public void putAttachmentPipeline() throws Exception {
        String source = "{\n" +
                "  \"description\" : \"Extract all-docs-attachment information\",\n" +
                "  \"processors\" : [\n" +
                "    {\n" +
                "      \"attachment\" : {\n" +
                "        \"field\" : \"data\",\n" +
                "        \"properties\": [ \"content\", \"title\"  ],\n" +
                "        \"indexed_chars\": -1,\n" +
                "        \"ignore_missing\": true\n" +
                "      },\n" +
                "      \"remove\": {\n" +
                "        \"field\": \"data\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        //remove 索引之后删除
        PutPipelineRequest pipelineRequest = new PutPipelineRequest("attachment",new BytesArray(source), XContentType.JSON);

        AcknowledgedResponse acknowledgedResponse = client.ingest().putPipeline(pipelineRequest, RequestOptions.DEFAULT);
        System.out.println("创建管道成功：" + acknowledgedResponse.isAcknowledged());
    }
    @Test
    public void testSearch() throws Exception {
        String keyword = "Netty";

        SearchRequest searchRequest = new SearchRequest("all-docs-index");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword,"attachment.content","attachment.name"));
        sourceBuilder.from(0); //从第几页开始
        sourceBuilder.size(10);
        searchRequest.source(sourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("total hits: " + response.getHits().getTotalHits().value);
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }}

}
