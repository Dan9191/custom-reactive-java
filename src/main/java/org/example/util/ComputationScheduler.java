package org.example.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Планировщик пула потоков с количеством потоков, равным числу доступных процессоров.
 */
class ComputationScheduler implements Scheduler {

    /**
     * Пул потоков с количеством потоков, равным числу доступных процессоров.
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

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
