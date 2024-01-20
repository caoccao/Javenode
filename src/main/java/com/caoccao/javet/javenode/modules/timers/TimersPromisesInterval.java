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

package com.caoccao.javet.javenode.modules.timers;

import com.caoccao.javet.enums.V8ValueSymbolType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.callback.IJavetDirectCallable;
import com.caoccao.javet.interop.callback.JavetCallbackContext;
import com.caoccao.javet.interop.callback.JavetCallbackType;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.BaseJNFunction;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;
import com.caoccao.javet.values.reference.builtin.V8ValueBuiltInPromise;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimersPromisesInterval extends BaseJNFunction implements IJavetDirectCallable {
    protected static final String FUNCTION_GET_ASYNC_ITERATOR = "getAsyncIterator";
    protected static final String FUNCTION_NEXT = "next";
    protected static final String PROPERTY_ASYNC_ITERATOR = "asyncIterator";
    protected static final String PROPERTY_DONE = "done";
    protected static final String PROPERTY_VALUE = "value";
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

    public V8Value asyncIterator() throws JavetException {
        if (isClosed()) {
            return getV8Runtime().createV8ValueUndefined();
        }
        return getV8Runtime().createV8ValueFunction(new JavetCallbackContext(
                FUNCTION_GET_ASYNC_ITERATOR,
                this, JavetCallbackType.DirectCallNoThisAndResult,
                (IJavetDirectCallable.NoThisAndResult<Exception>) this::getAsyncIterator));
    }

    @Override
    public void close() {
        if (!isClosed()) {
            active.set(false);
            JavetResourceUtils.safeClose(v8Value);
            v8Value = null;
        }
    }

    protected V8ValueObject getAsyncIterator(V8Value... v8Values) throws JavetException {
        V8ValueObject v8ValueObjectAsyncGenerator = getV8Runtime().createV8ValueObject();
        v8ValueObjectAsyncGenerator.bind(
                new IJavetDirectCallable() {
                    private JavetCallbackContext[] javetCallbackContexts;

                    @Override
                    public JavetCallbackContext[] getCallbackContexts() {
                        if (javetCallbackContexts == null) {
                            javetCallbackContexts = new JavetCallbackContext[]{
                                    new JavetCallbackContext(
                                            FUNCTION_NEXT,
                                            this, JavetCallbackType.DirectCallNoThisAndResult,
                                            (IJavetDirectCallable.NoThisAndResult<Exception>) this::next),
                            };
                        }
                        return javetCallbackContexts;
                    }

                    public V8ValuePromise next(V8Value... v8Values) throws JavetException {
                        boolean actualResolve = resolve;
                        if (actualResolve) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(delay);
                            } catch (InterruptedException e) {
                                actualResolve = false;
                            }
                        }
                        try (V8ValueObject v8ValueObjectResult = getV8Runtime().createV8ValueObject();
                             V8ValueBuiltInPromise v8ValueBuiltInPromise =
                                     getV8Runtime().getGlobalObject().getBuiltInPromise();
                             V8Value actualV8Value = v8Value.toClone()) {
                            v8ValueObjectResult.set(PROPERTY_VALUE, actualV8Value);
                            v8ValueObjectResult.set(PROPERTY_DONE, false);
                            if (actualResolve) {
                                return v8ValueBuiltInPromise.resolve(v8ValueObjectResult);
                            } else {
                                return v8ValueBuiltInPromise.reject(v8ValueObjectResult);
                            }
                        }
                    }
                }
        );
        return v8ValueObjectAsyncGenerator;
    }

    @Override
    public JavetCallbackContext[] getCallbackContexts() {
        if (javetCallbackContexts == null) {
            javetCallbackContexts = new JavetCallbackContext[]{
                    new JavetCallbackContext(
                            PROPERTY_ASYNC_ITERATOR,
                            V8ValueSymbolType.BuiltIn,
                            this, JavetCallbackType.DirectCallGetterAndNoThis,
                            (IJavetDirectCallable.GetterAndNoThis<Exception>) this::asyncIterator),
            };
        }
        return javetCallbackContexts;
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
