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
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.interfaces.IJNFunction;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.utils.JavetResourceUtils;

import java.util.LinkedList;
import java.util.Objects;

public abstract class BaseJNModule implements IJNModule {
    protected volatile boolean closed;
    protected JNEventLoop eventLoop;
    protected LinkedList<IJNFunction> moduleReferences;

    public BaseJNModule(JNEventLoop eventLoop) {
        closed = false;
        this.eventLoop = Objects.requireNonNull(eventLoop);
        moduleReferences = new LinkedList<>();
    }

    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            while (!moduleReferences.isEmpty()) {
                IJNFunction moduleReference = moduleReferences.poll();
                if (moduleReference == null) {
                    break;
                }
                JavetResourceUtils.safeClose(moduleReference);
            }
            closed = true;
        }
    }

    @Override
    public JNEventLoop getEventLoop() {
        return eventLoop;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
