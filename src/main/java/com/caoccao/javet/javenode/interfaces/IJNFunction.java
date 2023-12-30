/*
 * Copyright (c) 2021-2024. caoccao.com Sam Cao
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

package com.caoccao.javet.javenode.interfaces;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetClosable;
import com.caoccao.javet.interfaces.IJavetLogger;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.values.V8Value;

public interface IJNFunction extends IJavetClosable, Runnable {
    default JNEventLoop getEventLoop() {
        return getParentModule().getEventLoop();
    }

    default IJavetLogger getLogger() {
        return getEventLoop().getLogger();
    }

    IJNModule getParentModule();

    int getReferenceId();

    default V8Runtime getV8Runtime() {
        return getEventLoop().getV8Runtime();
    }

    V8Value toV8Value() throws JavetException;
}
