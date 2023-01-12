package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.File;
import org.ccclll777.alldocsbackend.entity.User;

import java.nio.file.Files;
import java.util.List;

@Mapper
public interface FilesDao {
    /**
     * 插入文档
     * @param file
     * @return
     */
    int insertFile(File file);

    /**
     * 查询文档列表
     * @param limit
     * @param offset
     * @return
     */
    List<File> selectFiles(Integer limit, Integer offset);

    /**
     * 查询属于某个用户的文档列表
     * @param limit
     * @param offset
     * @param userId
     * @return
     */
    List<File> selectFilesByUserId(Integer limit, Integer offset,Integer userId);

    /**
     * 文档数量
     * @return
     */
    int fileCount();

    /**
     * 用户文档数量
     * @param userId
     * @return
     */
    int fileCountByUserId(Integer userId);

    /**
     * 根据存储文档的GridfsId查询文档
     * @param GridfsId
     * @return
     */
    File selectFileByGridfsId(String GridfsId);

    /**
     * 根据文档id查询文档
     * @param fileId
     * @return
     */
    File selectFileById(Integer fileId);

    /**
     * 删除文档
     * @param fileId
     * @return
     */
    int deleteFile(Integer fileId);

    /**
     * 更新文档状态
     * @param file
     * @return
     */
    int updateFileState(File file);

    int updateFileStateSimply(Integer fileId, Integer fileState);

    /**
     * 根据文档的md5值查询文档，看有没有重复文档
     * @param md5
     * @return
     */
    int fileCountByMd5(String md5);

    /**
     * 根据文档存储的Mongo的id查询文档
     * @param mongoFileId
     * @param fileState
     * @return
     */
    int updateFileStateByMongoFileId(String mongoFileId, Integer fileState);

    /**
     * 更新文档操作的错误信息
     * @param mongoFileId
     * @param errorMessage
     * @return
     */

    int updateErrorMessageByMongoFileId(String mongoFileId, String errorMessage);

    /**
     * 根据文档的mongoFileId更新文档的缩略图id
     * @param ThumbId
     * @param mongoFileId
     * @return
     */
    int updateThumbIdByMongoFileId(String ThumbId, String mongoFileId);

    /**
     * 查询文档详情
     * @param mongoFileId
     * @return
     */
    File selectFileByMongoFileId(String mongoFileId);

    /**
     * 查询根据时间排序的文档
     * @param limit
     * @param offset
     * @return
     */

    List<File> selectFilesOrderByTime(Integer limit, Integer offset);

    /**
     * 点击文档后更新文档点击率
     * @param fileId
     * @return
     */
    int updateHits(Integer fileId);

    /**
     * 根据点击量排序查询文档
     * @param limit
     * @param offset
     * @return
     */
    List<File> selectFilesOrderByHits(Integer limit, Integer offset);

    /**
     * 根据分类选择文档
     * @param categoryId
     * @param limit
     * @param offset
     * @return
     */
    List<File> selectFilesByCategoryId(Integer categoryId,Integer limit, Integer offset);

    /**
     * 修改文档所属的分类
     * @param categoryId
     * @param fileId
     * @return
     */
    int updateFileCategory(Integer categoryId, Integer fileId);

    /**
     * 更新文档名称
     * @param name
     * @param fileId
     * @return
     */
    int updateFileName(String name, Integer fileId);

    /**
     * 更新文档描述
     * @param description
     * @param fileId
     * @return
     */
    int updateFileDescription(String description,Integer fileId);

    /**
     * 查询某一分类下的文档数量
     * @param categoryId
     * @return
     */
    int fileCountByCategoryId(Integer categoryId);
}
