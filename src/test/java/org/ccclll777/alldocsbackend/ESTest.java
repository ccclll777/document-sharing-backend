package org.ccclll777.alldocsbackend;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.service.ElasticService;
import org.ccclll777.alldocsbackend.utils.WordSegmentation;
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
import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ESTest {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    ElasticService elasticService;

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
        String keyword = "疫情";

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

    @Test
    public void search() throws Exception {
        String keyWord = "居家";
        List<FileDocument> esDoc  = elasticService.search(keyWord);
        for(FileDocument fileDocument : esDoc) {
            System.out.println(fileDocument.getDescription());
        }

//        System.out.println(esDoc.get(0).getContent());

    }
    @Test
    public void searchSuggest() throws Exception {
        String keyWord = "原";
        List<String> suggestions  = elasticService.searchSuggestion(keyWord);
        for(String s : suggestions) {
            System.out.println(s);
        }

//        System.out.println(esDoc.get(0).getContent());

    }

    @Test
    public void cutWord(){
        for(String word : WordSegmentation.cutWord("杨尚川是APDPlat应用级产品开发平台的作者")){
            System.out.println(word);
        }
    }
}
