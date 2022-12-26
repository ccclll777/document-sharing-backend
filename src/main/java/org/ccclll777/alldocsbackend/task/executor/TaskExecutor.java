package org.ccclll777.alldocsbackend.task.executor;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apdplat.word.WordSegmenter;
import org.ccclll777.alldocsbackend.AllDocsBackendApplication;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.FileObj;
import org.ccclll777.alldocsbackend.enums.FileFormatEnum;
import org.ccclll777.alldocsbackend.service.ElasticService;
import org.ccclll777.alldocsbackend.service.FileService;
import org.ccclll777.alldocsbackend.task.data.TaskData;
import org.ccclll777.alldocsbackend.task.exception.TaskRunException;
import org.ccclll777.alldocsbackend.utils.SpringApplicationContext;
import org.ccclll777.alldocsbackend.utils.WordSegmentation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @Author Jarrett Luo
 * @Date 2022/10/20 18:23
 * @Version 1.0
 */
@Slf4j
public abstract class TaskExecutor {

    public void execute(TaskData taskData) throws TaskRunException {

        // 第一步下载文件，转换为byte数组
        FileDocument fileDocument = taskData.getFileDocument();
        InputStream docInputStream = new ByteArrayInputStream(downFileBytes(fileDocument.getGridfsId()));

        // 将文本索引到es中
        try {
            uploadFileToEs(docInputStream, fileDocument, taskData);
        } catch (Exception e) {
            throw new TaskRunException("建立索引时出错：{}", e);
        }
        docInputStream = new ByteArrayInputStream(downFileBytes(fileDocument.getGridfsId()));
        try {
            // 制作不同分辨率的缩略图
            updateFileThumb(docInputStream, taskData.getFileDocument(), taskData);
        } catch (Exception e) {
            throw new TaskRunException("建立缩略图时出错", e);
        }
    }

    /**
     * 从gridFS 系统中下载文件为字节流 根据gridFsId获取对应的文件
     **/
    protected byte[] downFileBytes(String gridFsId) {
        FileService fileService = SpringApplicationContext.getBean(FileService.class);
        return fileService.getFileBytes(gridFsId);
    }

    /**
     * 将文件上传到elastic search 然后建立索引 并且要上传到mongoDB的文件系统中
     * @param is
     * @param fileDocument
     * @param taskData
     */
    public void uploadFileToEs(InputStream is, FileDocument fileDocument, TaskData taskData) {
        //文件路径
        String textFilePath = "./" + fileDocument.getMd5() + fileDocument.getName() + ".txt";
        taskData.setTxtFilePath(textFilePath);
        try {
            // 根据不同的执行器，执行不同的文本提取方法，在这里做出区别
            readText(is, textFilePath);
            if (!new File(textFilePath).exists()) {
                throw new TaskRunException("文本文件不存在，需要进行重新提取");
            }
            FileObj fileObj = new FileObj();
            fileObj.setId(fileDocument.getMd5());
            fileObj.setName(fileDocument.getName());
            fileObj.setType(fileDocument.getContentType());

            //读取文件
            fileObj.readFile(textFilePath);
            List<String> list = WordSegmentation.cutWord(fileObj.getName());
            fileObj.setSuggestion(list);
            this.upload(fileObj);

        } catch (IOException | TaskRunException e) {
            throw new TaskRunException("存入es的过程中报错了", e);
        }

        handleDescription(textFilePath, fileDocument);

        // 将建立elastic索引的文本文件上传到gridFS系统中
        try (FileInputStream inputStream = new FileInputStream(textFilePath)) {

            FileService fileService = SpringApplicationContext.getBean(FileService.class);
            //上传到gridFS系统中时，需要区别文件的类型
            String txtObjId = fileService.uploadFileToGridFs(
                    FileFormatEnum.TEXT.getFilePrefix(),
                    inputStream,
                    FileFormatEnum.TEXT.getContentType());
            fileDocument.setTextFileId(txtObjId);
        } catch (IOException e) {
            throw new TaskRunException("存储文本文件报错了，请核对", e);
        }

        try {
            Files.delete(Paths.get(textFilePath));
        } catch (IOException e) {
            log.error("删除文件路径{} ==> 失败信息{}", textFilePath, e);
        }

    }

    /**
     * @Author luojiarui
     * @Description 设置描述内容
     * @Date 18:54 2022/11/13
     * @Param [textFilePath, fileDocument]
     **/
    private void handleDescription(String textFilePath, FileDocument fileDocument) {
        try{
            List<String> stringList = FileUtils.readLines(new File(textFilePath), StandardCharsets.UTF_8);
            String str = null;
            if (!stringList.isEmpty()) {
                str = stringList.get(0);
            }

            if (str == null) {
                str = "无描述";
            } else if (str.length() > 128) {
                str = str.substring(0, 128);
            }
            fileDocument.setDescription(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件流，取到其中的文字信息
     *
     * @param is           文件流
     * @param textFilePath 存储文本文件
     * @throws IOException -> IO
     */
    protected abstract void readText(InputStream is, String textFilePath) throws IOException;

    /**
     * 制作缩略图
     *
     * @param is      文件流
     * @param picPath 图片地址
     * @throws IOException -> IOException
     */
    protected abstract void makeThumb(InputStream is, String picPath) throws IOException;

    /**
     * / 上传整备好的文本文件进行上传到es中
     **/
    public void upload(FileObj fileObj) throws IOException {
        ElasticService elasticService = SpringApplicationContext.getBean(ElasticService.class);
        elasticService.upload(fileObj);
    }

    /**
     * 传文件的缩略图
     **/
    public void updateFileThumb(InputStream inputStream, FileDocument fileDocument, TaskData taskData) throws IOException {
        String picPath = "./" + IdUtil.simpleUUID() + ".png";
        taskData.setThumbFilePath(picPath);
        // 将pdf输入流转换为图片并临时保存下来
        makeThumb(inputStream, picPath);
        if ( !new File(picPath).exists()) {
            return;
        }
        try (FileInputStream thumbIns = new FileInputStream(picPath)){
            // 存储到GridFS系统中
            FileService fileService = SpringApplicationContext.getBean(FileService.class);
            String txtObjId = fileService.uploadFileToGridFs(
                    FileFormatEnum.PNG.getFilePrefix(),
                    thumbIns,
                    FileFormatEnum.PNG.getContentType());
            fileDocument.setThumbId(txtObjId);
            //将缩略图的ID上传到Mysql中
            fileService.updateThumbIdByMongoFileId(txtObjId,fileDocument.getId());
        } catch (IOException e) {
            throw new TaskRunException("存储缩略图文件报错了，请核对", e);
        }

        try {
            Files.delete(Paths.get(picPath));
        } catch (IOException e) {
            log.error("删除文件路径{} ==> 失败信息{}", picPath, e);
        }

    }
}
