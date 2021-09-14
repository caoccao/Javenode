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

package com.caoccao.javet.javenode.enums;

import com.caoccao.javet.javenode.interfaces.IJNModule;
import com.caoccao.javet.javenode.modules.timers.TimersModule;
import com.caoccao.javet.javenode.modules.timers.TimersPromisesModule;

public final class JNModuleType {
    public static final JNModuleType TIMERS = new JNModuleType(TimersModule.NAME, TimersModule.class);
    public static final JNModuleType TIMERS_PROMISES = new JNModuleType(TimersPromisesModule.NAME, TimersPromisesModule.class);

    private Class<? extends IJNModule> moduleClass;
    private String name;

    public JNModuleType(String name, Class<? extends IJNModule> moduleClass) {
        this.moduleClass = moduleClass;
        this.name = name;
    }

    public Class<? extends IJNModule> getModuleClass() {
        return moduleClass;
    }

    public String getName() {
        return name;
    }
}
