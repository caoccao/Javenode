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
import com.caoccao.javet.exceptions.JavetExecutionException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.values.reference.V8ValueArray;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestTimersTimeout {
    @Test
    public void testInvalidArgumentCount() throws JavetException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            try (TimersModule timersModule = new TimersModule(v8Runtime)) {
                timersModule.bind(v8Runtime.getGlobalObject());
                v8Runtime.getExecutor("setTimeout();").executeVoid();
                fail("Failed to throw exception");
            } catch (JavetExecutionException e) {
                assertEquals("Error: setTimeout() takes a least 1 argument", e.getMessage());
            }
        }
    }

    @Test
    public void testInvalidCallback() throws JavetException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            try (TimersModule timersModule = new TimersModule(v8Runtime)) {
                timersModule.bind(v8Runtime.getGlobalObject());
                v8Runtime.getExecutor("setTimeout(1);").executeVoid();
                fail("Failed to throw exception");
            } catch (JavetExecutionException e) {
                assertEquals("Error: Argument [callback] must be a function", e.getMessage());
            }
        }
    }

    @Test
    public void testInvalidDelay() throws JavetException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            try (TimersModule timersModule = new TimersModule(v8Runtime)) {
                timersModule.bind(v8Runtime.getGlobalObject());
                v8Runtime.getExecutor("setTimeout(() => {}, 'a');").executeVoid();
                fail("Failed to throw exception");
            } catch (JavetExecutionException e) {
                assertEquals("Error: Argument [delay] must be an integer", e.getMessage());
            }
        }
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            try (TimersModule timersModule = new TimersModule(v8Runtime)) {
                timersModule.bind(v8Runtime.getGlobalObject());
                v8Runtime.getExecutor("setTimeout(() => {}, -1);").executeVoid();
                fail("Failed to throw exception");
            } catch (JavetExecutionException e) {
                assertEquals("Error: Argument [delay] must be a positive integer", e.getMessage());
            }
        }
    }

    @Test
    public void testWithDelayAndArgs() throws JavetException, InterruptedException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            try (TimersModule timersModule = new TimersModule(v8Runtime)) {
                timersModule.bind(v8Runtime.getGlobalObject());
                v8Runtime.getExecutor("var a = [];" +
                        "setTimeout((b) => {\n" +
                        "  a.push(b);\n" +
                        "}, 20, 2);\n" +
                        "a.push(1);").executeVoid();
                boolean testPassed = false;
                for (int i = 0; i < 100; ++i) {
                    TimeUnit.MILLISECONDS.sleep(1);
                    try (V8ValueArray v8ValueArray = v8Runtime.getGlobalObject().get("a")) {
                        if (v8ValueArray.getLength() == 2) {
                            assertEquals("[1,2]", v8ValueArray.toJsonString());
                            assertTrue(i > 5);
                            testPassed = true;
                            break;
                        }
                    }
                }
                assertTrue(testPassed);
            } finally {
                v8Runtime.lowMemoryNotification();
            }
        }
    }

    @Test
    public void testWithDelayWithoutArgs() throws JavetException, InterruptedException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            try (TimersModule timersModule = new TimersModule(v8Runtime)) {
                timersModule.bind(v8Runtime.getGlobalObject());
                v8Runtime.getExecutor("var a = [];" +
                        "setTimeout(() => {\n" +
                        "  a.push(2);\n" +
                        "}, 20);\n" +
                        "a.push(1);").executeVoid();
                boolean testPassed = false;
                for (int i = 0; i < 100; ++i) {
                    TimeUnit.MILLISECONDS.sleep(1);
                    try (V8ValueArray v8ValueArray = v8Runtime.getGlobalObject().get("a")) {
                        if (v8ValueArray.getLength() == 2) {
                            assertEquals("[1,2]", v8ValueArray.toJsonString());
                            assertTrue(i > 5);
                            testPassed = true;
                            break;
                        }
                    }
                }
                assertTrue(testPassed);
            } finally {
                v8Runtime.lowMemoryNotification();
            }
        }
    }
}
