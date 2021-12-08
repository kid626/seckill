package com.bruce.seckill.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;

@Slf4j
public class ThreadPoolUtil implements Closeable {

    @PostConstruct
    public void init() {
        final StringBuilder message = new StringBuilder();
        //线程池监控，定时打印线程池状态
        ScheduledThreadPoolExecutor timerExecutor =
                (ScheduledThreadPoolExecutor)
                        new ThreadPoolUtil.Builder()
                                .setCore(1)
                                .setPrefix("thread-pool-util-monitor")
                                .setScheduled(true)
                                .build();
        timerExecutor.scheduleAtFixedRate(
                () -> {
                    ConcurrentHashMap<String, ThreadPoolExecutor> map = ThreadPoolUtil.getThreadPoolExecutorConcurrentHashMap();
                    message.delete(0, message.length());
                    map.forEach(
                            (k, v) -> message.append(k).append(", ").append(v.toString()).append("\n"));
                    log.info(
                            "\nall thread pools(count: {}) status:\n{}",
                            map.size(),
                            message.toString());
                },
                0,
                1,
                TimeUnit.HOURS);
    }


    public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER =
            (t, e) ->
                    log.error(
                            "[thread:" + t.getName() + "] priority:" + t.getPriority() + ", " + e.getMessage(),
                            e);
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final ConcurrentHashMap<String, ThreadPoolExecutor>
            THREAD_POOL_EXECUTOR_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ThreadPoolExecutor> getThreadPoolExecutorConcurrentHashMap() {
        return THREAD_POOL_EXECUTOR_CONCURRENT_HASH_MAP;
    }

    public static ThreadPoolExecutor get(String name) {
        return THREAD_POOL_EXECUTOR_CONCURRENT_HASH_MAP.get(name);
    }

    public static void remove(String name) {
        THREAD_POOL_EXECUTOR_CONCURRENT_HASH_MAP.remove(name);
    }

    @Override
    public void close() {
        THREAD_POOL_EXECUTOR_CONCURRENT_HASH_MAP.values().forEach(ThreadPoolExecutor::shutdown);
    }

    @Getter
    public static class Builder {

        private int dynamicCount;
        private int core = 1;
        private int max = core * 2;
        private int queueSize = -1;
        private long keepAliveTime = 0;
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        private RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        private String format;
        private UncaughtExceptionHandler uncaughtExceptionHandler = DEFAULT_UNCAUGHT_EXCEPTION_HANDLER;
        private boolean scheduled;

        public Builder setCore(int core) {
            this.core = core;
            return this;
        }

        public Builder setMax(int max) {
            this.max = max;
            return this;
        }

        public Builder setQueueSize(int queueSize) {
            this.queueSize = queueSize;
            return this;
        }

        public Builder setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
            return this;
        }

        public Builder setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder setHandler(RejectedExecutionHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder setPrefix(String prefix) {
            this.format = prefix;
            return this;
        }

        public Builder setCoresDynamic(int dynamic) {
            this.dynamicCount = dynamic;
            return this;
        }

        public Builder setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
            return this;
        }

        public Builder setScheduled(boolean scheduled) {
            this.scheduled = scheduled;
            return this;
        }

        public synchronized ThreadPoolExecutor build() {
            String poolName = StringUtils.defaultString(format, "sm-default-pool-thread-%s");
            return THREAD_POOL_EXECUTOR_CONCURRENT_HASH_MAP.computeIfAbsent(
                    poolName,
                    name -> {
                        ThreadFactoryBuilder threadFactoryBuilder =
                                new ThreadFactoryBuilder().setNameFormat(poolName);
                        if (uncaughtExceptionHandler != null) {
                            threadFactoryBuilder.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                        }
                        if (CORES >= this.dynamicCount && this.dynamicCount > 0) {
                            core = CORES;
                            max = core * 2;
                            log.info("thread pool {} use dynamic config, core: {}, max: {}", poolName, core, max);
                        }
                        if (core < 0) {
                            log.warn("thread pool {} core must be gte 0, will set to cores: {}", poolName, CORES);
                            core = CORES;
                        }
                        if (max < core) {
                            log.warn(
                                    "thread pool {} max must be gte core, core: {}, max: {}, will set max eq core",
                                    poolName,
                                    core,
                                    max);
                        }
                        if (scheduled) {
                            return new ScheduledThreadPoolExecutor(core, threadFactoryBuilder.build(), handler);
                        }
                        return new ThreadPoolExecutor(
                                core,
                                max,
                                keepAliveTime,
                                timeUnit,
                                new LinkedBlockingQueue<>(queueSize == -1 ? max * 2 : queueSize),
                                threadFactoryBuilder.build(),
                                handler);
                    });
        }
    }
}
