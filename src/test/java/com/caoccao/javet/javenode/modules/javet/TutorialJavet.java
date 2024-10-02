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

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;

public class TutorialJavet {
    public static void main(String[] args) throws JavetException, InterruptedException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime();
             JNEventLoop eventLoop = new JNEventLoop(v8Runtime)) {
            JavetProxyConverter javetProxyConverter = new JavetProxyConverter();
            javetProxyConverter.getConfig().
                    setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance());
            v8Runtime.setConverter(javetProxyConverter);
            eventLoop.loadStaticModules(JNModuleType.Javet);
            testPackage(v8Runtime);
            testStringBuilder(v8Runtime);
            testGC(v8Runtime);
        }
    }

    private static void testGC(V8Runtime v8Runtime) throws JavetException {
        v8Runtime.getLogger().info("=== javet.v8.gc() ===");
        v8Runtime.getExecutor("javet.v8.gc();").executeVoid();
    }

    private static void testPackage(V8Runtime v8Runtime) throws JavetException {
        v8Runtime.getLogger().info("=== javet.package ===");
        // Test java
        v8Runtime.getExecutor("let java = javet.package.java").executeVoid();
        v8Runtime.getLogger().logInfo(
                "java[''.valid''] = {0}",
                v8Runtime.getExecutor("java['.valid']").executeBoolean());
        v8Runtime.getLogger().logInfo(
                "java[''.name''] = {0}",
                v8Runtime.getExecutor("java['.name']").executeString());
        // Test java.util
        v8Runtime.getExecutor("let javaUtil = java.util").executeVoid();
        v8Runtime.getLogger().logInfo(
                "java.util[''.name''] = {0}",
                v8Runtime.getExecutor("javaUtil['.name']").executeString());
        v8Runtime.getLogger().logInfo(
                "java.util[''.sealed''] = {0}",
                v8Runtime.getExecutor("javaUtil['.sealed']").executeBoolean());
        v8Runtime.getLogger().logInfo(
                "java.util[''.valid''] = {0}",
                v8Runtime.getExecutor("javaUtil['.valid']").executeBoolean());
        // Test java.lang.Object
        v8Runtime.getLogger().logInfo(
                "java.lang.Object = {0}",
                ((Class<?>) v8Runtime.getExecutor("java.lang.Object").executeObject()).getName());
        // Clean up
        v8Runtime.getExecutor("java = undefined; javaUtil = undefined").executeVoid();
    }

    private static void testStringBuilder(V8Runtime v8Runtime) throws JavetException {
        v8Runtime.getLogger().info("=== java.lang.StringBuilder ===");
        v8Runtime.getLogger().info(
                v8Runtime.getExecutor("let sb = new javet.package.java.lang.StringBuilder();" +
                        "sb.append('a').append(1); sb.toString();").executeString());
        v8Runtime.getExecutor("sb = undefined;").executeVoid();
    }
}
