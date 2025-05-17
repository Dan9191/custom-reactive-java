package org.example.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SingleThreadScheduler implements Scheduler {

    /**
     * Однопоточный пул потоков для последовательного выполнения задач.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Запуск выполнения задач
     *
     * @param task задача.
     */
    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
