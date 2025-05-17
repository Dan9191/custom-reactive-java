package org.example.util;

public interface Disposable {

    /**
     * Метод для отмены подписки.
     */
    void dispose();


    /**
     * Была ли отменена прописка?
     */
    boolean isDisposed();
}
