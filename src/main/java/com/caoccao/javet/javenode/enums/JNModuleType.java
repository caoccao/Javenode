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

import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.console.ConsoleModule;
import com.caoccao.javet.javenode.modules.timers.TimersModule;
import com.caoccao.javet.javenode.modules.timers.TimersPromisesModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JNModuleType {
    public static final JNModuleType CONSOLE = new JNModuleType(ConsoleModule.NAME, ConsoleModule.class,
            "assert", "clear", "count", "countReset", "debug",
            "error", "info", "log", "time", "timeEnd",
            "timeLog", "trace", "warn");
    public static final JNModuleType TIMERS = new JNModuleType(TimersModule.NAME, TimersModule.class,
            "clearImmediate", "clearInterval", "clearTimeout",
            "setImmediate", "setInterval", "setTimeout");
    public static final JNModuleType TIMERS_PROMISES = new JNModuleType(
            TimersPromisesModule.NAME, TimersPromisesModule.class,
            "setImmediate", "setInterval", "setTimeout");
    private final List<String> identifiers;
    private final Class<? extends IJNModule> moduleClass;
    private final String name;

    public JNModuleType(String name, Class<? extends IJNModule> moduleClass, String... identifiers) {
        this.moduleClass = moduleClass;
        this.name = name;
        List<String> tempIdentifiers = new ArrayList<>();
        Collections.addAll(tempIdentifiers, identifiers);
        this.identifiers = Collections.unmodifiableList(tempIdentifiers);
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public Class<? extends IJNModule> getModuleClass() {
        return moduleClass;
    }

    public String getName() {
        return name;
    }
}
