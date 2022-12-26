package org.ccclll777.alldocsbackend.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import org.apache.commons.compress.utils.Lists;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.FileObj;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ElasticService {

    private static final String INDEX_NAME = "all-docs-index";

    private static final String PIPELINE_NAME = "attachment";
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private FileService fileService;

    /**
     * 有三种类型
     * 1.文件的名字
     * 2.文件type
     * 3.文件的data 64编码
     */
    public void upload(FileObj file) throws IOException {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        //上传同时，使用attachment pipeline 进行提取文件
        String fileJson = (JSON.toJSONString(file));
        indexRequest.source(fileJson, XContentType.JSON);
        indexRequest.setPipeline(PIPELINE_NAME);
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
    }
//    public void upload(FileObj file) throws IOException {
//        XContentBuilder builder = XContentFactory.jsonBuilder();
//        builder.startObject();
//        {
//            builder.field("data", file.getContent());
//            builder.field("title", file.getName());
//        }
//        builder.endObject();
//        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
//        indexRequest.source(builder);
//        //设置文件管道attachment
//        indexRequest.setPipeline(PIPELINE_NAME);
//        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
//    }


    /**
     * 根据关键词，搜索对应的文件信息
     * 查询文件中的文本内容
     * 默认会search出所有的东西来
     * SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
     * <p>
     * // srb.query(QueryBuilders.matchQuery("attachment.content", keyword).analyzer("ik_smart"));
     *
     * @param keyword String
     * @return list
     * @throws IOException ioexception
     */
    public List<FileDocument> search(String keyword) throws IOException {
        List<FileDocument> fileDocumentList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        // 使用lk分词器查询，会把插入的字段分词，然后进行处理
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword,"attachment.content","attachment.name","attachment.id","attachment.type"));
        sourceBuilder.from(0); //从第几页开始
        sourceBuilder.size(10); // 每页10个数据
        searchRequest.source(sourceBuilder);
        //设置highlighting
        // 高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<em>"); // 高亮前缀
        highlightBuilder.postTags("</em>"); // 高亮后缀
        highlightBuilder.fields().add(new HighlightBuilder.Field("attachment.content")); // 高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("attachment.name"));
        //highlighting会自动返回匹配到的文本，所以就不需要再次返回文本了
        String[] includeFields = new String[]{"name", "id"};
        String[] excludeFields = new String[]{PIPELINE_NAME};
        sourceBuilder.fetchSource(includeFields, excludeFields);
        //把刚才设置的值导入进去
        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse res = client.search(searchRequest, RequestOptions.DEFAULT);
        if (res == null || res.getHits() == null) {
            return Lists.newArrayList();
        }
        //获取hits，这样就可以获取查询到的记录了
        SearchHits hits = res.getHits();
        //hits是一个迭代器，所以需要迭代返回每一个hits
        Iterator<SearchHit> iterator = hits.iterator();
        int count = 0;
        Set<String> idSet = Sets.newHashSet();

        while (iterator.hasNext()) {
            SearchHit hit = iterator.next();
            //获取返回的字段
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //统计找到了几条
            count++;
            //这个就会把匹配到的文本返回，而且只返回匹配到的部分文本
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("attachment.content");
            StringBuilder stringBuilder1 = new StringBuilder();
            for (Text fragment : highlightField.getFragments()) {
                stringBuilder1.append(fragment.toString());
            }
            String abstractString = stringBuilder1.toString();
            if (abstractString.length() > 500) {
                abstractString = abstractString.substring(0, 500);
            }
            if (sourceAsMap.containsKey("id")) {
                String id = (String) sourceAsMap.get("id");
                if (id != null && !idSet.contains(id)) {
                    idSet.add(id);
                    FileDocument fileDocument = fileService.getByMd5(id);
                    if (fileDocument == null) {
                        continue;
                    }
                    fileDocument.setDescription(abstractString);
                    fileDocumentList.add(fileDocument);
                }
            }
        }
        return fileDocumentList;
    }

    /**
     * 查询的自动补全
     * @param keyWord
     * @return
     */
    public List<String>  searchSuggestion(String keyWord)  throws IOException
    {
        //1 创建request请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
//        SuggestBuilder suggestBuilder = new SuggestBuilder();
        //2 准备DSL
        searchRequest.source().suggest(new SuggestBuilder()
                .addSuggestion(
                        "suggestions",
                        SuggestBuilders.completionSuggestion("suggestion")
                                .prefix(keyWord)
                                .skipDuplicates(true)
                                .size(10)
                ));
        //3.发起请求
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        //4.解析结果
        Suggest suggest = response.getSuggest();
        //4.1根据补全查询名称，获取补全结果
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        //4.2获取options
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        //4.3遍历
        List<String> suggestResult = new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option: options) {
            String text = option.getText().toString();
            suggestResult.add(text);
        }
        return suggestResult;
    }


}
