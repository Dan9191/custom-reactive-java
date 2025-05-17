package org.example.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CustomReactiveTest {

    @Test
    @DisplayName("проверяет создание Observable и подписку на него")
    void testObservableCreateAndSubscribe() throws Exception {
        List<Integer> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Error occurred: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(List.of(1, 2, 3), results);

    }

    @Test
    @DisplayName("проверяет оператор map")
    void testMapOperator() throws Exception {
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable.map(x -> "Value: " + x)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Error occurred: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(List.of("Value: 1", "Value: 2", "Value: 3"), results);
    }


    @Test
    @DisplayName("проверяет оператор flatMap")
    void testFlatMapOperator() throws Exception {
        List<Integer> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        // Explicitly annotate the Function to ensure type clarity
        observable.flatMap((Function<Integer, Observable<Integer>>) x -> Observable.create(emitter -> {
                    emitter.onNext(x * 10);
                    emitter.onNext(x * 20);
                    emitter.onComplete();
                }))
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Error occurred: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(List.of(10, 20, 20, 40), results);
    }

    @Test
    @DisplayName("проверяет обработку ошибок")
    void testErrorHandling() throws Exception {
        List<Throwable> errors = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onError(new RuntimeException("Test error"));
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
            }

            @Override
            public void onError(Throwable t) {
                errors.add(t);
                latch.countDown();
            }

            @Override
            public void onComplete() {
            }
        });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(1, errors.size());
        assertEquals("Test error", errors.get(0).getMessage());
    }

    @Test
    @DisplayName("проверяет операторы subscribeOn и observeOn")
    void testSubscribeOnAndObserveOn() throws Exception {
        List<String> threadNames = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        Scheduler ioScheduler = new IOThreadScheduler();
        Scheduler computationScheduler = new ComputationScheduler();

        observable.subscribeOn(ioScheduler)
                .observeOn(computationScheduler)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        threadNames.add(Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Error occurred: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        threadNames.add(Thread.currentThread().getName());
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);
        assertFalse(threadNames.isEmpty());
        assertTrue(threadNames.stream().anyMatch(name -> name.contains("pool")));
    }

    @Test
    @DisplayName("проверяет механизм Disposable")
    void testDisposable() throws InterruptedException {
        List<Integer> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        final Disposable[] disposableHolder = new Disposable[1]; // Use an array to hold the Disposable

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        Scheduler scheduler = new SingleThreadScheduler(); // Use scheduler for async emission

        // Создание наблюдателя
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
                if (item == 2) {
                    disposableHolder[0].dispose(); // Access Disposable via the array
                }
            }

            @Override
            public void onError(Throwable t) {
                fail("Error occurred: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        // Подписка с использованием планировщика
        disposableHolder[0] = observable.subscribeOn(scheduler).subscribe(observer);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(List.of(1, 2), results);
        assertTrue(disposableHolder[0].isDisposed());

    }
}
