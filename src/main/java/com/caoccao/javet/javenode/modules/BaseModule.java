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

package com.caoccao.javet.javenode.modules;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.interfaces.IModuleBindable;
import com.caoccao.javet.javenode.interfaces.IModuleFunction;
import com.caoccao.javet.utils.JavetResourceUtils;

import java.util.LinkedList;

public abstract class BaseModule implements IModuleBindable {
    protected volatile boolean closed;
    protected LinkedList<IModuleFunction> moduleReferences;
    protected V8Runtime v8Runtime;

    public BaseModule(V8Runtime v8Runtime) {
        closed = false;
        moduleReferences = new LinkedList<>();
        this.v8Runtime = v8Runtime;
    }


    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            while (!moduleReferences.isEmpty()) {
                IModuleFunction moduleReference = moduleReferences.poll();
                if (moduleReference == null) {
                    break;
                }
                JavetResourceUtils.safeClose(moduleReference);
            }
            closed = true;
        }
    }

    public V8Runtime getV8Runtime() {
        return v8Runtime;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void setV8Runtime(V8Runtime v8Runtime) {
        this.v8Runtime = v8Runtime;
    }
}
