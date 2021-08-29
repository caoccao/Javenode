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

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.modules.BaseJavaV8Bridge;
import com.caoccao.javet.utils.V8ValueUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueInteger;
import com.caoccao.javet.values.reference.V8ValueFunction;

public class TimersTimeout extends BaseJavaV8Bridge {
    protected V8Value[] v8ValueArgs;
    protected V8ValueFunction v8ValueFunctionCallback;
    protected V8ValueInteger v8ValueIntegerDelay;

    public TimersTimeout(
            V8Runtime v8Runtime,
            V8ValueFunction v8ValueFunctionCallback,
            V8ValueInteger v8ValueIntegerDelay,
            V8Value... v8ValueArgs) throws JavetException {
        super(v8Runtime);
        this.v8ValueArgs = V8ValueUtils.toClone(v8ValueArgs);
        this.v8ValueFunctionCallback = v8ValueFunctionCallback.toClone();
        this.v8ValueIntegerDelay = v8ValueIntegerDelay.toClone();
    }
}
