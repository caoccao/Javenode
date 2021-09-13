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

package com.caoccao.javet.javenode.modules;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNPrivatePropertyEnum;
import com.caoccao.javet.javenode.interfaces.IJNFunction;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BaseJNModule implements IJNModule {
    protected volatile boolean closed;
    protected JNEventLoop eventLoop;
    protected ReadWriteLock readWriteLock;
    private Map<Integer, IJNFunction> functionMap;

    public BaseJNModule(JNEventLoop eventLoop) {
        closed = false;
        this.eventLoop = Objects.requireNonNull(eventLoop);
        functionMap = new TreeMap<>();
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            Lock writeLock = readWriteLock.writeLock();
            try {
                writeLock.lock();
                for (IJNFunction iJNFunction : functionMap.values()) {
                    JavetResourceUtils.safeClose(iJNFunction);
                    functionMap.remove(iJNFunction.getReferenceId());
                }
            } finally {
                writeLock.unlock();
                closed = true;
            }
        }
    }

    protected V8Value[] extractArgs(V8Value[] v8ValueArgs, int startIndex) {
        assert startIndex >= 0;
        if (v8ValueArgs.length > startIndex) {
            return Arrays.copyOfRange(v8ValueArgs, startIndex, v8ValueArgs.length);
        } else {
            return new V8Value[0];
        }
    }

    @Override
    public JNEventLoop getEventLoop() {
        return eventLoop;
    }

    protected <F extends IJNFunction> F getFunction(V8ValueObject v8ValueObject) throws JavetException {
        return getFunction(Objects.requireNonNull(v8ValueObject)
                .getPrivatePropertyInteger(JNPrivatePropertyEnum.REFERENCE_ID));
    }

    @SuppressWarnings("unchecked")
    protected <F extends IJNFunction> F getFunction(Integer referenceId) {
        if (referenceId != null) {
            Lock readLock = readWriteLock.readLock();
            try {
                readLock.lock();
                return (F) functionMap.get(referenceId);
            } finally {
                readLock.unlock();
            }
        }
        return null;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    protected IJNFunction putFunction(IJNFunction iJNFunction) {
        Objects.requireNonNull(iJNFunction);
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            return functionMap.put(iJNFunction.getReferenceId(), iJNFunction);
        } finally {
            writeLock.unlock();
        }
    }
}
