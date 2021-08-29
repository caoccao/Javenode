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
import com.caoccao.javet.javenode.modules.BaseJavaV8Bridge;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueInteger;
import com.caoccao.javet.values.reference.V8ValueFunction;

public class TimersModule extends BaseJavaV8Bridge {
    public TimersModule(V8Runtime v8Runtime) {
        super(v8Runtime);
    }

    @V8Function
    public V8Value setTimeout(
            V8Value v8ValueCallback,
            V8Value v8ValueDelay,
            V8Value... v8ValueArgs) throws JavetException {
        if (!(v8ValueCallback instanceof V8ValueFunction)) {
            throw new IllegalArgumentException("[callback] must be a function");
        }
        if (!(v8ValueDelay instanceof V8ValueInteger)) {
            throw new IllegalArgumentException("[delay] must be an integer");
        }
        TimersTimeout timersTimeout = new TimersTimeout(
                v8Runtime,
                (V8ValueFunction) v8ValueCallback,
                (V8ValueInteger) v8ValueDelay,
                v8ValueArgs);
        return timersTimeout.toV8Value();
    }
}
