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
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.values.V8Value;

public class TimersPromisesTimeout extends BaseTimersPromisesFunction {
    public TimersPromisesTimeout(
            IJNModule parentModule,
            boolean recurrent,
            long delay,
            V8Value v8Value,
            boolean resolve) throws JavetException {
        super(parentModule, recurrent, delay, v8Value, resolve);
    }
}
