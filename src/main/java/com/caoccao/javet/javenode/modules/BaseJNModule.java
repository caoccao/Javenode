/*
 * Copyright (c) 2021-2024. caoccao.com Sam Cao
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
import com.caoccao.javet.interop.callback.JavetCallbackContext;
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

public abstract class BaseJNModule extends BaseJNCallable implements IJNModule {
    protected final ReadWriteLock readWriteLock;
    private final JNEventLoop eventLoop;
    private final Map<Integer, IJNFunction> functionMap;
    protected volatile boolean closed;

    public BaseJNModule(JNEventLoop eventLoop) {
        super();
        closed = false;
        this.eventLoop = Objects.requireNonNull(eventLoop);
        functionMap = new TreeMap<>();
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public void close() {
        if (!isClosed()) {
            Lock writeLock = readWriteLock.writeLock();
            try {
                writeLock.lock();
                for (IJNFunction iJNFunction : functionMap.values()) {
                    JavetResourceUtils.safeClose(iJNFunction);
                    functionMap.remove(iJNFunction.getReferenceId());
                }
                super.close();
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

    @Override
    public <F extends IJNFunction> F getFunction(V8ValueObject v8ValueObject) throws JavetException {
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

    @Override
    public <F extends IJNFunction> F putFunction(F iJNFunction) {
        Objects.requireNonNull(iJNFunction);
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            functionMap.put(iJNFunction.getReferenceId(), iJNFunction);
            return iJNFunction;
        } finally {
            writeLock.unlock();
        }
    }
}
