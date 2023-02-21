package org.ccclll777.alldocsbackend.task.thread;


public interface RunnableTask extends Runnable {
    /**
     * 任务执行成功以后的方法
     */
    void success();
    /**
     * 任务执行失败以后的方法
     */
    void failed(Throwable throwable);
    void fallback();
}
