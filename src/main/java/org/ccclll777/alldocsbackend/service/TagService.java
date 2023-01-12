package org.ccclll777.alldocsbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.FileTagDao;
import org.ccclll777.alldocsbackend.dao.TagDao;
import org.ccclll777.alldocsbackend.entity.Category;
import org.ccclll777.alldocsbackend.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TagService {
    @Autowired
    private TagDao tagDao;
    @Autowired
    private FileTagDao fileTagDao;
    public int insertTag(Tag tag) {
        int count = tagDao.haveTag(tag.getName());
        if(count > 0) {
            return -1;
        }
        return tagDao.insertTag(tag);
    }
    public int updateTag(Tag tag){
        return tagDao.updateTag(tag);
    }
    public int deleteTag(int tagId) {
        int count = fileTagDao.fileCountByTagId(tagId);
        if (count > 0) {
            return -2;
        }
        return tagDao.deleteTag(tagId);
    }

    public List<Tag> selectTagList(int pageNum, int pageSize) {
        int offset = pageNum * pageSize;
        return tagDao.selectTagList(pageSize,offset);
    }

    public int getTagCount() {
        return tagDao.tagCount();
    }

    public int deleteTagList(String tagIds) {
        String[] idxs = tagIds.split(",");
        if(idxs.length == 0){
            return -1;
        }
        for (String idx : idxs) {
            Integer id = Integer.parseInt(idx);
            tagDao.deleteTag(id);
        }
        return 1;
    }
}
