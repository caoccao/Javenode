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
import com.caoccao.javet.interop.callback.IJavetDirectCallable;
import com.caoccao.javet.interop.callback.JavetCallbackContext;
import com.caoccao.javet.interop.callback.JavetCallbackType;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.modules.BaseJNModule;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueInteger;
import com.caoccao.javet.values.reference.V8ValuePromise;

public class TimersPromisesModule extends BaseJNModule implements IJavetDirectCallable {
    public static final String NAME = "timers/promises";

    public TimersPromisesModule(JNEventLoop eventLoop) {
        super(eventLoop);
    }

    protected long extractAndValidateDelay(V8Value[] v8ValueArgs) {
        long delay = TimersConstants.DEFAULT_DELAY;
        if (v8ValueArgs != null && v8ValueArgs.length > 0) {
            V8Value v8ValueDelay = v8ValueArgs[0];
            if (!(v8ValueDelay instanceof V8ValueInteger)) {
                throw new IllegalArgumentException("Argument [delay] must be a number");
            }
            delay = ((V8ValueInteger) v8ValueDelay).toPrimitive();
        }
        if (delay <= 0) {
            throw new IllegalArgumentException("Argument [delay] must be a positive number");
        }
        return delay;
    }

    @Override
    public JavetCallbackContext[] getCallbackContexts() {
        if (javetCallbackContexts == null) {
            javetCallbackContexts = new JavetCallbackContext[]{
                    new JavetCallbackContext(
                            "setImmediate",
                            this, JavetCallbackType.DirectCallNoThisAndResult,
                            (IJavetDirectCallable.NoThisAndResult<Exception>) this::setImmediate),
                    new JavetCallbackContext(
                            "setInterval",
                            this, JavetCallbackType.DirectCallNoThisAndResult,
                            (IJavetDirectCallable.NoThisAndResult<Exception>) this::setInterval),
                    new JavetCallbackContext(
                            "setTimeout",
                            this, JavetCallbackType.DirectCallNoThisAndResult,
                            (IJavetDirectCallable.NoThisAndResult<Exception>) this::setTimeout),
            };
        }
        return javetCallbackContexts;
    }

    @Override
    public JNModuleType getType() {
        return JNModuleType.TimersPromises;
    }

    public V8ValuePromise setImmediate(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueResult = null;
        if (v8ValueArgs != null && v8ValueArgs.length > 0) {
            v8ValueResult = v8ValueArgs[0];
        }
        TimersPromisesImmediate timersPromisesImmediate = new TimersPromisesImmediate(
                this, v8ValueResult, true);
        timersPromisesImmediate.run();
        return timersPromisesImmediate.getV8ValuePromiseResolver().getPromise();
    }

    public V8Value setInterval(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueResult = null;
        long delay = TimersConstants.DEFAULT_DELAY;
        boolean resolve = true;
        try {
            delay = extractAndValidateDelay(v8ValueArgs);
            if (v8ValueArgs != null && v8ValueArgs.length > 1) {
                v8ValueResult = v8ValueArgs[1];
            }
        } catch (Throwable t) {
            getLogger().logError(t, "Failed to execute setInterval().");
            v8ValueResult = getV8Runtime().createV8ValueString(t.getMessage());
            resolve = false;
        }
        TimersPromisesInterval timersPromisesInterval = new TimersPromisesInterval(
                this, delay, v8ValueResult, resolve);
        timersPromisesInterval.run();
        return timersPromisesInterval.toV8Value();
    }

    public V8ValuePromise setTimeout(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueResult = null;
        long delay = TimersConstants.DEFAULT_DELAY;
        boolean resolve = true;
        try {
            delay = extractAndValidateDelay(v8ValueArgs);
            if (v8ValueArgs != null && v8ValueArgs.length > 1) {
                v8ValueResult = v8ValueArgs[1];
            }
        } catch (Throwable t) {
            getLogger().logError(t, "Failed to execute setTimeout().");
            v8ValueResult = getV8Runtime().createV8ValueString(t.getMessage());
            resolve = false;
        }
        TimersPromisesTimeout timersPromisesTimeout = new TimersPromisesTimeout(
                this, false, delay, v8ValueResult, resolve);
        timersPromisesTimeout.run();
        return timersPromisesTimeout.getV8ValuePromiseResolver().getPromise();
    }
}
