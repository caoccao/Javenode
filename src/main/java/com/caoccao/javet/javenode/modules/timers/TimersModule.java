/*
 * Copyright (c) 2021-2021. caoccao.com Sam Cao
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
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.text.MessageFormat;

public class TimersModule extends BaseJNModule {
    public static final String NAME = "timers";

    public TimersModule(JNEventLoop eventLoop) {
        super(eventLoop);
    }

    protected V8Value cancel(V8Value v8Value, String variableName) throws JavetException {
        if (!(v8Value instanceof V8ValueObject)) {
            throw new IllegalArgumentException(MessageFormat.format("Argument [{0}] is invalid", variableName));
        }
        BaseTimersFunction baseTimersFunction = getFunction((V8ValueObject) v8Value);
        if (baseTimersFunction == null) {
            throw new IllegalArgumentException(MessageFormat.format("Argument [{0}] is invalid", variableName));
        }
        baseTimersFunction.cancel();
        return eventLoop.getV8Runtime().createV8ValueUndefined();
    }

    @V8Function
    public V8Value clearImmediate(V8Value... v8ValueArgs) throws JavetException {
        if (v8ValueArgs == null || v8ValueArgs.length != 1) {
            throw new IllegalArgumentException("clearImmediate() takes 1 argument");
        }
        return cancel(v8ValueArgs[0], "immediate");
    }

    @V8Function
    public V8Value clearInterval(V8Value... v8ValueArgs) throws JavetException {
        if (v8ValueArgs == null || v8ValueArgs.length != 1) {
            throw new IllegalArgumentException("clearInterval() takes 1 argument");
        }
        return cancel(v8ValueArgs[0], "interval");
    }

    @V8Function
    public V8Value clearTimeout(V8Value... v8ValueArgs) throws JavetException {
        if (v8ValueArgs == null || v8ValueArgs.length != 1) {
            throw new IllegalArgumentException("clearTimeout() takes 1 argument");
        }
        return cancel(v8ValueArgs[0], "timeout");
    }

    protected long extractAndValidateDelay(V8Value[] v8ValueArgs) {
        long delay = TimersConstants.DEFAULT_DELAY;
        if (v8ValueArgs.length > 1) {
            V8Value v8ValueDelay = v8ValueArgs[1];
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
        return JNModuleType.TIMERS;
    }

    @V8Function
    public V8Value setImmediate(V8Value... v8ValueArgs) throws JavetException {
        if (v8ValueArgs == null || v8ValueArgs.length == 0) {
            throw new IllegalArgumentException("setImmediate() takes a least 1 argument");
        }
        V8Value v8ValueCallback = v8ValueArgs[0];
        validateCallback(v8ValueCallback);
        TimersImmediate timersImmediate = new TimersImmediate(
                eventLoop, (V8ValueFunction) v8ValueCallback, extractArgs(v8ValueArgs, 1));
        putFunction(timersImmediate);
        timersImmediate.run();
        return timersImmediate.toV8Value();
    }

    @V8Function
    public V8Value setInterval(V8Value... v8ValueArgs) throws JavetException {
        if (v8ValueArgs == null || v8ValueArgs.length == 0) {
            throw new IllegalArgumentException("setInterval() takes a least 1 argument");
        }
        V8Value v8ValueCallback = v8ValueArgs[0];
        validateCallback(v8ValueCallback);
        long delay = extractAndValidateDelay(v8ValueArgs);
        TimersTimeout timersTimeout = new TimersTimeout(
                eventLoop, true, (V8ValueFunction) v8ValueCallback, delay, extractArgs(v8ValueArgs, 2));
        putFunction(timersTimeout);
        timersTimeout.run();
        return timersTimeout.toV8Value();
    }

    @V8Function
    public V8Value setTimeout(V8Value... v8ValueArgs) throws JavetException {
        if (v8ValueArgs == null || v8ValueArgs.length == 0) {
            throw new IllegalArgumentException("setTimeout() takes a least 1 argument");
        }
        V8Value v8ValueCallback = v8ValueArgs[0];
        validateCallback(v8ValueCallback);
        long delay = extractAndValidateDelay(v8ValueArgs);
        TimersTimeout timersTimeout = new TimersTimeout(
                eventLoop, false, (V8ValueFunction) v8ValueCallback, delay, extractArgs(v8ValueArgs, 2));
        putFunction(timersTimeout);
        timersTimeout.run();
        return timersTimeout.toV8Value();
    }

    protected void validateCallback(V8Value v8ValueCallback) {
        if (!(v8ValueCallback instanceof V8ValueFunction)) {
            throw new IllegalArgumentException("Argument [callback] must be a function");
        }
    }
}
