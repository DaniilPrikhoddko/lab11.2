package org.example;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolExample {

    private static final Object lock = new Object();
    private static final int QUEUE_CAPACITY = 3;

    public static void main(String[] args) throws InterruptedException {

        // создаем пул для выполнения наших задач
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(QUEUE_CAPACITY));

        // сколько задач выполнилось
        AtomicInteger count = new AtomicInteger(0);

        // сколько задач выполняется
        AtomicInteger inProgress = new AtomicInteger(0);

        // отправляем задачи на выполнение
        for (int i = 0; i < 30; i++) {
            final int number = i;
            System.out.println("creating #" + number);

            synchronized (lock) {
                while (executor.getQueue().size() >= 3) {
                    lock.wait();
                }

                executor.submit(() -> {
                    int working;
                    working = inProgress.incrementAndGet();
                    System.out.println("start #" + number + ", in progress: " + working);
                    try {
                        // тут какая-то полезная работа
                        Thread.sleep(Math.round(1000 + Math.random() * 2000));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    working = inProgress.decrementAndGet();
                    System.out.println("end #" + number + ", in progress: " + working + ", done tasks: " + count.incrementAndGet());
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                    return null;
                });
            }
        }

        executor.shutdown();
    }
}
