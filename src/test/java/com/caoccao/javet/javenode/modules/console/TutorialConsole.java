/*
 * Copyright (c) 2021-2023. caoccao.com Sam Cao
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

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;

import java.util.concurrent.TimeUnit;

public class TutorialConsole {
    public static void main(String[] args) throws JavetException, InterruptedException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime();
             JNEventLoop eventLoop = new JNEventLoop(v8Runtime)) {
            eventLoop.loadStaticModules(JNModuleType.CONSOLE);
            testLogging(v8Runtime);
            testTime(v8Runtime, eventLoop);
            testCount(v8Runtime, eventLoop);
            testAssert(v8Runtime, eventLoop);
        }
    }

    private static void testAssert(V8Runtime v8Runtime, JNEventLoop eventLoop) throws JavetException {
        v8Runtime.getLogger().info("=== assert ===");
        v8Runtime.getExecutor("console.assert();").executeVoid();
        v8Runtime.getExecutor("console.assert(true, 'nothing');").executeVoid();
        v8Runtime.getExecutor("console.assert('nothing');").executeVoid();
        v8Runtime.getExecutor("console.assert(123, 234);").executeVoid();
        v8Runtime.getExecutor("console.assert(false);").executeVoid();
        v8Runtime.getExecutor("console.assert(false, 'abc', 123, true, false, 234.567);").executeVoid();
    }

    private static void testCount(V8Runtime v8Runtime, JNEventLoop eventLoop) throws JavetException {
        v8Runtime.getLogger().info("=== count ===");
        v8Runtime.getExecutor("console.count();").executeVoid();
        v8Runtime.getExecutor("console.count();").executeVoid();
        v8Runtime.getExecutor("console.count('abc');").executeVoid();
        v8Runtime.getExecutor("console.count('abc');").executeVoid();
        v8Runtime.getExecutor("console.countReset();").executeVoid();
        v8Runtime.getExecutor("console.countReset('abc');").executeVoid();
        try {
            v8Runtime.getExecutor("console.countReset();").executeVoid();
        } catch (JavetException e) {
            eventLoop.getLogger().logError(e.getMessage());
        }
        try {
            v8Runtime.getExecutor("console.countReset('abc');").executeVoid();
        } catch (JavetException e) {
            eventLoop.getLogger().logError(e.getMessage());
        }
    }

    private static void testLogging(V8Runtime v8Runtime) throws JavetException {
        v8Runtime.getLogger().info("=== console ===");
        v8Runtime.getExecutor("console.clear();").executeVoid();
        v8Runtime.getExecutor("console.debug('debug()');").executeVoid();
        v8Runtime.getExecutor("console.error('error()');").executeVoid();
        v8Runtime.getExecutor("console.info('info()');").executeVoid();
        v8Runtime.getExecutor("console.log('log()');").executeVoid();
        v8Runtime.getExecutor("console.trace('trace()');").executeVoid();
        v8Runtime.getExecutor("console.warn('warn()');").executeVoid();
    }

    private static void testTime(V8Runtime v8Runtime, JNEventLoop eventLoop) throws JavetException, InterruptedException {
        v8Runtime.getLogger().info("=== time ===");
        v8Runtime.getExecutor("console.time();").executeVoid();
        v8Runtime.getExecutor("console.time('abc');").executeVoid();
        try {
            v8Runtime.getExecutor("console.time('abc');").executeVoid();
        } catch (JavetException e) {
            eventLoop.getLogger().logError(e.getMessage());
        }
        v8Runtime.getExecutor("console.time('123');").executeVoid();
        v8Runtime.getExecutor("console.timeLog();").executeVoid();
        v8Runtime.getExecutor("console.timeLog('abc', 123, 'abc');").executeVoid();
        v8Runtime.getExecutor("console.timeLog('abc', true, false);").executeVoid();
        TimeUnit.MILLISECONDS.sleep(10);
        v8Runtime.getExecutor("console.timeEnd();").executeVoid();
        TimeUnit.MILLISECONDS.sleep(10);
        v8Runtime.getExecutor("console.timeEnd('abc');").executeVoid();
        TimeUnit.MILLISECONDS.sleep(10);
        v8Runtime.getExecutor("console.timeEnd('123');").executeVoid();
        try {
            v8Runtime.getExecutor("console.timeEnd('123');").executeVoid();
        } catch (JavetException e) {
            eventLoop.getLogger().logError(e.getMessage());
        }
    }
}
