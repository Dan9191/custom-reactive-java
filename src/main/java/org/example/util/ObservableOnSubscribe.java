package org.example.util;

interface ObservableOnSubscribe<T> {

    /**
     * Подписка для наблюдения за элементами.
     *
     * @param observer наблюдатель, получающий элементы
     */
    void subscribe(Observer<? super T> observer) throws Exception;
}