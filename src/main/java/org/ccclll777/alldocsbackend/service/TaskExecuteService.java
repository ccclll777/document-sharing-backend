package org.ccclll777.alldocsbackend.service;

import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.task.thread.MainTask;
import org.ccclll777.alldocsbackend.task.thread.TaskThreadPool;
import org.springframework.stereotype.Service;

/**
 * 提交任务执行
 */
@Service
public class TaskExecuteService {
    public void execute(FileDocument fileDocument) {
        MainTask mainTask = new MainTask(fileDocument);
        TaskThreadPool.getInstance().submit(mainTask);
    }
}
