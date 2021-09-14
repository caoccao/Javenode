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
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;

public class TimersImmediate extends BaseTimersFunction {

    public static final long DEFAULT_DELAY = 1;

    public TimersImmediate(
            JNEventLoop eventLoop,
            V8ValueFunction v8ValueFunctionCallback,
            V8Value... v8ValueArgs) throws JavetException {
        super(eventLoop, false, v8ValueFunctionCallback, DEFAULT_DELAY, v8ValueArgs);
    }

    @V8Function
    @Override
    public boolean hasRef() {
        return super.hasRef();
    }

    @V8Function(thisObjectRequired = true)
    @Override
    public V8Value ref(V8Value thisObject) {
        return super.ref(thisObject);
    }

    @V8Function(thisObjectRequired = true)
    @Override
    public V8Value unref(V8Value thisObject) {
        return super.unref(thisObject);
    }
}
