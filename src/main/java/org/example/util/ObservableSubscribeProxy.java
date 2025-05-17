package org.example.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Реализует интерфейсы Observer и Disposable для управления подпиской и обработки событий.
 *
 * @param <T>
 */
class ObservableSubscribeProxy<T> implements Observer<T>, Disposable {

    /**
     * Наблюдатель, которому делегируются события.
     */
    private final Observer<? super T> actual;

    /**
     * Флаг для отслеживания состояния подписки.
     */
    private final AtomicBoolean disposed = new AtomicBoolean();

    ObservableSubscribeProxy(Observer<? super T> actual) {
        this.actual = actual;
    }

    @Override
    public void onNext(T item) {
        if (!isDisposed()) {
            actual.onNext(item);
        }
    }

    @Override
    public void onError(Throwable t) {
        if (!isDisposed()) {
            actual.onError(t);
        }
    }

    @Override
    public void onComplete() {
        if (!isDisposed()) {
            actual.onComplete();
        }
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
