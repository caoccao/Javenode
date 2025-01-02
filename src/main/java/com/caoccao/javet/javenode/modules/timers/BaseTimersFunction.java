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

package com.caoccao.javet.javenode.modules.timers;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.BaseJNFunction;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseTimersFunction extends BaseJNFunction {
    protected AtomicBoolean active;
    protected long delay;
    protected boolean recurrent;
    protected long timerId;
    protected V8Value[] v8ValueArgs;
    protected V8ValueFunction v8ValueFunctionCallback;

    public BaseTimersFunction(
            IJNModule parentModule,
            boolean recurrent,
            V8ValueFunction v8ValueFunctionCallback,
            long delay,
            V8Value... v8ValueArgs) throws JavetException {
        super(parentModule);
        active = new AtomicBoolean(false);
        this.recurrent = recurrent;
        this.v8ValueArgs = JavetResourceUtils.toClone(v8ValueArgs);
        this.delay = delay;
        this.v8ValueFunctionCallback = Objects.requireNonNull(v8ValueFunctionCallback).toClone();
        timerId = TimersConstants.DEFAULT_TIMER_ID;
    }

    protected void cancel() {
        if (hasRef()) {
            active.set(false);
            getEventLoop().getVertx().cancelTimer(timerId);
            timerId = TimersConstants.DEFAULT_TIMER_ID;
            if (!recurrent) {
                getEventLoop().decrementBlockingEventCount();
            }
        }
    }

    @Override
    public void close() {
        if (!isClosed()) {
            cancel();
            JavetResourceUtils.safeClose(v8ValueFunctionCallback);
            v8ValueFunctionCallback = null;
            JavetResourceUtils.safeClose((Object[]) v8ValueArgs);
            v8ValueArgs = null;
        }
    }

    public boolean hasRef() {
        return active.get();
    }

    public V8Value hasRef(V8Value... v8Values) throws JavetException {
        return getV8Runtime().createV8ValueBoolean(hasRef());
    }

    @Override
    public boolean isClosed() {
        return JavetResourceUtils.isClosed(v8ValueFunctionCallback);
    }

    public V8Value ref(V8Value thisObject, V8Value... v8Values) {
        return thisObject;
    }

    @Override
    public void run() {
        active.set(true);
        if (recurrent) {
            timerId = getEventLoop().getVertx().setPeriodic(delay, id -> {
                if (!isClosed()) {
                    try {
                        v8ValueFunctionCallback.call(null, v8ValueArgs);
                    } catch (Throwable t) {
                        getLogger().logError(t, "Failed to call a function.");
                    }
                }
            });
        } else {
            getEventLoop().incrementBlockingEventCount();
            timerId = getEventLoop().getVertx().setTimer(delay, id -> {
                if (!isClosed()) {
                    try {
                        v8ValueFunctionCallback.call(null, v8ValueArgs);
                    } catch (Throwable t) {
                        getLogger().logError(t, "Failed to call a function.");
                    } finally {
                        active.set(false);
                        getEventLoop().decrementBlockingEventCount();
                    }
                }
            });
        }
    }

    public V8Value unref(V8Value thisObject, V8Value... v8Values) {
        cancel();
        return thisObject;
    }
}
