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

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetClosable;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.util.Objects;

public interface IJNModule extends IJavetClosable {
    default void bind(V8ValueObject v8ValueObject) throws JavetException {
        Objects.requireNonNull(v8ValueObject).bind(this);
    }

    JNEventLoop getEventLoop();

    default void unbind(V8ValueObject v8ValueObject) throws JavetException {
        // TODO
//        Objects.requireNonNull(v8ValueObject).unbind(this);
    }
}