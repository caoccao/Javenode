/*
 * Copyright (c) 2024. caoccao.com Sam Cao
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
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.modules.BaseJNModule;
import com.caoccao.javet.values.reference.V8ValueObject;

public class JavetModule extends BaseJNModule {
    public static final String NAME = "javet";

    protected JavetJVMInterceptor interceptor;

    public JavetModule(JNEventLoop eventLoop) {
        super(eventLoop);
        interceptor = new JavetJVMInterceptor(getV8Runtime());
    }

    @Override
    public void bind(V8ValueObject v8ValueObject) throws JavetException {
        interceptor.register(v8ValueObject);
    }

    @Override
    public JNModuleType getType() {
        return JNModuleType.Javet;
    }

    @Override
    public void unbind(V8ValueObject v8ValueObject) throws JavetException {
        interceptor.unregister(v8ValueObject);
    }
}
