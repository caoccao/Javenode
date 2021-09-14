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
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestDynamicTimersImmediate extends BaseTestDynamicTimers {
    @Test
    public void testWithArgs() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("import { setImmediate } from 'timers';\n" +
                "const a = [];\n" +
                "var t = setImmediate((b) => {\n" +
                "  a.push(b);\n" +
                "}, 2);\n" +
                "a.push(1);\n" +
                "globalThis.a = a;").setModule(true).executeVoid();
        assertEquals("[1]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(1, eventLoop.getBlockingEventCount());
        eventLoop.await();
        assertEquals("[1,2]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(0, eventLoop.getBlockingEventCount());
    }
}
