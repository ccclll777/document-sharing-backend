package org.ccclll777.alldocsbackend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.Category;
import org.ccclll777.alldocsbackend.entity.Tag;

import java.util.List;

@Mapper
public interface TagDao {
    int insertTag(Tag tag);

    int updateTag(Tag tag);

    int deleteTag(Integer tagId);

    List<Tag> selectTagList(Integer limit, Integer offset);

    int haveTag(String tagName);

    int tagCount();
}
