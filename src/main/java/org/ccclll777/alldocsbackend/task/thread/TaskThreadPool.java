package org.ccclll777.alldocsbackend.task.thread;

import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class TaskThreadPool {

    private final ThreadPoolExecutor threadPoolExecutor;

    private final ListeningExecutorService listeningExecutorService;

    /**
     * 线程池中三个线程
     */
    private static final TaskThreadPool INSTANCE = new TaskThreadPool(3, "Task_Thread_%d");

    private List<MainTask> mainTaskList;


    private TaskThreadPool(Integer threadsNum, String threadNameFormat) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(threadNameFormat)
                .build();
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadsNum, threadFactory);
        listeningExecutorService = MoreExecutors.listeningDecorator(this.threadPoolExecutor);
        mainTaskList = new LinkedList<>();
    }

    public static TaskThreadPool getInstance() {
        return INSTANCE;
    }

    public <V> void submit(MainTask mainTask) {
        mainTaskList.add(mainTask);
        // 使用线程池执行任务工作流
        ListenableFuture<V> future = (ListenableFuture<V>) this.listeningExecutorService.submit(mainTask);

        // 工作流执行完成后，回调，将工作流从执行map中移除
        FutureCallback<V> futureCallback = new FutureCallback<V>() {
            @Override
            public void onSuccess(Object o) {
                mainTask.success();
                mainTaskList.remove(mainTask);
            }

            @Override
            public void onFailure(Throwable throwable) {
                mainTask.failed(throwable);
                mainTask.fallback();
                mainTaskList.remove(mainTask);
            }
        };
        Futures.addCallback(future, futureCallback, this.listeningExecutorService);

    }
}
