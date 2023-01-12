package org.ccclll777.alldocsbackend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.Category;
import org.ccclll777.alldocsbackend.entity.Tag;

import java.util.List;

@Mapper
public interface TagDao {

    /**
     * 插入新标签
     * @param tag
     * @return
     */
    int insertTag(Tag tag);

    /**
     * 更新标签信息
     * @param tag
     * @return
     */
    int updateTag(Tag tag);

    /**
     * 删除标签
     * @param tagId
     * @return
     */
    int deleteTag(Integer tagId);

    /**
     * 查询标签列表
     * @param limit
     * @param offset
     * @return
     */
    List<Tag> selectTagList(Integer limit, Integer offset);

    /**
     * 根据名称查询是否存在某个标签
     * @param tagName
     * @return
     */
    int haveTag(String tagName);

    /**
     * 标签数量
     * @return
     */
    int tagCount();

    /**
     * 根据标签名称查询标签
     * @param tagName
     * @return
     */
    Tag selectTagByName(String tagName);
}
