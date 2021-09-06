/*
 * Copyright (c) 2021. caoccao.com Sam Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.caoccao.javet.javenode;

import com.caoccao.javet.exceptions.JavetError;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetClosable;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.utils.SimpleMap;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JNEventLoop implements IJavetClosable {

    public static final int AWAIT_SLEEP_INTERVAL_IN_MILLIS = 1;
    public static final int DEFAULT_AWAIT_TIMEOUT = 60;
    public static final TimeUnit DEFAULT_AWAIT_TIME_UNIT = TimeUnit.SECONDS;
    public static final int DEFAULT_THREAD_POOL_SIZE = 4;

    protected TimeUnit awaitTimeUnit;
    protected long awaitTimeout;
    protected AtomicInteger blockingEventCount;
    protected volatile boolean closed;
    protected ExecutorService executorService;
    protected V8Runtime v8Runtime;

    public JNEventLoop(V8Runtime v8Runtime) {
        this(v8Runtime, Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE));
    }

    public JNEventLoop(V8Runtime v8Runtime, ExecutorService executorService) {
        awaitTimeout = DEFAULT_AWAIT_TIMEOUT;
        awaitTimeUnit = DEFAULT_AWAIT_TIME_UNIT;
        blockingEventCount = new AtomicInteger();
        closed = false;
        this.executorService = Objects.requireNonNull(executorService);
        this.v8Runtime = Objects.requireNonNull(v8Runtime);
    }

    public boolean await() throws InterruptedException {
        return await(awaitTimeout, awaitTimeUnit);
    }

    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        assert timeout > 0;
        Objects.requireNonNull(timeUnit);
        long totalMillis = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        long startMillis = System.currentTimeMillis();
        while (blockingEventCount.get() > 0) {
            if (System.currentTimeMillis() - startMillis >= totalMillis) {
                return false;
            }
            TimeUnit.MILLISECONDS.sleep(AWAIT_SLEEP_INTERVAL_IN_MILLIS);
        }
        return true;
    }

    protected boolean awaitTermination() throws InterruptedException {
        return executorService.awaitTermination(awaitTimeout, awaitTimeUnit);
    }

    @Override
    public synchronized void close() throws JavetException {
        if (!isClosed()) {
            if (executorService.isShutdown() || executorService.isTerminated()) {
                closed = true;
            } else {
                try {
                    await();
                    executorService.shutdown();
                    if (!awaitTermination()) {
                        executorService.shutdownNow();
                        if (!awaitTermination()) {
                            throw new JavetException(
                                    JavetError.RuntimeCloseFailure,
                                    SimpleMap.of(JavetError.PARAMETER_MESSAGE, "Failed to shutdown the event loop"));
                        }
                    }
                } catch (JavetException e) {
                    throw e;
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    throw new JavetException(
                            JavetError.RuntimeCloseFailure,
                            SimpleMap.of(JavetError.PARAMETER_MESSAGE, "Event loop shutdown was interrupted"));
                } finally {
                    closed = true;
                }
            }
        }
    }

    public void decrementBlockingEventCount() {
        blockingEventCount.decrementAndGet();
    }

    public TimeUnit getAwaitTimeUnit() {
        return awaitTimeUnit;
    }

    public long getAwaitTimeout() {
        return awaitTimeout;
    }

    public int getBlockingEventCount() {
        return blockingEventCount.get();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public V8Runtime getV8Runtime() {
        return v8Runtime;
    }

    public void incrementBlockingEventCount() {
        blockingEventCount.incrementAndGet();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void setAwaitTimeUnit(TimeUnit awaitTimeUnit) {
        this.awaitTimeUnit = Objects.requireNonNull(awaitTimeUnit);
    }

    public void setAwaitTimeout(long awaitTimeout) {
        assert awaitTimeout > 0;
        this.awaitTimeout = awaitTimeout;
    }
}
