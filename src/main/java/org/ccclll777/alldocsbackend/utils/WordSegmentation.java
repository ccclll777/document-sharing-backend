package org.ccclll777.alldocsbackend.utils;

import com.hankcs.hanlp.HanLP;

import java.util.ArrayList;
import java.util.List;

import static com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary.shouldInclude;

public class WordSegmentation {
    public static List<String>  cutWord(String sentence) {
        List<com.hankcs.hanlp.seg.common.Term> termList = HanLP.segment(sentence);
        List<String> wordList = new ArrayList<>();
        //是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
        for (com.hankcs.hanlp.seg.common.Term term : termList) {
            if (shouldInclude(term)) {
                wordList.add(term.word);
            }
        }
        return wordList;
    }
}
