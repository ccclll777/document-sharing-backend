package org.ccclll777.alldocsbackend.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.ccclll777.alldocsbackend.dao.*;
import org.ccclll777.alldocsbackend.entity.File;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.Tag;
import org.ccclll777.alldocsbackend.entity.vo.FilesVO;
import org.ccclll777.alldocsbackend.entity.vo.SearchFilesVO;
import org.ccclll777.alldocsbackend.enums.FileStateEnum;
import org.ccclll777.alldocsbackend.task.exception.TaskRunException;
import org.ccclll777.alldocsbackend.utils.ByteConverter;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
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

import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
    private FileService fileService;
    /**
     * 上传文件到mongo
     *
     * 由于需要拿到gridfsId的id，所以要先将数据存入mongo，返回ID后将相关数据存入mysql（file的信息，file tag的关系）
     * 存入mysql的操作需要保证事务性，事务成功后返回成功，事务如果失败，需要将存入mongo中的数据删除，保证无事发生
     *
     * @param md5  文件md5
     * @param file 文件
     * @return FileDocument
     */
    public FileDocument saveFile(String md5, MultipartFile file, Integer userId) {
        //已存在该文件，则实现秒传
        FileDocument fileDocument = getByMd5(md5);
        if (fileDocument != null) {
            File tmpFile = filesDao.selectFileByGridfsId(fileDocument.getGridfsId());
            tmpFile.setFileState(FileStateEnum.WAITE.getCode());
            filesDao.updateFileState(tmpFile);
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
        String gridfsId;
        String fileDocumentId;
        try {
            //将数据传到mongo中
            gridfsId = uploadFileToGridFs(file.getInputStream(), file.getContentType());
            fileDocument.setGridfsId(gridfsId);
            fileDocument = mongoTemplate.save(fileDocument, COLLECTION_NAME);
            fileDocumentId = fileDocument.getId();
        } catch (IOException ex) {
            //IOException 表示MongoDB存储失败 不执行之后的代码了
            log.info("saveFile 保存文件到gridfs失败 发生IOException,{}",ex.getMessage());
            ex.printStackTrace();
            return null;
        }
        //走到这 说明没有发生IO Exception 可以进行数据库操作
        try {
            //TODO 异步保存数据标签 文档分类到数据库
            //fileService 自己注入自己 避免调用时事务失效。
            //这里传入的分类id是默认分类的分类id
            fileService.saveWhenSaveDoc(fileDocument,gridfsId,17,userId);
        }catch (RuntimeException rx) {
            //如果抛出了 RuntimeException 表示事务调用处理失败，需要删除存入mongo中的文件，保证无事发生
            log.info("saveFile 保存数据到Mysql发生RuntimeException 事务回滚 ,{}",rx.getMessage());
            removeFile(fileDocumentId,true);
            return null;
        }
        return fileDocument;
    }

    /**
     * 需要保证这个方法的事务，同时执行
     * @param fileDocument
     * @param gridfsId
     * @param categoryId
     */
    @Transactional
    public void saveWhenSaveDoc(FileDocument fileDocument,String gridfsId,Integer categoryId,Integer userId) {
        try {
            if(fileDocument == null ) {
                return;
            }
            Integer rewiewing = fileDocument.isReviewing()?1:0;
            //首先保存文档信息到数据库的file表中
            File file = File.builder()
                    .gridfsId(gridfsId).name(fileDocument.getName()).createTime(fileDocument.getUploadDate())
                    .contentType(fileDocument.getContentType()).suffix(fileDocument.getSuffix())
                    .reviewing(rewiewing).fileState(fileDocument.getDocState().getCode()).categoryId(categoryId)
                    .errorMessage("").userId(userId).size(fileDocument.getSize())
                    .MongoFileId(fileDocument.getId()).thumbId(fileDocument.getThumbId()).md5(fileDocument.getMd5())
                    .build();
            filesDao.insertFile(file);
            int fileId = filesDao.selectFileByGridfsId(file.getGridfsId()).getId();
            /**
             * 保存标签
             */
            String suffix = fileDocument.getSuffix();
            String tagName = suffix.substring(suffix.lastIndexOf(".") + 1);
            if (tagName.length() == 0) {
                return;
            }
            Tag tag = tagDao.selectTagByName(tagName);

            //如果没有这种后缀的tag 则新建一个并插入数据库
            Integer tagId = 0;
            if( tag == null) {
                Tag newTag = Tag.builder()
                        .userId(userId).name(tagName).build();
                tagDao.insertTag(newTag);
                tagId = tagDao.selectTagByName(tagName).getId();
            } else {
                tagId = tag.getId();
            }
            //插入file和tag的关系
            fileTagDao.insertFileTag(fileId,tagId);
        } catch (RuntimeException e ) {
            //手动抛出异常给上层 避免事务失效
            //并且这样抛出异常，可以让这个方法的事务正常回滚
            throw new RuntimeException(e.getMessage());
        }


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
        String mongoFileId = fileDocument.getId();
        if (state != FileStateEnum.FAIL) {
            errorMsg = "无";
        }
        try {
            fileService.updateDatabaseState(mongoFileId,errorMsg,state.getCode());
        } catch (RuntimeException ex) {
            log.error("更新数据库中文档状态信息{}==>出错==>{}", fileDocument, ex.getMessage());
            return;
        }
        //如果没有异常，则更新Mongo
        Query query = new Query(Criteria.where("_id").is(mongoFileId));
        Update update = new Update();
        update.set("docState", state);
        update.set("errorMsg", errorMsg);
        //先找到数据库的FileId
        try {
            mongoTemplate.updateFirst(query, update, FileDocument.class, COLLECTION_NAME);
        } catch (Exception e) {
            log.error("更新文档状态信息{}==>出错==>{}", fileDocument, e);
            throw new TaskRunException("更新文档状态信息==>出错==>{}", e);
        }
    }
    @Transactional
    public void updateDatabaseState(String mongoFileId,String errorMsg,Integer state) {
        try {
            //先更新数据库
            filesDao.updateErrorMessageByMongoFileId(mongoFileId,errorMsg);
            filesDao.updateFileStateByMongoFileId(mongoFileId,state);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex.getMessage());
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
        //如果在mongo和mysql中找到对应文件的md5值，则说明已经上传过了，实现秒传
        int count = filesDao.fileCountByMd5(md5);
        if (count <=0 ) {
            return null;
        }
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
     * 上传文本文件到Mongodb的GridFs中
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

    public List<FilesVO> selectFiles(int pageNum, int pageSize) {
        int offset = pageNum * pageSize;
        List<File> files = filesDao.selectFiles(pageSize,offset);
        List<FilesVO> filesVOs = new ArrayList<>();
        for (File file : files) {
            String userName = userDao.selectUserById(file.getUserId()).getUserName();
            String categoryName = categoryDao.selectCategoryById(file.getCategoryId()).getName();
            String fileState = FileStateEnum.getMessage(file.getFileState());
            String reviewState = file.getReviewing() == 1? "正在审查": "审查完毕";
            List<String> tagNames = fileTagDao.selectTagNameById(file.getId());
            FilesVO filesVO = FilesVO.builder()
                    .id(file.getId()).name(file.getName()).suffix(file.getSuffix())
                    .categoryName(categoryName).tagNames(tagNames)
                    .userName(userName).fileState(fileState)
                    .errorMessage(file.getErrorMessage()).reviewState(reviewState).size(ByteConverter.getSize(file.getSize())).build();
            filesVOs.add(filesVO);

         }
        return filesVOs;
    }
    public int fileCount() {
        return filesDao.fileCount();
    }

    /**
     * 彻底删除文档 需要保证删除操作的原子性
     *
     * 为了保证安全，先删除Mysql数据，再删除MongoDB中数据
     * @param fileId
     * @return
     */
    public int deleteFileCompletely(Integer fileId) {
        File file;
        try {
            //先查再删
            file = filesDao.selectFileById(fileId);
            //通过方法调用，避免Transactional注解失效
            fileService.deleteFileAndTag(fileId);
        } catch (RuntimeException e) {
            //如果抛出了 RuntimeException 表示事务调用处理失败，需要删除存入mongo中的文件，保证无事发生
            log.info("deleteFileCompletely 修改Mysql时发生RuntimeException 事务回滚");
            return -1;
        }

        if (file != null) {
            String MongoFileId = file.getMongoFileId();
            removeFile(MongoFileId,true);
            return 1;
        }
        return -1;
    }
    @Transactional
    public void deleteFileAndTag(int fileId) {
        try {
            filesDao.deleteFile(fileId);
            fileTagDao.deleteFileTag(fileId);
        } catch (RuntimeException e ) {
            //手动抛出异常给上层 避免事务失效
            //并且这样抛出异常，可以让这个方法的事务正常回滚
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * 用户使用
     * 不彻底删除文档 指将文档状态改编为已删除
     */
    public int deleteFile(int fileId) {
        File file = filesDao.selectFileById(fileId);
        file.setFileState(FileStateEnum.DELETE.getCode());
        return filesDao.updateFileState(file);
    }

    public int fileCountByUserId(int userId) {
        return filesDao.fileCountByUserId(userId);
    }

    public List<FilesVO> selectFilesByUserId(int pageNum, int pageSize, int userId) {
        int offset = pageNum * pageSize;
        List<File> files = filesDao.selectFilesByUserId(pageSize,offset,userId);
        List<FilesVO> filesVOs = new ArrayList<>();
        for (File file : files) {
            if(file.getFileState().equals(FileStateEnum.DELETE.getCode())) {
                continue;
            }
            String userName = userDao.selectUserById(file.getUserId()).getUserName();
            String categoryName = categoryDao.selectCategoryById(file.getCategoryId()).getName();
            String reviewState = file.getReviewing() == 1? "正在审查": "审查完毕";
            List<String> tagNames = fileTagDao.selectTagNameById(file.getId());
            FilesVO filesVO = FilesVO.builder()
                    .id(file.getId()).name(file.getName()).suffix(file.getSuffix())
                    .categoryName(categoryName).tagNames(tagNames)
                    .userName(userName).reviewState(reviewState).size(ByteConverter.getSize(file.getSize())).build();
            filesVOs.add(filesVO);
        }
        return filesVOs;
    }

    /**
     * 将生成的 缩略图在GridFS系统中的ID存储到Mysql中
     * @param ThumbId
     * @param mongoFileId
     * @return
     */
    public int updateThumbIdByMongoFileId(String ThumbId, String mongoFileId) {
       return filesDao.updateThumbIdByMongoFileId(ThumbId,mongoFileId);
    }

    /**
     * 根据关键词在elasticsearch中查询文档信息
     * @param keyWord
     */
    public List<SearchFilesVO> search(String keyWord) throws IOException {
        List<FileDocument> esFileDocuments;
        List<SearchFilesVO>  searchFilesVOS = new ArrayList<>();

        esFileDocuments = elasticService.search(keyWord);
        for(FileDocument fileDocument: esFileDocuments) {
            SearchFilesVO searchFilesVO = SearchFilesVO.builder()
                    .mongoFileId(fileDocument.getId())
                    .description(fileDocument.getDescription())
                    .name(fileDocument.getName())
                    .build();
            searchFilesVOS.add(searchFilesVO);
        }
        return searchFilesVOS;
    }

    /**
     * 搜索建议
     * @param keyWord
     * @return
     * @throws IOException
     */
    public List<String> searchSuggest(String keyWord) throws IOException {
        return  elasticService.searchSuggestion(keyWord);
    }

    public FilesVO selectFIleByMongoFileId(String mongoFileId) {
        File file = filesDao.selectFileByMongoFileId(mongoFileId);
        if (file == null) {
            return null;
        }
        String userName = userDao.selectUserById(file.getUserId()).getUserName();
        String categoryName = categoryDao.selectCategoryById(file.getCategoryId()).getName();
        String fileState = FileStateEnum.getMessage(file.getFileState());
        String reviewState = file.getReviewing() == 1? "正在审查": "审查完毕";
        List<String> tagNames = fileTagDao.selectTagNameById(file.getId());
        FilesVO filesVO = FilesVO.builder()
                .id(file.getId()).name(file.getName()).suffix(file.getSuffix())
                .categoryName(categoryName).tagNames(tagNames)
                .userName(userName).fileState(fileState)
                .errorMessage(file.getErrorMessage()).reviewState(reviewState).size(ByteConverter.getSize(file.getSize()))
                .thumbId(file.getThumbId()).createTime(file.getCreateTime())
                .build();
        return filesVO;
    }


}
