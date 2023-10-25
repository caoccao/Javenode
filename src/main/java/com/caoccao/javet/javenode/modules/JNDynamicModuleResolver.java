/*
 * Copyright (c) 2021-2023. caoccao.com Sam Cao
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

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetClosable;
import com.caoccao.javet.interfaces.IV8ModuleResolver;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.reference.IV8Module;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class JNDynamicModuleResolver implements IV8ModuleResolver, IJavetClosable {
    private static final String DUMMY_VAR_NAME = "dummy";
    private boolean closed;
    private Map<String, DynamicModule> dynamicModuleMap;
    private JNEventLoop eventLoop;
    private ReadWriteLock readWriteLock;

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
                JavetResourceUtils.safeClose(dynamicModule.jnModule, dynamicModule.bindingObject, dynamicModule.v8Module);
            }
            dynamicModuleMap.clear();
        } finally {
            closed = true;
            writeLock.unlock();
        }
    }

    private String getSourceCode(JNModuleType jnModuleType) {
        Class<? extends IJNModule> moduleClass = jnModuleType.getModuleClass();
        StringBuilder stringBuilder = new StringBuilder();
        for (Method method : moduleClass.getMethods()) {
            if (method.isAnnotationPresent(V8Function.class)) {
                V8Function v8Function = method.getAnnotation(V8Function.class);
                String functionName = v8Function.name() == null || v8Function.name().length() == 0 ?
                        method.getName() : v8Function.name();
                stringBuilder.append("export const ").append(functionName).append(" = ").append(DUMMY_VAR_NAME).append(".")
                        .append(functionName).append("? ").append(DUMMY_VAR_NAME).append(".")
                        .append(functionName).append(": undefined;\n");
            }
        }
        return stringBuilder.toString();
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
        DynamicModule dynamicModule = null;
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
                String sourceCode = getSourceCode(dynamicModule.type);
                Class<? extends IJNModule> moduleClass = dynamicModule.type.getModuleClass();
                Constructor constructor = moduleClass.getConstructor(eventLoop.getClass());
                dynamicModule.jnModule = (IJNModule) constructor.newInstance(eventLoop);
                dynamicModule.bindingObject = eventLoop.getV8Runtime().createV8ValueObject();
                dynamicModule.jnModule.bind(dynamicModule.bindingObject);
                eventLoop.getV8Runtime().getGlobalObject().set(DUMMY_VAR_NAME, dynamicModule.bindingObject);
                dynamicModule.v8Module = v8Runtime.getExecutor(sourceCode)
                        .setResourceName(resourceName).compileV8Module();
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
