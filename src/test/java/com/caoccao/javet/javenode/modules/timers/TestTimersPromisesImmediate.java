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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTimersPromisesImmediate extends BaseTestTimersPromises {
    @Test
    public void testWithArgs() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("import { setImmediate } from 'timers/promises';\n" +
                "const a = [];\n" +
                "setImmediate(2).then(result => a.push(result));\n" +
                "a.push(1);\n" +
                "globalThis.a = a;").setModule(true).executeVoid();
        assertEquals("[1]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(1, eventLoop.getBlockingEventCount());
        eventLoop.await();
        assertEquals("[1,2]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(0, eventLoop.getBlockingEventCount());
    }

    @Test
    public void testWithoutArgs() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("import { setImmediate } from 'timers/promises';\n" +
                "const a = [];\n" +
                "a.push(1);\n" +
                "await setImmediate();\n" +
                "a.push(2)\n" +
                "globalThis.a = a;").setModule(true).executeVoid();
        assertTrue(v8Runtime.getExecutor("typeof a === 'undefined'").executeBoolean());
        assertEquals(1, eventLoop.getBlockingEventCount());
        eventLoop.await();
        assertEquals("[1,2]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(0, eventLoop.getBlockingEventCount());
    }
}
