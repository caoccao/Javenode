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

import com.caoccao.javet.exceptions.JavetError;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNPrivatePropertyEnum;
import com.caoccao.javet.javenode.interfaces.IJNFunction;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseJNFunction implements IJNFunction {
    protected final static AtomicInteger GLOBAL_REFERENCE_ID = new AtomicInteger(0);
    protected JNEventLoop eventLoop;
    protected int referenceId;

    public BaseJNFunction(JNEventLoop eventLoop) {
        this.eventLoop = eventLoop;
        referenceId = GLOBAL_REFERENCE_ID.incrementAndGet();
    }

    public static int getGlobalReferenceId() {
        return GLOBAL_REFERENCE_ID.get();
    }

    @Override
    public JNEventLoop getEventLoop() {
        return eventLoop;
    }

    @Override
    public int getReferenceId() {
        return referenceId;
    }

    @Override
    public V8Value toV8Value() throws JavetException {
        V8Runtime v8Runtime = getEventLoop().getV8Runtime();
        if (JavetResourceUtils.isClosed(v8Runtime)) {
            throw new JavetException(JavetError.RuntimeAlreadyClosed);
        }
        V8ValueObject v8ValueObject = v8Runtime.createV8ValueObject();
        v8ValueObject.bind(this);
        v8ValueObject.setPrivateProperty(JNPrivatePropertyEnum.REFERENCE_ID, getReferenceId());
        return v8ValueObject;
    }
}
