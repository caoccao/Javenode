/*
 * Copyright (c) 2021-2023. caoccao.com Sam Cao
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
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;

public class TimersTimeout extends BaseTimersFunction implements IJavetDirectCallable {

    public TimersTimeout(
            IJNModule parentModule,
            boolean recurrent,
            V8ValueFunction v8ValueFunctionCallback,
            long delay,
            V8Value... v8ValueArgs) throws JavetException {
        super(parentModule, recurrent, v8ValueFunctionCallback, delay, v8ValueArgs);
    }

    @Override
    public JavetCallbackContext[] getCallbackContexts() {
        if (javetCallbackContexts == null) {
            javetCallbackContexts = new JavetCallbackContext[]{
                    new JavetCallbackContext(
                            "hasRef",
                            this, JavetCallbackType.DirectCallNoThisAndResult,
                            (IJavetDirectCallable.NoThisAndResult<Exception>) this::hasRef),
                    new JavetCallbackContext(
                            "toPrimitive",
                            V8ValueSymbolType.BuiltIn,
                            this, JavetCallbackType.DirectCallGetterAndNoThis,
                            (IJavetDirectCallable.GetterAndNoThis<Exception>) this::getReferenceIdFunction),
                    new JavetCallbackContext(
                            "ref",
                            this, JavetCallbackType.DirectCallThisAndResult,
                            (IJavetDirectCallable.ThisAndResult<Exception>) this::ref),
                    new JavetCallbackContext(
                            "unref",
                            this, JavetCallbackType.DirectCallThisAndResult,
                            (IJavetDirectCallable.ThisAndResult<Exception>) this::unref),
                    new JavetCallbackContext(
                            "refresh",
                            this, JavetCallbackType.DirectCallThisAndResult,
                            (IJavetDirectCallable.ThisAndResult<Exception>) this::refresh),
            };
        }
        return javetCallbackContexts;
    }

    public V8ValueFunction getReferenceIdFunction(V8Value... v8Values) throws JavetException {
        StringBuilder stringBuilder = new StringBuilder("() => ").append(getReferenceId());
        return getEventLoop().getV8Runtime().createV8ValueFunction(stringBuilder.toString());
    }

    public V8Value refresh(V8Value thisObject, V8Value... v8Values) {
        if (hasRef()) {
            cancel();
            run();
        }
        return thisObject;
    }
}
