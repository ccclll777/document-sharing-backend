package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileTagDao {

    int insertFileTag(Integer fileId, Integer tagId);
    List<String> selectTagNameById(Integer fileId);

    int deleteFileTag(Integer fileId);

    List<Integer> selectFilesByTagId(Integer tagId);

}
