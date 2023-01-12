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

    /**
     * 插入标签
     * @param tag
     * @return
     */
    public int insertTag(Tag tag) {
        int count = tagDao.haveTag(tag.getName());
        if(count > 0) {
            return -1;
        }
        return tagDao.insertTag(tag);
    }

    /**
     * 更新标签信息
     * @param tag
     * @return
     */
    public int updateTag(Tag tag){
        return tagDao.updateTag(tag);
    }

    /**
     * 删除标签
     * @param tagId
     * @return
     */
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

    /**
     * 批量删除标签
     * @param tagIds
     * @return
     */
    public int deleteTagList(String tagIds) {
        String[] idxs = tagIds.split(",");
        if(idxs.length == 0){
            return -1;
        }
        for (String idx : idxs) {
            Integer id = Integer.parseInt(idx);
            int count = fileTagDao.fileCountByTagId(id);
            if (count <= 0) {
                tagDao.deleteTag(id);
            }
        }
        return 1;
    }
}
