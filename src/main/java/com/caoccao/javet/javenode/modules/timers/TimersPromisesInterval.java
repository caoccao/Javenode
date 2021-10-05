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

package com.caoccao.javet.javenode.modules.timers;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.annotations.V8Property;
import com.caoccao.javet.enums.V8ValueSymbolType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetAnonymous;
import com.caoccao.javet.interop.V8Scope;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.BaseJNFunction;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;

import java.util.concurrent.atomic.AtomicBoolean;

public class TimersPromisesInterval extends BaseJNFunction {
    protected final AtomicBoolean active;
    protected final long delay;
    protected final boolean resolve;
    protected V8Value v8Value;

    public TimersPromisesInterval(
            IJNModule parentModule,
            long delay,
            V8Value v8Value,
            boolean resolve) throws JavetException {
        super(parentModule);
        active = new AtomicBoolean(false);
        this.delay = delay;
        this.resolve = resolve;
        this.v8Value = v8Value == null ? getV8Runtime().createV8ValueNull() : v8Value.toClone();
    }

    @V8Property(name = "asyncIterator", symbolType = V8ValueSymbolType.BuiltIn)
    public V8ValueObject asyncIterator() throws JavetException {
        try (V8Scope v8Scope = getV8Runtime().getV8Scope()) {
            V8ValueObject v8ValueObject = v8Scope.createV8ValueObject();
            v8ValueObject.bind(new IJavetAnonymous() {
                @V8Function
                public V8ValuePromise next() throws JavetException {
                    TimersPromisesTimeout timersPromisesTimeout = new TimersPromisesTimeout(
                            getParentModule(), false, delay, v8Value, resolve);
                    timersPromisesTimeout.run();
                    return timersPromisesTimeout.getV8ValuePromiseResolver().getPromise();
                }
            });
            v8Scope.setEscapable();
            return v8ValueObject;
        }
    }

    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            active.set(false);
            JavetResourceUtils.safeClose(v8Value);
            v8Value = null;
        }
    }

    @Override
    public boolean isClosed() {
        return !active.get();
    }

    @Override
    public void run() {
        active.set(true);
    }
}
