package org.ccclll777.alldocsbackend.task.executor;

import org.ccclll777.alldocsbackend.enums.FileType;

import java.util.EnumMap;
import java.util.Map;

/**
 * 工厂类，每种类型的文件 都要有自己的taskExector
 */
public class TaskExecutorFactory {

    private TaskExecutorFactory() {

    }

    static Map<FileType, TaskExecutor> taskExecutorMap = new EnumMap<>(FileType.class);

    public static TaskExecutor getTaskExecutor(FileType fileType) {
        TaskExecutor taskExecutor = taskExecutorMap.get(fileType);
        if (null != taskExecutor) {
            return taskExecutor;
        }
        return createTaskExecutor(fileType);
    }

    /**
     * 创建任务执行器
     * @param fileType 文档类型
     * @return 任务执行器
     */
    private static TaskExecutor createTaskExecutor(FileType fileType) {
        TaskExecutor taskExecutor = null;
        //目前只有pdf到word的
        switch (fileType) {
            case PDF:
                taskExecutor = new PdfWordTaskExecutor();
                break;
            case DOCX:
            case PPTX:
            case XLSX:
                taskExecutor = new DocxExecutor();
                break;
            default:
                break;
        }
        if (null != taskExecutor) {
            taskExecutorMap.put(fileType, taskExecutor);
        }
        return taskExecutor;
    }
}
