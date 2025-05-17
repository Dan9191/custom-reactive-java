package org.example.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class IOThreadScheduler implements Scheduler {

    /**
     * Пул потоков с кэшированием для динамического выделения потоков.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
