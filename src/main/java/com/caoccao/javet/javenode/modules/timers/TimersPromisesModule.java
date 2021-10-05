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
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.modules.BaseJNModule;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueInteger;
import com.caoccao.javet.values.reference.V8ValuePromise;

public class TimersPromisesModule extends BaseJNModule {
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
    public JNModuleType getType() {
        return JNModuleType.TIMERS_PROMISES;
    }

    @V8Function
    public V8ValuePromise setImmediate(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueResult = null;
        if (v8ValueArgs != null && v8ValueArgs.length > 0) {
            v8ValueResult = v8ValueArgs[0];
        }
        if (v8ValueResult == null) {
            v8ValueResult = getEventLoop().getV8Runtime().createV8ValueNull();
        }
        TimersPromisesImmediate timersPromisesImmediate = new TimersPromisesImmediate(
                getEventLoop(), v8ValueResult, true);
        timersPromisesImmediate.run();
        return timersPromisesImmediate.getV8ValuePromiseResolver().getPromise();
    }

    @V8Function
    public V8ValuePromise setInterval(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueResult = null;
        TimersPromisesTimeout timersPromisesTimeout;
        try {
            long delay = extractAndValidateDelay(v8ValueArgs);
            if (v8ValueArgs != null && v8ValueArgs.length > 1) {
                v8ValueResult = v8ValueArgs[1];
            }
            if (v8ValueResult == null) {
                v8ValueResult = getEventLoop().getV8Runtime().createV8ValueNull();
            }
            timersPromisesTimeout = new TimersPromisesTimeout(
                    getEventLoop(), true, delay, v8ValueResult, true);
        } catch (Throwable t) {
            getEventLoop().getLogger().logError(t, "Failed to execute setInterval().");
            v8ValueResult = getEventLoop().getV8Runtime().createV8ValueString(t.getMessage());
            timersPromisesTimeout = new TimersPromisesTimeout(
                    getEventLoop(), true, TimersConstants.DEFAULT_DELAY, v8ValueResult, false);
        }
        timersPromisesTimeout.run();
        return timersPromisesTimeout.getV8ValuePromiseResolver().getPromise();
    }

    @V8Function
    public V8ValuePromise setTimeout(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueResult = null;
        TimersPromisesTimeout timersPromisesTimeout;
        try {
            long delay = extractAndValidateDelay(v8ValueArgs);
            if (v8ValueArgs != null && v8ValueArgs.length > 1) {
                v8ValueResult = v8ValueArgs[1];
            }
            if (v8ValueResult == null) {
                v8ValueResult = getEventLoop().getV8Runtime().createV8ValueNull();
            }
            timersPromisesTimeout = new TimersPromisesTimeout(
                    getEventLoop(), false, delay, v8ValueResult, true);
        } catch (Throwable t) {
            getEventLoop().getLogger().logError(t, "Failed to execute setTimeout().");
            v8ValueResult = getEventLoop().getV8Runtime().createV8ValueString(t.getMessage());
            timersPromisesTimeout = new TimersPromisesTimeout(
                    getEventLoop(), false, TimersConstants.DEFAULT_DELAY, v8ValueResult, false);
        }
        timersPromisesTimeout.run();
        return timersPromisesTimeout.getV8ValuePromiseResolver().getPromise();
    }
}
