package org.ccclll777.alldocsbackend.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.*;
import org.ccclll777.alldocsbackend.entity.File;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.Tag;
import org.ccclll777.alldocsbackend.entity.dto.UpdateFileDTO;
import org.ccclll777.alldocsbackend.entity.dto.UploadDTO;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
     * ???????????????mongo
     *
     * ??????????????????gridfsId???id??????????????????????????????mongo?????????ID????????????????????????mysql???file????????????file tag????????????
     * ??????mysql???????????????????????????????????????????????????????????????????????????????????????????????????mongo???????????????????????????????????????
     *
     * @param md5  ??????md5
     * @param file ??????
     * @return FileDocument
     */
    public FileDocument saveFile(String md5, MultipartFile file, Integer userId, UploadDTO uploadDTO) {
        //????????????????????????????????????
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
            //???????????????mongo???
            gridfsId = uploadFileToGridFs(file.getInputStream(), file.getContentType());
            fileDocument.setGridfsId(gridfsId);
            fileDocument = mongoTemplate.save(fileDocument, COLLECTION_NAME);
            fileDocumentId = fileDocument.getId();
        } catch (IOException ex) {
            //IOException ??????MongoDB???????????? ???????????????????????????
            log.info("saveFile ???????????????gridfs?????? ??????IOException,{}",ex.getMessage());
            ex.printStackTrace();
            return null;
        }
        //????????? ??????????????????IO Exception ???????????????????????????
        try {
            //TODO ???????????????????????? ????????????????????????
            //fileService ?????????????????? ??????????????????????????????
            //?????????????????????id????????????????????????id
            if (uploadDTO != null) {
                fileService.saveWhenSaveDoc(fileDocument,gridfsId,uploadDTO.getCategoryId(),userId,uploadDTO.getDescription());
            } else {
                fileService.saveWhenSaveDoc(fileDocument,gridfsId,17,userId,"");
            }
        }catch (RuntimeException rx) {
            //??????????????? RuntimeException ???????????????????????????????????????????????????mongo?????????????????????????????????
            log.info("saveFile ???????????????Mysql??????RuntimeException ???????????? ,{}",rx.getMessage());
            removeFile(fileDocumentId,true);
            return null;
        }
        return fileDocument;
    }
    /**
     * ????????????????????????????????????????????????
     * @param fileDocument
     * @param gridfsId
     * @param categoryId
     */


    public void saveWhenSaveDoc(FileDocument fileDocument,String gridfsId,Integer categoryId,Integer userId,String description) {
        try {
            if(fileDocument == null ) {
                return;
            }
            Integer rewiewing = fileDocument.isReviewing()?1:0;
            //???????????????????????????????????????file??????
            File file = File.builder()
                    .gridfsId(gridfsId).name(fileDocument.getName()).createTime(fileDocument.getUploadDate())
                    .contentType(fileDocument.getContentType()).suffix(fileDocument.getSuffix())
                    .reviewing(rewiewing).fileState(fileDocument.getDocState().getCode()).categoryId(categoryId)
                    .errorMessage("").userId(userId).size(fileDocument.getSize())
                    .MongoFileId(fileDocument.getId()).thumbId(fileDocument.getThumbId()).md5(fileDocument.getMd5()).description(description)
                    .build();
            filesDao.insertFile(file);
            int fileId = filesDao.selectFileByGridfsId(file.getGridfsId()).getId();
            /**
             * ????????????
             */
            String suffix = fileDocument.getSuffix();
            String tagName = suffix.substring(suffix.lastIndexOf(".") + 1);
            if (tagName.length() == 0) {
                return;
            }
            Tag tag = tagDao.selectTagByName(tagName);

            //???????????????????????????tag ?????????????????????????????????
            Integer tagId = 0;
            if( tag == null) {
                Tag newTag = Tag.builder()
                        .userId(userId).name(tagName).build();
                tagDao.insertTag(newTag);
                tagId = tagDao.selectTagByName(tagName).getId();
            } else {
                tagId = tag.getId();
            }
            //??????file???tag?????????
            fileTagDao.insertFileTag(fileId,tagId);
        } catch (RuntimeException e ) {
            //??????????????????????????? ??????????????????
            //?????????????????????????????????????????????????????????????????????
            throw new RuntimeException(e.getMessage());
        }


    }
    /**
     * js?????????????????????
     *
     * @param fileDocument ????????????
     * @param inputStream  ???????????????
     * @return FileDocument
     */
    public FileDocument saveFile(FileDocument fileDocument, InputStream inputStream) {
        //????????????????????????????????????
        FileDocument dbFile = getByMd5(fileDocument.getMd5());
        if (dbFile != null) {
            return dbFile;
        }
        //GridFSInputFile inputFile = gridFsTemplate

        String gridfsId = uploadFileToGridFs(inputStream, fileDocument.getContentType());
        fileDocument.setGridfsId(gridfsId);

        fileDocument = mongoTemplate.save(fileDocument, COLLECTION_NAME);

        // TODO ???????????????????????????

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
     * @Description // ??????????????????
     * @Date 15:41 2022/11/13
     * @Param [fileDocument, state]
     **/

    public void updateState(FileDocument fileDocument, FileStateEnum state, String errorMsg) throws TaskRunException {
        String mongoFileId = fileDocument.getId();
        if (state != FileStateEnum.FAIL) {
            errorMsg = "???";
        }
        try {
            fileService.updateDatabaseState(mongoFileId,errorMsg,state.getCode());
        } catch (RuntimeException ex) {
            log.error("????????????????????????????????????{}==>??????==>{}", fileDocument, ex.getMessage());
            return;
        }
        //??????????????????????????????Mongo
        Query query = new Query(Criteria.where("_id").is(mongoFileId));
        Update update = new Update();
        update.set("docState", state);
        update.set("errorMsg", errorMsg);
        //?????????????????????FileId
        try {
            mongoTemplate.updateFirst(query, update, FileDocument.class, COLLECTION_NAME);
        } catch (Exception e) {
            log.error("????????????????????????{}==>??????==>{}", fileDocument, e);
            throw new TaskRunException("????????????????????????==>??????==>{}", e);
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void updateDatabaseState(String mongoFileId,String errorMsg,Integer state) {
        try {
            //??????????????????
            filesDao.updateErrorMessageByMongoFileId(mongoFileId,errorMsg);
            filesDao.updateFileStateByMongoFileId(mongoFileId,state);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     *  ???gridFs???????????????
     **/
    public void deleteGridFs(String... id) {
        Query deleteQuery = new Query().addCriteria(Criteria.where(FILE_NAME).in(id));
        gridFsTemplate.delete(deleteQuery);
    }

    /**
     * ??????md5??????????????????
     *
     * @param md5 String
     * @return -> FileDocument
     */
    public FileDocument getByMd5(String md5) {
        if (md5 == null) {
            return null;
        }
        //?????????mongo???mysql????????????????????????md5????????????????????????????????????????????????
        int count = filesDao.fileCountByMd5(md5);
        if (count <=0 ) {
            return null;
        }
        Query query = new Query().addCriteria(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query, FileDocument.class, COLLECTION_NAME);
    }
    /**
     * ???????????????Mongodb???GridFs???
     *
     * @param in          -> InputStream
     * @param contentType -> String
     * @return -> String
     */
    private String uploadFileToGridFs(InputStream in, String contentType) {
        //???????????????UUID
        String gridfsId = IdUtil.simpleUUID();
        //??????????????????GridFS???
        gridFsTemplate.store(in, gridfsId, contentType);
        // ????????????????????????id????????????
//        ObjectId objectId = gridFsTemplate.store()
        return gridfsId;
    }
    /**
     * ?????????????????????Mongodb???GridFs???
     *
     */
    public String uploadFileToGridFs(String prefix, InputStream in, String contentType) {
        String gridfsId = prefix + IdUtil.simpleUUID();
        //??????????????????GridFS???
        gridFsTemplate.store(in, gridfsId, contentType);
        return gridfsId;
    }

    /**
     * ????????????
     *
     * @param id           ??????id
     * @param isDeleteFile ??????????????????
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
     * ????????????
     *
     * @param id ??????id
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
            // ??????????????????
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
     * ??????Mongo??????????????????thumbId??????Byte???
     * @param thumbId
     * @return
     */
    public byte[] getFileBytes(String thumbId) {
        if (StringUtils.hasText(thumbId)) {
            Query gridQuery = new Query().addCriteria(Criteria.where(FILE_NAME).is(thumbId));
            //??????????????????
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
            String reviewState = file.getReviewing() == 1? "????????????": "????????????";
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
     * ?????????????????? ????????????????????????????????????
     *
     * ??????????????????????????????Mysql??????????????????MongoDB?????????
     * @param fileId
     * @return
     */
    public int deleteFileCompletely(Integer fileId) {
        File file;
        try {
            //????????????
            file = filesDao.selectFileById(fileId);
            //???????????????????????????Transactional????????????
            fileService.deleteFileAndTag(fileId);
        } catch (RuntimeException e) {
            //??????????????? RuntimeException ???????????????????????????????????????????????????mongo?????????????????????????????????
            log.info("deleteFileCompletely ??????Mysql?????????RuntimeException ????????????");
            return -1;
        }

        if (file != null) {
            String MongoFileId = file.getMongoFileId();
            removeFile(MongoFileId,true);
            return 1;
        }
        return -1;
    }
    @Transactional(rollbackFor=Exception.class)
    public void deleteFileAndTag(int fileId) {
        try {
            filesDao.deleteFile(fileId);
            fileTagDao.deleteFileTag(fileId);
        } catch (RuntimeException e ) {
            //??????????????????????????? ??????????????????
            //?????????????????????????????????????????????????????????????????????
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * ????????????
     * ????????????????????? ????????????????????????????????????
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
            String reviewState = file.getReviewing() == 1? "????????????": "????????????";
            List<String> tagNames = fileTagDao.selectTagNameById(file.getId());
            FilesVO filesVO = FilesVO.builder()
                    .id(file.getId()).name(file.getName()).suffix(file.getSuffix())
                    .categoryName(categoryName).tagNames(tagNames).mongoFileId(file.getMongoFileId())
                    .userName(userName).reviewState(reviewState).size(ByteConverter.getSize(file.getSize())).build();
            filesVOs.add(filesVO);
        }
        return filesVOs;
    }

    /**
     * ???????????? ????????????GridFS????????????ID?????????Mysql???
     * @param ThumbId
     * @param mongoFileId
     * @return
     */
    public int updateThumbIdByMongoFileId(String ThumbId, String mongoFileId) {
       return filesDao.updateThumbIdByMongoFileId(ThumbId,mongoFileId);
    }

    /**
     * ??????????????????elasticsearch?????????????????????
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
     * ????????????
     * @param keyWord
     * @return
     * @throws IOException
     */
    public List<String> searchSuggest(String keyWord) throws IOException {
        return  elasticService.searchSuggestion(keyWord);
    }

    /**
     * ??????????????????
     * @param mongoFileId
     * @return
     */
    public FilesVO selectFIleByMongoFileId(String mongoFileId) {
        File file = filesDao.selectFileByMongoFileId(mongoFileId);
        filesDao.updateHits(file.getId());
        if (file == null) {
            return null;
        }
        String userName = userDao.selectUserById(file.getUserId()).getUserName();
        String categoryName = categoryDao.selectCategoryById(file.getCategoryId()).getName();
        String fileState = FileStateEnum.getMessage(file.getFileState());
        String reviewState = file.getReviewing() == 1? "????????????": "????????????";
        List<String> tagNames = fileTagDao.selectTagNameById(file.getId());
        FilesVO filesVO = FilesVO.builder()
                .id(file.getId()).name(file.getName()).suffix(file.getSuffix())
                .categoryName(categoryName).tagNames(tagNames)
                .userName(userName).fileState(fileState)
                .errorMessage(file.getErrorMessage()).reviewState(reviewState).size(ByteConverter.getSize(file.getSize()))
                .thumbId(file.getThumbId()).createTime(file.getCreateTime()).description(file.getDescription())
                .build();
        return filesVO;
    }

    /**
     * ??????????????????
     * @param updateFileDTO
     * @return
     */
    @Transactional(rollbackFor=Exception.class)
    public int updateFile(UpdateFileDTO updateFileDTO) {
        filesDao.updateFileCategory(updateFileDTO.getCategoryId(), updateFileDTO.getFileId());
        filesDao.updateFileName(updateFileDTO.getName(), updateFileDTO.getFileId());
        filesDao.updateFileDescription(updateFileDTO.getDescription(),updateFileDTO.getFileId());
        return 1;
    }



}
