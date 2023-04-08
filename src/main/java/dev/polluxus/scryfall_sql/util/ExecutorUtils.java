package dev.polluxus.scryfall_sql.util;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.etl.Etl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorUtils {

    private static final Logger log = LoggerFactory.getLogger(ExecutorUtils.class);

    public static ExecutorService directExecutorService() {
        return new DirectExecutorService();
    }

    public static ExecutorService getExecutor(Configuration config) {

        if (config.parallelism().equals(1)) {

            log.info("Running with parallelism of 1 (disabled)");
            return directExecutorService();
        }

        log.warn("Running with parallelism of {}", config.parallelism());

        return Executors.newFixedThreadPool(config.parallelism());
    }

    /**
     * An {@link ExecutorService} that runs all tasks on the calling thread,
     * i.e. synchronously.
     */
    private static class DirectExecutorService implements ExecutorService {

        boolean isShutdown = false;

        @Override
        public void shutdown() {

            isShutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return isShutdown;
        }

        @Override
        public boolean isTerminated() {
            return isShutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {

            try {
                final T result = task.call();
                return CompletableFuture.completedFuture(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {

            try {
                task.run();
                return CompletableFuture.completedFuture(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Future<?> submit(Runnable task) {

            return submit(task, null);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {

            if (tasks == null) {
                return Collections.emptyList();
            }

            return tasks.stream()
                    .map(c -> {
                        try {
                            return c.call();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .map(c -> (Future<T>) CompletableFuture.completedFuture(c))
                    .toList();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return invokeAll(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {

            if (tasks == null) {
                return null;
            }

            for (var t : tasks) {
                try {
                    return t.call();
                } catch (Exception e) {
                    // maybe another one will work?
                }
            }

            throw new RuntimeException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit){
            return invokeAny(tasks);
        }

        @Override
        public void execute(Runnable command) {
            submit(command);
        }
    }
}
