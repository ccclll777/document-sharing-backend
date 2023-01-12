package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileTagDao {
    /**
     * 插入标签
     * 
     * @param fileId
     * @param tagId
     * @return
     */

    int insertFileTag(Integer fileId, Integer tagId);

    /**
     * 查询文档的标签
     * @param fileId
     * @return
     */
    List<String> selectTagNameById(Integer fileId);

    /**
     * 删除file时，删除对应的tag
     * @param fileId
     * @return
     */
    int deleteFileTag(Integer fileId);

    /**
     * 根据tag查询文档
     * @param tagId
     * @return
     */
    List<Integer> selectFilesByTagId(Integer tagId);

    /**
     * 查询标签下对应的文档
     * @param tagId
     * @return
     */
    int fileCountByTagId(Integer tagId);

}
