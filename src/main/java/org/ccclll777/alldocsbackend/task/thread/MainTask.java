package org.ccclll777.alldocsbackend.task.thread;


import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.enums.FileStateEnum;
import org.ccclll777.alldocsbackend.enums.FileType;
import org.ccclll777.alldocsbackend.service.FileService;
import org.ccclll777.alldocsbackend.task.data.TaskData;
import org.ccclll777.alldocsbackend.task.exception.TaskRunException;
import org.ccclll777.alldocsbackend.task.executor.TaskExecutor;
import org.ccclll777.alldocsbackend.task.executor.TaskExecutorFactory;
import org.ccclll777.alldocsbackend.utils.SpringApplicationContext;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Author Jarrett Luo
 * @Date 2022/10/20 17:59
 * @Version 1.0
 */
@Slf4j
public class MainTask implements RunnableTask {

    private final TaskExecutor taskExecutor;

    /**
     * 任务要处理的相关数据
     */
    private TaskData taskData = new TaskData();

    /**
     * @Author luojiarui
     * @Description // 初始化任务，指定一个
     * @Date 15:43 2022/11/13
     * @Param [fileDocument]
     **/
    public MainTask(FileDocument fileDocument) {
        taskData.setFileDocument(fileDocument);
        taskData.setThumbFilePath("");
        taskData.setThumbFilePath("");
        //根据后缀名获取文件类型
        String fileSuffix = fileDocument.getSuffix();
        FileType fileType = FileType.getFileType(fileSuffix);
        taskData.setFileType(fileType);
        //根据对应的文件类型获取执行器
        this.taskExecutor = TaskExecutorFactory.getTaskExecutor(fileType);
    }

    /**
     * @Author luojiarui
     * @Description 成功以后更新文件
     * @Date 18:17 2022/11/13
     * @Param []
     **/
    @Override
    public void success() {
        taskData.getFileDocument().setDocState(FileStateEnum.SUCCESS);
        updateTaskStatus();
        // 更新文档的数据
        //TODO 在这里加修改数据库的状态信息的部分
        FileService fileService = SpringApplicationContext.getBean(FileService.class);
        fileService.updateFile(taskData.getFileDocument());
    }

    @Override
    public void failed(Throwable throwable) {
        log.error("解析文件报错啦", throwable);
        String errorMsg = throwable.getLocalizedMessage();
        taskData.getFileDocument().setDocState(FileStateEnum.FAIL);
        taskData.getFileDocument().setErrorMsg(errorMsg);
        updateTaskStatus();
    }

    @Override
    public void run() {
        FileDocument fileDocument = taskData.getFileDocument();
        if (null == taskExecutor || fileDocument == null) {
            log.error("执行文件{}报错",fileDocument);
            throw new TaskRunException("当前执行器初始化失败！");
        }
        if (StringUtils.hasText(fileDocument.getThumbId()) || StringUtils.hasText(fileDocument.getTextFileId())) {
            removeExistGridFs();
        }
        // 更新子任务数据,开始更新状态，开始进行解析等等
        taskData.getFileDocument().setDocState(FileStateEnum.ON_PROCESS);
        updateTaskStatus();

        // 调用执行器执行任务
        this.taskExecutor.execute(taskData);

    }

    @Override
    public void fallback() {
        // 删除es中的数据，删除thumb数据，删除存储的txt文本文件
        try {
            String txtFilePath = taskData.getTxtFilePath();
            if (StringUtils.hasText(txtFilePath) && new File(txtFilePath).exists()) {
                Files.delete(Paths.get(txtFilePath));
            }
            String picFilePath = taskData.getThumbFilePath();
            if (StringUtils.hasText(picFilePath) && new File(picFilePath).exists()) {
                Files.delete(Paths.get(picFilePath));
            }
        } catch (IOException e) {
            log.error("删除文件路径{} ==> 失败信息{}", taskData.getTxtFilePath(), e);
        }
        // 删除相关的文件
        removeExistGridFs();
        // 删除es中的数据
    }

    /**
     * 更新文档状态
     */
    private void updateTaskStatus() {
        FileService fileService = SpringApplicationContext.getBean(FileService.class);
        FileDocument fileDocument = taskData.getFileDocument();
        try {
            fileService.updateState(fileDocument, fileDocument.getDocState(), fileDocument.getErrorMsg());
        } catch (TaskRunException e) {
            throw new TaskRunException("更新文档状态失败", e);
        }
    }

    /**
     * 删除已经存在的文本文件和缩略图文件
     **/
    private void removeExistGridFs() {
        FileDocument fileDocument = taskData.getFileDocument();
        String textFileId = fileDocument.getTextFileId();
        String thumbFileId = fileDocument.getThumbId();
        FileService fileService = SpringApplicationContext.getBean(FileService.class);
        fileService.deleteGridFs(textFileId, thumbFileId);
    }

}
