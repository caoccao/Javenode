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

package com.caoccao.javet.javenode;

import com.caoccao.javet.javenode.modules.console.TutorialConsole;
import com.caoccao.javet.javenode.modules.timers.TutorialTimersPromisesTimeout;
import com.caoccao.javet.javenode.modules.timers.TutorialTimersTimeout;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class TestTutorial {
    protected void testMain(Class<?> tutorialClass, String[] args) throws NoSuchMethodException {
        assertNotNull(tutorialClass, "Tutorial class is not found.");
        Method methodMain = tutorialClass.getMethod("main", String[].class);
        assertNotNull(methodMain, "main() is not found.");
        assertTrue(Modifier.isStatic(methodMain.getModifiers()), "main() is not static.");
        try {
            methodMain.invoke(null, (Object) args);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            fail(t.getMessage());
        }
    }

    @Test
    public void testModules() throws NoSuchMethodException {
        testMain(TutorialConsole.class, new String[0]);
        testMain(TutorialTimersPromisesTimeout.class, new String[0]);
        testMain(TutorialTimersTimeout.class, new String[0]);
    }
}
