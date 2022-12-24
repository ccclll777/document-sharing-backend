package org.ccclll777.alldocsbackend.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.enums.FileStateEnum;
import org.ccclll777.alldocsbackend.task.exception.TaskRunException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileService {
    private static final String COLLECTION_NAME = "files";

    private static final String PDF_SUFFIX = ".pdf";

    private static final String FILE_NAME = "filename";

    private static final String CONTENT = "content";

    private static final String[] EXCLUDE_FIELD = new String[]{"md5", "content", "contentType", "suffix", "description",
            "gridfsId", "thumbId", "textFileId", "errorMsg"};
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFsBucket;
    @Autowired
    private ElasticService elasticService;
    @Autowired
    private RedisService redisService;
    /**
     * 表单上传附件
     *
     * @param md5  文件md5
     * @param file 文件
     * @return FileDocument
     */
    public FileDocument saveFile(String md5, MultipartFile file) {
        //已存在该文件，则实现秒传
        FileDocument fileDocument = getByMd5(md5);
        if (fileDocument != null) {
            return fileDocument;
        }
        String originFilename = file.getOriginalFilename();
        fileDocument = new FileDocument();
        fileDocument.setName(originFilename);
        fileDocument.setSize(file.getSize());
        fileDocument.setContentType(file.getContentType());
        fileDocument.setUploadDate(new Date());
        fileDocument.setMd5(md5);

        if (StringUtils.hasText(originFilename)) {
            String suffix = originFilename.substring(originFilename.lastIndexOf("."));
            fileDocument.setSuffix(suffix);
        }
        try {
            //将数据传到mongo中
            String gridfsId = uploadFileToGridFs(file.getInputStream(), file.getContentType());
            fileDocument.setGridfsId(gridfsId);
            fileDocument = mongoTemplate.save(fileDocument, COLLECTION_NAME);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //TODO 异步保存数据标签
//        tagServiceImpl.saveTagWhenSaveDoc(fileDocument);

        return fileDocument;
    }
    /**
     * js文件流上传附件
     *
     * @param fileDocument 文档对象
     * @param inputStream  文档文件流
     * @return FileDocument
     */
    public FileDocument saveFile(FileDocument fileDocument, InputStream inputStream) {
        //已存在该文件，则实现秒传
        FileDocument dbFile = getByMd5(fileDocument.getMd5());
        if (dbFile != null) {
            return dbFile;
        }
        //GridFSInputFile inputFile = gridFsTemplate

        String gridfsId = uploadFileToGridFs(inputStream, fileDocument.getContentType());
        fileDocument.setGridfsId(gridfsId);

        fileDocument = mongoTemplate.save(fileDocument, COLLECTION_NAME);

        // TODO 在这里进行异步操作

        return fileDocument;
    }

    public void updateFile(FileDocument fileDocument) {
        Query query = new Query(Criteria.where("_id").is(fileDocument.getId()));
        Update update = new Update();
        update.set("textFileId", fileDocument.getTextFileId());
        update.set("thumbId", fileDocument.getThumbId());
        update.set("description", fileDocument.getDescription());
        mongoTemplate.updateFirst(query, update, FileDocument.class, COLLECTION_NAME);

    }

    /**
     * @Author luojiarui
     * @Description // 更新文档状态
     * @Date 15:41 2022/11/13
     * @Param [fileDocument, state]
     **/
    public void updateState(FileDocument fileDocument, FileStateEnum state, String errorMsg) throws TaskRunException {
        Query query = new Query(Criteria.where("_id").is(fileDocument.getId()));
        if (state != FileStateEnum.FAIL) {
            errorMsg = "无";
        }
        Update update = new Update();
        update.set("docState", state);
        update.set("errorMsg", errorMsg);
        try {
            mongoTemplate.updateFirst(query, update, FileDocument.class, COLLECTION_NAME);
        } catch (Exception e) {
            log.error("更新文档状态信息{}==>出错==>{}", fileDocument, e);
            throw new TaskRunException("更新文档状态信息==>出错==>{}", e);
        }
    }

    /**
     *  从gridFs中删除文件
     **/
    public void deleteGridFs(String... id) {
        Query deleteQuery = new Query().addCriteria(Criteria.where(FILE_NAME).in(id));
        gridFsTemplate.delete(deleteQuery);
    }

    /**
     * 根据md5获取文件对象
     *
     * @param md5 String
     * @return -> FileDocument
     */
    public FileDocument getByMd5(String md5) {
        if (md5 == null) {
            return null;
        }
        //如果在mongo中找到对应文件的md5值，则说明已经上传过了，实现秒传
        Query query = new Query().addCriteria(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query, FileDocument.class, COLLECTION_NAME);
    }
    /**
     * 上传文件到Mongodb的GridFs中
     *
     * @param in          -> InputStream
     * @param contentType -> String
     * @return -> String
     */
    private String uploadFileToGridFs(InputStream in, String contentType) {
        //生成独立的UUID
        String gridfsId = IdUtil.simpleUUID();
        //文件，存储在GridFS中
        gridFsTemplate.store(in, gridfsId, contentType);
        // 其实应该使用文件id进行存储
//        ObjectId objectId = gridFsTemplate.store()
        return gridfsId;
    }
    /**
     * 上传文件到Mongodb的GridFs中
     *
     */
    public String uploadFileToGridFs(String prefix, InputStream in, String contentType) {
        String gridfsId = prefix + IdUtil.simpleUUID();
        //文件，存储在GridFS中
        gridFsTemplate.store(in, gridfsId, contentType);
        return gridfsId;
    }

    /**
     * 删除附件
     *
     * @param id           文件id
     * @param isDeleteFile 是否删除文件
     */
    public void removeFile(String id, boolean isDeleteFile) {
        FileDocument fileDocument = mongoTemplate.findById(id, FileDocument.class, COLLECTION_NAME);
        if (fileDocument != null) {
            Query query = new Query().addCriteria(Criteria.where("_id").is(id));
            mongoTemplate.remove(query, COLLECTION_NAME);
            if (isDeleteFile) {
                Query deleteQuery = new Query().addCriteria(Criteria.where(FILE_NAME).is(fileDocument.getGridfsId()));
                gridFsTemplate.delete(deleteQuery);
            }
        }
    }

    /**
     * 查询附件
     *
     * @param id 文件id
     * @return
     */
    public Optional<FileDocument> getById(String id) {
        FileDocument fileDocument = mongoTemplate.findById(id, FileDocument.class, COLLECTION_NAME);
        if (fileDocument != null) {
            Query gridQuery = new Query().addCriteria(Criteria.where(FILE_NAME).is(fileDocument.getGridfsId()));
            GridFSFile fsFile = gridFsTemplate.findOne(gridQuery);

            if (fsFile == null || fsFile.getObjectId() == null) {
                return Optional.empty();
            }

            // 开启文件下载
            GridFSDownloadOptions gridFSDownloadOptions = new GridFSDownloadOptions();

            try (GridFSDownloadStream in = gridFsBucket.openDownloadStream(fsFile.getObjectId())) {
                if (in.getGridFSFile().getLength() > 0) {
                    GridFsResource resource = new GridFsResource(fsFile, in);
                    fileDocument.setContent(IoUtil.readBytes(resource.getInputStream()));
                    return Optional.of(fileDocument);
                } else {
                    return Optional.empty();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return Optional.empty();
    }

    /**
     * 根据Mongo中存储文件的thumbId获取Byte值
     * @param thumbId
     * @return
     */
    public byte[] getFileBytes(String thumbId) {

        if (StringUtils.hasText(thumbId)) {
            Query gridQuery = new Query().addCriteria(Criteria.where(FILE_NAME).is(thumbId));
            //从文件中读取
            GridFSFile fsFile = gridFsTemplate.findOne(gridQuery);
            if (fsFile == null) {
                return new byte[0];
            }
            try (GridFSDownloadStream in = gridFsBucket.openDownloadStream(fsFile.getObjectId())) {
                if (in.getGridFSFile().getLength() > 0) {
                    GridFsResource resource = new GridFsResource(fsFile, in);
                    return IoUtil.readBytes(resource.getInputStream());
                } else {
                    return new byte[0];
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return new byte[0];
    }

}
