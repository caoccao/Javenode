/*
 * Copyright (c) 2021-2024. caoccao.com Sam Cao
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

package com.caoccao.javet.javenode.modules.javet;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.javenode.BaseJNTestSuite;
import com.caoccao.javet.javenode.enums.JNModuleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavetModule extends BaseJNTestSuite {
    @Override
    public void beforeEach() throws JavetException {
        super.beforeEach();
        v8Runtime.setConverter(new JavetProxyConverter());
        eventLoop.loadStaticModules(JNModuleType.Javet);
    }

    @Test
    public void testPackage() throws JavetException {
        // Test java
        v8Runtime.getExecutor("let java = javet.package.java").executeVoid();
        assertFalse(v8Runtime.getExecutor("java['.valid']").executeBoolean());
        assertEquals("java", v8Runtime.getExecutor("java['.name']").executeString());
        // Test java.util
        v8Runtime.getExecutor("let javaUtil = java.util").executeVoid();
        assertTrue(v8Runtime.getExecutor("javaUtil['.valid']").executeBoolean());
        assertFalse(v8Runtime.getExecutor("javaUtil['.sealed']").executeBoolean());
        assertEquals("java.util", v8Runtime.getExecutor("javaUtil['.name']").executeString());
        // Test java.lang.Object
        assertEquals(Object.class, v8Runtime.getExecutor("java.lang.Object").executeObject());
        // Test getPackages
        assertEquals(
                "java.lang.annotation,java.lang.invoke,java.lang.management,java.lang.ref,java.lang.reflect," +
                        "java.nio.channels,java.nio.channels.spi,java.nio.charset,java.nio.charset.spi,java.nio.file," +
                        "java.nio.file.attribute,java.nio.file.spi,java.security.cert,java.text.spi,java.time.chrono," +
                        "java.time.format,java.time.temporal,java.util.concurrent,java.util.concurrent.atomic," +
                        "java.util.concurrent.locks,java.util.function,java.util.jar,java.util.logging," +
                        "java.util.regex,java.util.spi,java.util.stream,java.util.zip",
                v8Runtime.getExecutor("java['.getPackages']().map(p => p['.name']).sort().join(',')").executeString()
        );
        // Clean up
        v8Runtime.getExecutor("java = undefined; javaUtil = undefined;").executeVoid();
    }

    @Test
    public void testV8GC() throws JavetException {
        int initialCallbackContextCount = v8Runtime.getCallbackContextCount();
        v8Runtime.getGlobalObject().set("test", String.class);
        assertEquals(initialCallbackContextCount + 5, v8Runtime.getCallbackContextCount());
        v8Runtime.getGlobalObject().delete("test");
        assertEquals(initialCallbackContextCount + 5, v8Runtime.getCallbackContextCount());
        v8Runtime.getExecutor("javet.v8.gc()").executeVoid();
        assertEquals(initialCallbackContextCount + 5, v8Runtime.getCallbackContextCount());
        v8Runtime.lowMemoryNotification();
        assertEquals(initialCallbackContextCount, v8Runtime.getCallbackContextCount());
    }
}
