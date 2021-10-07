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

package com.caoccao.javet.javenode;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.enums.JNModuleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

public abstract class BaseJNTestSuite {
    protected JNEventLoop eventLoop;
    protected V8Runtime v8Runtime;

    @AfterEach
    public void afterEach() throws JavetException {
        eventLoop.close();
        v8Runtime.close();
    }

    @BeforeEach
    public void beforeEach() throws JavetException {
        v8Runtime = V8Host.getV8Instance().createV8Runtime();
        eventLoop = new JNEventLoop(v8Runtime);
        eventLoop.setAwaitTimeout(5).setAwaitTimeUnit(TimeUnit.SECONDS);
        eventLoop.loadStaticModules(JNModuleType.CONSOLE);
    }
}
