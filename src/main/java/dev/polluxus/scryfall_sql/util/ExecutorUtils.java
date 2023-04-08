package dev.polluxus.scryfall_sql.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorUtils {

    public static ExecutorService directExecutorService() {
        return new DirectExecutorService();
    }

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
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit){
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(Runnable command) {
            submit(command);
        }
    }
}
