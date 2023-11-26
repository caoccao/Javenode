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

package com.caoccao.javet.javenode.enums;

import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.console.ConsoleModule;
import com.caoccao.javet.javenode.modules.timers.TimersModule;
import com.caoccao.javet.javenode.modules.timers.TimersPromisesModule;

import java.util.function.Function;

public final class JNModuleType {
    public static final JNModuleType Console = new JNModuleType(ConsoleModule.NAME, ConsoleModule::new);
    public static final JNModuleType Timers = new JNModuleType(TimersModule.NAME, TimersModule::new);
    public static final JNModuleType TimersPromises = new JNModuleType(TimersPromisesModule.NAME, TimersPromisesModule::new);

    private final Function<JNEventLoop, IJNModule> moduleConstructor;
    private final String name;

    public JNModuleType(String name, Function<JNEventLoop, IJNModule> moduleConstructor) {
        this.moduleConstructor = moduleConstructor;
        this.name = name;
    }

    public Function<JNEventLoop, IJNModule> getModuleConstructor() {
        return moduleConstructor;
    }

    public String getName() {
        return name;
    }
}
