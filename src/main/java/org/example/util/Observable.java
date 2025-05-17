package org.example.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Источник реактивного потока данных.
 * @param <T>
 */
abstract class Observable<T> {

    /**
     * Статический метод create создает Observable из пользовательского источника.
     *
     * @param source источник данных, реализующий интерфейс ObservableOnSubscribe
     * @return новый Observable, связанный с указанным источником
     * @param <T>
     */
    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                try {
                    source.subscribe(observer);
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        };
    }

    protected abstract void subscribeActual(Observer<? super T> observer);

    /**
     * Регистрирует наблюдателя и возвращает Disposable для управления подпиской.
     *
     * @param observer наблюдатель, который будет получать данные
     * @return Disposable для возможности отмены подписки
     */
    public Disposable subscribe(Observer<? super T> observer) {
        ObservableSubscribeProxy<T> proxy = new ObservableSubscribeProxy<>(observer);
        subscribeActual(proxy);
        return proxy;
    }

    /**
     * Преобразует элементы потока с помощью заданной функции.
     */
    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<R>() {
            @Override
            protected void subscribeActual(Observer<? super R> observer) {
                Observable.this.subscribeActual(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            observer.onNext(mapper.apply(item));
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        };
    }

    /**
     * Отфильтровывает элементы потока на основе предиката.
     *
     * @param predicate предикапт фильтрации
     * @return
     */
    public Observable<T> filter(Predicate<? super T> predicate) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                Observable.this.subscribeActual(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        };
    }

    /**
     * Преобразует каждый элемент в новый Observable и объединяет их результаты.
     *
     * @param mapper функция, преобразующая элементы в Observable
     * @return новый Observable с объединенными элементами
     * @param <R>
     */
    public <R> Observable<R> flatMap(Function<? super T, Observable<R>> mapper) {
        return new Observable<R>() {
            @Override
            protected void subscribeActual(Observer<? super R> observer) {
                try {
                    Observable.this.subscribeActual(new Observer<T>() {
                        private final ConcurrentLinkedQueue<Observable<R>> queue = new ConcurrentLinkedQueue<>();
                        private final AtomicBoolean isProcessing = new AtomicBoolean(false);
                        private volatile boolean sourceCompleted = false;

                        @Override
                        public void onNext(T item) {
                            try {
                                Observable<R> observable = mapper.apply(item);
                                queue.offer(observable);
                                processQueue(observer);
                            } catch (Throwable t) {
                                observer.onError(t);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            observer.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            sourceCompleted = true;
                            processQueue(observer);
                        }

                        private void processQueue(Observer<? super R> observer) {
                            if (isProcessing.compareAndSet(false, true)) {
                                try {
                                    while (!queue.isEmpty()) {
                                        Observable<R> next = queue.poll();
                                        if (next != null) {
                                            next.subscribeActual(new Observer<R>() {
                                                @Override
                                                public void onNext(R item) {
                                                    observer.onNext(item);
                                                }

                                                @Override
                                                public void onError(Throwable t) {
                                                    observer.onError(t);
                                                }

                                                @Override
                                                public void onComplete() {
                                                    processQueue(observer);
                                                }
                                            });
                                            return; // Обрабатываем по одному
                                        }
                                    }
                                    if (sourceCompleted) {
                                        observer.onComplete();
                                    }
                                } finally {
                                    isProcessing.set(false);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    observer.onError(e);
                }
            };
        };
    }

    /**
     * Задает планировщик для асинхронного выполнения подписки.
     *
     * @param scheduler планировщик для выполнения
     * @return
     */
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                scheduler.execute(() -> Observable.this.subscribeActual(observer));
            }
        };
    }

    /**
     * Задает планировщик для асинхронной обработки событий.
     *
     * @param scheduler планировщик для обработки
     * @return
     */
    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                Observable.this.subscribeActual(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(() -> observer.onComplete());
                    }
                });
            }
        };
    }
}