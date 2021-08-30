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

package com.caoccao.javet.javenode.interfaces;

import com.caoccao.javet.exceptions.JavetError;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetClosable;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;

public interface IModuleReference extends IJavetClosable, Runnable {
    V8Runtime getV8Runtime();

    default V8Value toV8Value() throws JavetException {
        V8Runtime v8Runtime = getV8Runtime();
        if (JavetResourceUtils.isClosed(v8Runtime)) {
            throw new JavetException(JavetError.RuntimeAlreadyClosed);
        }
        V8ValueObject v8ValueObject = v8Runtime.createV8ValueObject();
        v8ValueObject.bind(this);
        return v8ValueObject;
    }
}
