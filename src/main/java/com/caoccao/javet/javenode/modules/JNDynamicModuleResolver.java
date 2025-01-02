/*
 * Copyright (c) 2021-2025. caoccao.com Sam Cao
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

package com.caoccao.javet.javenode.modules;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetClosable;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.callback.IV8ModuleResolver;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.reference.IV8Module;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class JNDynamicModuleResolver implements IV8ModuleResolver, IJavetClosable {
    private final Map<String, DynamicModule> dynamicModuleMap;
    private final JNEventLoop eventLoop;
    private final ReadWriteLock readWriteLock;
    private volatile boolean closed;

    public JNDynamicModuleResolver(JNEventLoop eventLoop) {
        closed = false;
        dynamicModuleMap = new HashMap<>();
        this.eventLoop = eventLoop;
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public void close() throws JavetException {
        if (isClosed()) {
            return;
        }
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            for (DynamicModule dynamicModule : dynamicModuleMap.values()) {
                if (dynamicModule.jnModule != null && dynamicModule.bindingObject != null) {
                    dynamicModule.jnModule.unbind(dynamicModule.bindingObject);
                }
                JavetResourceUtils.safeClose(
                        dynamicModule.jnModule,
                        dynamicModule.bindingObject,
                        dynamicModule.v8Module);
            }
            dynamicModuleMap.clear();
        } finally {
            closed = true;
            writeLock.unlock();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public int registerModules(JNModuleType... jnModuleTypes) {
        if (jnModuleTypes.length == 0) {
            return 0;
        }
        int registeredModuleCount = 0;
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            for (JNModuleType jnModuleType : jnModuleTypes) {
                if (jnModuleType == null) {
                    continue;
                }
                String moduleName = jnModuleType.getName();
                if (moduleName == null || dynamicModuleMap.containsKey(moduleName)) {
                    continue;
                }
                DynamicModule dynamicModule = new DynamicModule();
                dynamicModule.type = jnModuleType;
                dynamicModuleMap.put(moduleName, dynamicModule);
                ++registeredModuleCount;
            }
        } finally {
            writeLock.unlock();
        }
        return registeredModuleCount;
    }

    @Override
    public IV8Module resolve(V8Runtime v8Runtime, String resourceName, IV8Module v8ModuleReferrer) throws JavetException {
        Lock readLock = readWriteLock.readLock();
        DynamicModule dynamicModule;
        try {
            readLock.lock();
            dynamicModule = dynamicModuleMap.get(resourceName);
        } finally {
            readLock.unlock();
        }
        if (dynamicModule != null) {
            Lock writeLock = readWriteLock.writeLock();
            try {
                writeLock.lock();
                if (dynamicModule.v8Module != null) {
                    return dynamicModule.v8Module;
                }
                dynamicModule.jnModule = dynamicModule.type.getModuleConstructor().apply(eventLoop);
                dynamicModule.bindingObject = eventLoop.getV8Runtime().createV8ValueObject();
                dynamicModule.jnModule.bind(dynamicModule.bindingObject);
                dynamicModule.v8Module = v8Runtime.createV8Module(resourceName, dynamicModule.bindingObject);
                return dynamicModule.v8Module;
            } catch (JavetException e) {
                throw e;
            } catch (Throwable t) {
                eventLoop.getLogger().logError(t, "Failed to resolve module {0}.", resourceName);
            } finally {
                writeLock.unlock();
            }
        }
        return null;
    }

    private static class DynamicModule {
        public V8ValueObject bindingObject;
        public IJNModule jnModule;
        public JNModuleType type;
        public IV8Module v8Module;
    }
}
