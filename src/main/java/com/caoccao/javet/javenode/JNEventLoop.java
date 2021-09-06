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
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.utils.SimpleMap;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    protected Map<String, IJNModule> moduleMap;
    protected ReadWriteLock readWriteLock;
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
        moduleMap = new HashMap<>();
        readWriteLock = new ReentrantReadWriteLock();
        this.v8Runtime = Objects.requireNonNull(v8Runtime);
    }

    public boolean await() throws InterruptedException {
        return await(awaitTimeout, awaitTimeUnit);
    }

    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (isClosed()) {
            return false;
        }
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
        if (isClosed()) {
            return false;
        }
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
            if (closed) {
                Lock writeLock = readWriteLock.writeLock();
                try {
                    writeLock.lock();
                    for (IJNModule iJNModule : moduleMap.values()) {
                        try {
                            iJNModule.unbind();
                        } catch (Throwable t) {
                        }
                        JavetResourceUtils.safeClose(iJNModule);
                        moduleMap.remove(iJNModule.getType().getName());
                    }
                    moduleMap.clear();
                } finally {
                    writeLock.unlock();
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

    public IJNModule loadModule(JNModuleType jnModuleType) throws JavetException {
        if (isClosed()) {
            return null;
        }
        Objects.requireNonNull(jnModuleType);
        String moduleName = jnModuleType.getName();
        Lock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            if (moduleMap.containsKey(moduleName)) {
                return moduleMap.get(moduleName);
            }
        } finally {
            readLock.unlock();
        }
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            Class<? extends IJNModule> moduleClass = jnModuleType.getModuleClass();
            Constructor constructor = moduleClass.getConstructor(getClass());
            IJNModule iJNModule = (IJNModule) constructor.newInstance(this);
            iJNModule.bind();
            moduleMap.put(moduleName, iJNModule);
            return iJNModule;
        } catch (JavetException e) {
            throw e;
        } catch (Throwable t) {
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    public void setAwaitTimeUnit(TimeUnit awaitTimeUnit) {
        this.awaitTimeUnit = Objects.requireNonNull(awaitTimeUnit);
    }

    public void setAwaitTimeout(long awaitTimeout) {
        assert awaitTimeout > 0;
        this.awaitTimeout = awaitTimeout;
    }

    public boolean unloadModule(JNModuleType jnModuleType) throws JavetException {
        if (isClosed()) {
            return false;
        }
        Objects.requireNonNull(jnModuleType);
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            if (moduleMap.containsKey(jnModuleType.getName())) {
                IJNModule iJNModule = moduleMap.get(jnModuleType.getName());
                iJNModule.unbind();
                moduleMap.remove(jnModuleType.getName());
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }
}
