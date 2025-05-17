package org.example.util;

/**
 * Интерфейс для обработки событий реактивного потока.
 * @param <T>
 */
interface Observer<T> {

    /**
     * Передает элемент наблюдателю.
     */
    void onNext(T item);

    /**
     * Передает ошибку наблюдателю.
     *
     * @param t ошибка
     */
    void onError(Throwable t);

    /**
     * Уведомляет наблюдателя о завершении.
     */
    void onComplete();
}

