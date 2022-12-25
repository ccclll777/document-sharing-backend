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
public class ESTest {
    @Autowired
    private RestHighLevelClient client;

    /**
     * 创建attachment管道
     * @throws Exception
     */
    @Test
    public void putAttachmentPipeline() throws Exception {
        String source = "{\n" +
                "  \"description\" : \"Extract attachment information\",\n" +
                "  \"processors\" : [\n" +
                "    {\n" +
                "      \"attachment\" : {\n" +
                "        \"field\" : \"data\",\n" +
                "        \"properties\": [ \"content\", \"title\" ],\n" +
                "        \"indexed_chars\": -1,\n" +
                "        \"ignore_missing\": true\n" +
                "      },\n" +
                "      \"remove\": {\n" +
                "        \"field\": \"data\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        PutPipelineRequest pipelineRequest = new PutPipelineRequest("attachment",new BytesArray(source), XContentType.JSON);

        AcknowledgedResponse acknowledgedResponse = client.ingest().putPipeline(pipelineRequest, RequestOptions.DEFAULT);
        System.out.println("创建管道成功：" + acknowledgedResponse.isAcknowledged());
    }
    /**
     * 将文件转成base64编码的方式上传
     * attachment管道
     */
    @Test
    public void testPutFile3() throws Exception {
//        String path = "D:\\test\\testText3.txt";
        String path = "/Users/lichao/Downloads/2.pdf";
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        //将文件转成base64编码
        String fileString = Base64Utils.encodeToString(buffer);

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("data", fileString);
            builder.field("title", file.getName());
        }
        System.out.println(fileString);
        System.out.println(file.getName());
        builder.endObject();
//        Map<String, Object> map = new HashMap<>();
//        map.put("data", buffer);
//        map.put("title", file.getName());
//        map.put("name", "测试文件上传base64转码的文件pdf格式的");
        IndexRequest indexRequest = new IndexRequest("test_file");
        indexRequest.source(builder);
//        indexRequest.source(JSON.toJSONString(map), XContentType.JSON);

        //设置文件管道attachment
        indexRequest.setPipeline("attachment");

        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index.getResult());
    }

    @Test
    public void getDoc() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("docwrite");

        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("total hits: " + search.getHits().getTotalHits().value);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
    @Test
    public void testSearch() throws Exception {
        String keyword = "netty";

        SearchRequest searchRequest = new SearchRequest("docwrite");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword,"attachment.content","attachment.title"));
        sourceBuilder.from(0); //从第几页开始
        sourceBuilder.size(10);
        searchRequest.source(sourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("total hits: " + response.getHits().getTotalHits().value);
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }}

}
