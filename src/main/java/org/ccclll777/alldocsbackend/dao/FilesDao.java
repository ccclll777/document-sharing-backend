package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.File;
import org.ccclll777.alldocsbackend.entity.User;

import java.nio.file.Files;
import java.util.List;

@Mapper
public interface FilesDao {

    int insertFile(File file);
    List<File> selectFiles(Integer limit, Integer offset);
    List<File> selectFilesByUserId(Integer limit, Integer offset,Integer userId);
    int fileCount();
    int fileCountByUserId(Integer userId);
    File selectFileByGridfsId(String GridfsId);
    File selectFileById(Integer fileId);

    int deleteFile(Integer fileId);

    int updateFileState(File file);

    int updateFileStateSimply(Integer fileId, Integer fileState);

    int fileCountByMd5(String md5);

    int updateFileStateByMongoFileId(String mongoFileId, Integer fileState);

    int updateErrorMessageByMongoFileId(String mongoFileId, String errorMessage);

    int updateThumbIdByMongoFileId(String ThumbId, String mongoFileId);
}
