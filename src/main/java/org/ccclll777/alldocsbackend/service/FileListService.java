package org.ccclll777.alldocsbackend.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Maps;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.ccclll777.alldocsbackend.dao.*;
import org.ccclll777.alldocsbackend.entity.File;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.FileTag;
import org.ccclll777.alldocsbackend.entity.Tag;
import org.ccclll777.alldocsbackend.entity.vo.FilesVO;
import org.ccclll777.alldocsbackend.entity.vo.SearchFilesVO;
import org.ccclll777.alldocsbackend.enums.FileStateEnum;
import org.ccclll777.alldocsbackend.task.exception.TaskRunException;
import org.ccclll777.alldocsbackend.utils.ByteConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileListService {
    @Autowired
    private FilesDao filesDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private FileTagDao fileTagDao;
    @Autowired
    private FileListService fileService;

    public List<Map<String, Object>> selectFilesRecently( int pageSize) {
        List<File> files = filesDao.selectFilesOrderByTime(pageSize,0);
        List<Map<String, Object>> result = Lists.newArrayList();
        List<Map<String, Object>> recentMap = fileToMap(files);
        result.add(getTagMap("最近的文档", -1, recentMap));

        List<Tag> tags = tagDao.selectTagList(100,0);
        for (Tag tag : tags) {
            //当前tag下对应的file
            List<Integer> fileIds = fileTagDao.selectFilesByTagId(tag.getId());
            List<File> tagFiles = new ArrayList<>();
            for (int fileId : fileIds) {
                File file = filesDao.selectFileById(fileId);
                tagFiles.add(file);

            }
            recentMap = fileToMap(tagFiles);
            result.add(getTagMap(tag.getName(), tag.getId(), recentMap));
        }
        return result;
    }

    /**
     * 返回生成的数据
     * @param name
     * @param tagId
     * @param FileList
     * @return
     */
    private Map<String, Object> getTagMap(String name, Integer tagId, Object FileList) {
        Map<String, Object> tagMap = Maps.newHashMap();
        if (name == null || tagId == null || FileList == null) {
            return tagMap;
        }
        tagMap.put("name", name);
        tagMap.put("tagId", tagId);
        tagMap.put("fileList", FileList);
        return tagMap;
    }

    /**
     * 将文档转化为map的形式
     * @param files
     * @return
     */
    private List<Map<String, Object>> fileToMap(List<File> files) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(files)) {
            return result;
        }
        for (File file : files) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("name", file.getName());
            map.put("id", file.getId());
            map.put("thumbId", file.getThumbId());
            map.put("mongoFileId", file.getMongoFileId());
            result.add(map);
        }
        return result;
    }

}
