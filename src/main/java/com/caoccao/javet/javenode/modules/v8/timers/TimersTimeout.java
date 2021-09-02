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

package com.caoccao.javet.javenode.modules.v8.timers;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.modules.BaseFunction;
import com.caoccao.javet.javenode.utils.V8ValueExUtils;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.concurrent.TimeUnit;

public class TimersTimeout extends BaseFunction {
    protected int delay;
    protected Disposable disposable;
    protected V8Value[] v8ValueArgs;
    protected V8ValueFunction v8ValueFunctionCallback;

    public TimersTimeout(
            V8Runtime v8Runtime,
            V8ValueFunction v8ValueFunctionCallback,
            int delay,
            V8Value... v8ValueArgs) throws JavetException {
        super(v8Runtime);
        this.v8ValueArgs = V8ValueExUtils.toClone(v8ValueArgs);
        this.v8ValueFunctionCallback = v8ValueFunctionCallback.toClone();
        this.delay = delay;
        disposable = null;
    }

    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            if (hasRef()) {
                disposable.dispose();
                disposable = null;
            }
            JavetResourceUtils.safeClose(v8ValueFunctionCallback);
            v8ValueFunctionCallback = null;
            JavetResourceUtils.safeClose((Object[]) v8ValueArgs);
            v8ValueArgs = null;
        }
    }

    @V8Function
    public boolean hasRef() {
        return disposable != null && !disposable.isDisposed();
    }

    @Override
    public boolean isClosed() {
        return JavetResourceUtils.isClosed(v8ValueFunctionCallback);
    }

    @V8Function(thisObjectRequired = true)
    public V8Value ref(V8Value thisObject) {
        return thisObject;
    }

    @V8Function(thisObjectRequired = true)
    public V8Value refresh(V8Value thisObject) {
        if (hasRef()) {
            disposable.dispose();
            run();
        }
        return thisObject;
    }

    @Override
    public void run() {
        disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                .subscribe(t -> {
                    if (!isClosed()) {
                        v8ValueFunctionCallback.call(null, v8ValueArgs);
                    }
                });
    }

    @V8Function(thisObjectRequired = true)
    public V8Value unref(V8Value thisObject) {
        if (hasRef()) {
            disposable.dispose();
            disposable = null;
        }
        return thisObject;
    }
}
