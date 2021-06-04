package com.example.pbk_test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApplicationExecutors {


    private final ThreadPoolExecutor background;
    // private final ExecutorService mainThread;

    public ThreadPoolExecutor getBackground() {
        return background;
    }

    /*
    public Executor getMainThread() {
        return mainThread;
    }
     */

    public ApplicationExecutors() {
        this.background = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        // this.mainThread = new MainThreadExecutor();
    }

    /*
    private static class MainThreadExecutor implements ExecutorService {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
     */
}
