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
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.values.reference.V8ValueArray;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTimersModule {
    @Test
    public void testTimeout() throws JavetException, InterruptedException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
            JavetStandardConsoleInterceptor javetStandardConsoleInterceptor =
                    new JavetStandardConsoleInterceptor(v8Runtime);
            javetStandardConsoleInterceptor.register(v8Runtime.getGlobalObject());
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
                javetStandardConsoleInterceptor.unregister(v8Runtime.getGlobalObject());
                v8Runtime.lowMemoryNotification();
            }
        }
    }
}
