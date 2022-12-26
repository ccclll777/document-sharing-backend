package org.ccclll777.alldocsbackend.intercepter;

import com.google.common.collect.Maps;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName SensitiveWordInit
 * @Description 过滤不雅词汇，加上@Configuration在项目启动的时候加载一下; 屏蔽敏感词初始化
 * @Author luojiarui
 * @Date 2022/8/14 16:09
 * @Version 1.0
 **/
@Configuration
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SensitiveWordInit {

    /**
     * 字符编码
     */
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * 初始化敏感字库
     * @return Map
     * @throws IOException exception
     */
    public Map initKeyWord() throws IOException {
        // 读取敏感词库 ,存入Set中
        Set<String> wordSet = readSensitiveWordFile();
        if(CollectionUtils.isEmpty(wordSet)) {
            return Maps.newHashMap();
        }
        // 将敏感词库加入到HashMap中//确定有穷自动机DFA
        return addSensitiveWordToHashMap(wordSet);
    }

    /**
     * 读取敏感词库 ,存入HashMap中
     * @return Set
     * @throws IOException ioexception
     */
    private Set<String> readSensitiveWordFile() throws IOException {
        Set<String> wordSet = null;
        ClassPathResource classPathResource = new ClassPathResource("static/censorword.txt");
        InputStream inputStream = classPathResource.getInputStream();
        //敏感词库
        try {
            // 读取文件输入流
            InputStreamReader read = new InputStreamReader(inputStream, ENCODING);
            // 文件是否是文件 和 是否存在
            wordSet = new HashSet<>();
            // BufferedReader是包装类，先把字符读到缓存里，到缓存满了，再读入内存，提高了读的效率。
            BufferedReader br = new BufferedReader(read);
            String txt = null;
            // 读取文件，将文件内容放入到set中
            while ((txt = br.readLine()) != null) {
                wordSet.add(txt);
            }
            br.close();
            // 关闭文件流
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordSet;
    }

    /**
     * 将HashSet中的敏感词,存入HashMap中
     * @param wordSet Set
     * @return Map
     */
    private Map addSensitiveWordToHashMap(Set<String> wordSet) {
        // 初始化敏感词容器，减少扩容操作
        Map wordMap = new HashMap(wordSet.size());
        for (String word : wordSet) {
            Map nowMap = wordMap;
            for (int i = 0; i < word.length(); i++) {
                // 转换成char型
                char keyChar = word.charAt(i);
                // 获取
                Object tempMap = nowMap.get(keyChar);
                // 如果存在该key，直接赋值
                if (tempMap != null) {
                    nowMap = (Map) tempMap;
                }
                // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                else {
                    // 设置标志位
                    Map<String, String> newMap = new HashMap<>(8);
                    newMap.put("isEnd", "0");
                    // 添加到集合
                    nowMap.put(keyChar, newMap);
                    nowMap = newMap;
                }
                // 最后一个
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
        return wordMap;
    }
}
