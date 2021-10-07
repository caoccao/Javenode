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
import com.caoccao.javet.interfaces.IJavetLogger;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.JNDynamicModuleResolver;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.utils.SimpleMap;
import io.vertx.core.Vertx;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JNEventLoop implements IJavetClosable {

    public static final int AWAIT_SLEEP_INTERVAL_IN_MILLIS = 1;
    public static final int DEFAULT_AWAIT_TIMEOUT = 60;
    public static final TimeUnit DEFAULT_AWAIT_TIME_UNIT = TimeUnit.SECONDS;

    protected TimeUnit awaitTimeUnit;
    protected long awaitTimeout;
    protected AtomicInteger blockingEventCount;
    protected volatile boolean closed;
    protected JNDynamicModuleResolver dynamicModuleResolver;
    protected JNEventLoopOptions options;
    protected ReadWriteLock readWriteLockForStaticModuleMap;
    protected Map<String, IJNModule> staticModuleMap;
    protected V8Runtime v8Runtime;
    protected Vertx vertx;

    public JNEventLoop(V8Runtime v8Runtime) {
        this(v8Runtime, new JNEventLoopOptions());
    }

    public JNEventLoop(V8Runtime v8Runtime, JNEventLoopOptions options) {
        awaitTimeout = DEFAULT_AWAIT_TIMEOUT;
        awaitTimeUnit = DEFAULT_AWAIT_TIME_UNIT;
        blockingEventCount = new AtomicInteger();
        closed = false;
        dynamicModuleResolver = new JNDynamicModuleResolver(this);
        this.options = Objects.requireNonNull(options);
        readWriteLockForStaticModuleMap = new ReentrantReadWriteLock();
        staticModuleMap = new HashMap<>();
        this.v8Runtime = Objects.requireNonNull(v8Runtime);
        v8Runtime.setV8ModuleResolver(dynamicModuleResolver);
        vertx = Vertx.vertx(options.getVertxOptions());
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

    @Override
    public synchronized void close() throws JavetException {
        if (!isClosed()) {
            try {
                if (!await()) {
                    throw new JavetException(
                            JavetError.RuntimeCloseFailure,
                            SimpleMap.of(JavetError.PARAMETER_MESSAGE, "Failed to shutdown the event loop"));
                }
            } catch (JavetException e) {
                throw e;
            } catch (InterruptedException e) {
                throw new JavetException(
                        JavetError.RuntimeCloseFailure,
                        SimpleMap.of(JavetError.PARAMETER_MESSAGE, "Event loop shutdown was interrupted"));
            } finally {
                closed = true;
            }
            if (closed) {
                JavetResourceUtils.safeClose(dynamicModuleResolver);
                vertx.close().toCompletionStage();
                Lock writeLock = readWriteLockForStaticModuleMap.writeLock();
                try {
                    writeLock.lock();
                    for (IJNModule iJNModule : staticModuleMap.values()) {
                        try {
                            iJNModule.unbind();
                        } catch (Throwable t) {
                            getLogger().logError(
                                    t, "Failed to unbind {0}.", iJNModule.getType().getName());
                        } finally {
                            JavetResourceUtils.safeClose(iJNModule);
                        }
                    }
                    staticModuleMap.clear();
                } finally {
                    writeLock.unlock();
                }
                if (options.isGcBeforeClosing()) {
                    getV8Runtime().lowMemoryNotification();
                }
            }
        }
    }

    public JNEventLoop decrementBlockingEventCount() {
        blockingEventCount.decrementAndGet();
        return this;
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

    public IJavetLogger getLogger() {
        return getV8Runtime().getLogger();
    }

    public V8Runtime getV8Runtime() {
        return v8Runtime;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public JNEventLoop incrementBlockingEventCount() {
        blockingEventCount.incrementAndGet();
        return this;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public int loadStaticModules(JNModuleType... jnModuleTypes) throws JavetException {
        if (isClosed()) {
            return 0;
        }
        int loadedModuleCount = 0;
        if (jnModuleTypes.length > 0) {
            Lock writeLock = readWriteLockForStaticModuleMap.writeLock();
            try {
                writeLock.lock();
                for (JNModuleType jnModuleType : jnModuleTypes) {
                    if (jnModuleType == null) {
                        continue;
                    }
                    String moduleName = jnModuleType.getName();
                    if (moduleName == null || staticModuleMap.containsKey(moduleName)) {
                        continue;
                    }
                    try {
                        Class<? extends IJNModule> moduleClass = jnModuleType.getModuleClass();
                        Constructor constructor = moduleClass.getConstructor(getClass());
                        IJNModule iJNModule = (IJNModule) constructor.newInstance(this);
                        iJNModule.bind();
                        staticModuleMap.put(moduleName, iJNModule);
                        ++loadedModuleCount;
                    } catch (JavetException e) {
                        throw e;
                    } catch (Throwable t) {
                        getLogger().logError(t, "Failed to load static module {0}.", moduleName);
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
        return loadedModuleCount;
    }

    public int registerDynamicModules(JNModuleType... jnModuleTypes) {
        if (!isClosed()) {
            return dynamicModuleResolver.registerModules(jnModuleTypes);
        }
        return 0;
    }

    public JNEventLoop setAwaitTimeUnit(TimeUnit awaitTimeUnit) {
        this.awaitTimeUnit = Objects.requireNonNull(awaitTimeUnit);
        return this;
    }

    public JNEventLoop setAwaitTimeout(long awaitTimeout) {
        assert awaitTimeout > 0;
        this.awaitTimeout = awaitTimeout;
        return this;
    }

    public int unloadStaticModules(JNModuleType... jnModuleTypes) throws JavetException {
        if (isClosed() || jnModuleTypes.length == 0) {
            return 0;
        }
        int unloadedModuleCount = 0;
        Lock writeLock = readWriteLockForStaticModuleMap.writeLock();
        try {
            writeLock.lock();
            for (JNModuleType jnModuleType : jnModuleTypes) {
                if (jnModuleType == null) {
                    continue;
                }
                if (staticModuleMap.containsKey(jnModuleType.getName())) {
                    IJNModule iJNModule = staticModuleMap.get(jnModuleType.getName());
                    try {
                        iJNModule.unbind();
                    } catch (Throwable t) {
                        getLogger().logError(
                                t, "Failed to unbind {0}.", iJNModule.getType().getName());
                    } finally {
                        JavetResourceUtils.safeClose(iJNModule);
                        staticModuleMap.remove(jnModuleType.getName());
                        ++unloadedModuleCount;
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
        return unloadedModuleCount;
    }
}
