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

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.modules.BaseJNFunction;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValuePromise;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseTimersPromisesFunction extends BaseJNFunction {
    protected AtomicBoolean active;
    protected long delay;
    protected boolean recurrent;
    protected boolean resolve;
    protected long timerId;
    protected V8Value v8Value;
    protected V8ValuePromise v8ValuePromiseResolver;

    public BaseTimersPromisesFunction(
            JNEventLoop eventLoop,
            boolean recurrent,
            long delay,
            V8Value v8Value,
            boolean resolve) throws JavetException {
        super(eventLoop);
        active = new AtomicBoolean(false);
        this.recurrent = recurrent;
        this.delay = delay;
        this.resolve = resolve;
        timerId = TimersConstants.DEFAULT_TIMER_ID;
        this.v8Value = Objects.requireNonNull(v8Value).toClone();
        v8ValuePromiseResolver = null;
    }

    protected void cancel() {
        if (!isClosed()) {
            active.set(false);
            eventLoop.getVertx().cancelTimer(timerId);
            timerId = TimersConstants.DEFAULT_TIMER_ID;
            if (!recurrent) {
                eventLoop.decrementBlockingEventCount();
            }
        }
    }

    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            cancel();
            JavetResourceUtils.safeClose(v8Value);
            v8Value = null;
            JavetResourceUtils.safeClose(v8ValuePromiseResolver);
            v8ValuePromiseResolver = null;
        }
    }

    public V8ValuePromise getV8ValuePromiseResolver() {
        return v8ValuePromiseResolver;
    }

    @Override
    public boolean isClosed() {
        return !active.get();
    }

    @Override
    public void run() {
        active.set(true);
        try {
            final V8Value v8ValueResult = v8Value;
            v8ValuePromiseResolver = eventLoop.getV8Runtime().createV8ValuePromise();
            if (recurrent) {
                timerId = eventLoop.getVertx().setPeriodic(delay, id -> {
                    if (!isClosed()) {
                        if (resolve) {
                            try {
                                v8ValuePromiseResolver.resolve(v8ValueResult);
                            } catch (Throwable t) {
                                getEventLoop().getLogger().logError(t, "Failed to resolve the promise.");
                            }
                        } else {
                            try {
                                v8ValuePromiseResolver.reject(v8ValueResult);
                            } catch (Throwable t) {
                                getEventLoop().getLogger().logError(t, "Failed to reject the promise.");
                            }
                        }
                    }
                });
            } else {
                eventLoop.incrementBlockingEventCount();
                timerId = eventLoop.getVertx().setTimer(delay, id -> {
                    try {
                        if (!isClosed()) {
                            if (resolve) {
                                try {
                                    v8ValuePromiseResolver.resolve(v8ValueResult);
                                } catch (Throwable t) {
                                    getEventLoop().getLogger().logError(t, "Failed to resolve the promise.");
                                }
                            } else {
                                try {
                                    v8ValuePromiseResolver.reject(v8ValueResult);
                                } catch (Throwable t) {
                                    getEventLoop().getLogger().logError(t, "Failed to reject the promise.");
                                }
                            }
                        }
                    } finally {
                        JavetResourceUtils.safeClose(v8ValueResult, v8ValuePromiseResolver);
                        eventLoop.decrementBlockingEventCount();
                    }
                });
            }
        } catch (Throwable t) {
            getEventLoop().getLogger().logError(t, "Failed to create the promise.");
        }
    }
}
