/*
 * Copyright (c) 2021-2025. caoccao.com Sam Cao
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

package com.caoccao.javet.javenode.modules.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConsoleModule {
    @Test
    public void testGetTimeString() {
        assertEquals("1ms", ConsoleModule.getTimeString(1));
        assertEquals("123ms", ConsoleModule.getTimeString(123));
        assertEquals("12.345s", ConsoleModule.getTimeString(12345));
        assertEquals("2:03.456 (m:ss.mmm)", ConsoleModule.getTimeString(123456));
        assertEquals("20:34.567 (m:ss.mmm)", ConsoleModule.getTimeString(1234567));
        assertEquals("3:25:45.678 (h:mm:ss.mmm)", ConsoleModule.getTimeString(12345678));
    }
}
