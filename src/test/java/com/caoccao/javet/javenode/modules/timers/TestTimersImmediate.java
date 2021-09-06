/*
 * Copyright (c) 2021-2021. caoccao.com Sam Cao
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
import com.caoccao.javet.exceptions.JavetExecutionException;
import com.caoccao.javet.javenode.BaseTestJavenodeSuite;
import com.caoccao.javet.javenode.enums.JNModuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestTimersImmediate extends BaseTestJavenodeSuite {
    @BeforeEach
    @Override
    public void beforeEach() throws JavetException {
        super.beforeEach();
        eventLoop.loadModule(JNModuleType.Timers);
    }

    @Test
    public void testInvalidArgumentCount() throws JavetException {
        try {
            v8Runtime.getExecutor("setImmediate();").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: setImmediate() takes a least 1 argument", e.getMessage());
        }
    }

    @Test
    public void testInvalidCallback() throws JavetException {
        try {

            v8Runtime.getExecutor("setImmediate(1);").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [callback] must be a function", e.getMessage());
        }
    }

    @Test
    public void testRef() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("const a = [];\n" +
                "var t = setImmediate(() => { a.push(true); });").executeVoid();
        assertEquals(1, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.hasRef());").executeVoid();
        eventLoop.await();
        v8Runtime.getExecutor("a.push(t.ref() == t);").executeVoid();
        assertEquals(0, eventLoop.getBlockingEventCount());
        // RxJava needs some time to process Disposable.
        TimeUnit.MILLISECONDS.sleep(10);
        v8Runtime.getExecutor("a.push(t.hasRef());").executeVoid();
        assertEquals(0, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.unref() == t);").executeVoid();
        assertEquals(0, eventLoop.getBlockingEventCount());
        String jsonString = v8Runtime.getExecutor("JSON.stringify(a);").executeString();
        assertEquals("[true,true,true,false,true]", jsonString);
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }

    @Test
    public void testWithArgs() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("const a = [];" +
                "var t = setImmediate((b) => {\n" +
                "  a.push(b);\n" +
                "}, 2);\n" +
                "a.push(1);").executeVoid();
        assertEquals("[1]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(1, eventLoop.getBlockingEventCount());
        eventLoop.await();
        assertEquals("[1,2]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(0, eventLoop.getBlockingEventCount());
        TimeUnit.MILLISECONDS.sleep(10);
        assertFalse(v8Runtime.getExecutor("t.hasRef()").executeBoolean());
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }
}
