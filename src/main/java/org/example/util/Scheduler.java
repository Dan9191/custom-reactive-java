package org.example.util;

interface Scheduler {

    /**
     * Запуск выполнения задач
     *
     * @param task задача.
     */
    void execute(Runnable task);
}