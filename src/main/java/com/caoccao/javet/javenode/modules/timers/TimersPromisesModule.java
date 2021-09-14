/*
 * Copyright (c) 2021. caoccao.com Sam Cao
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

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.modules.BaseJNModule;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValuePromise;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class TimersPromisesModule extends BaseJNModule {
    public static final long DEFAULT_DELAY = 1;
    public static final String NAME = "timers/promises";

    public TimersPromisesModule(JNEventLoop eventLoop) {
        super(eventLoop);
    }

    @Override
    public JNModuleType getType() {
        return JNModuleType.TIMERS_PROMISES;
    }

    @V8Function
    public V8ValuePromise setImmediate(V8Value... v8ValueArgs) throws JavetException {
        V8Value v8ValueArg = null;
        if (v8ValueArgs != null && v8ValueArgs.length > 0) {
            v8ValueArg = v8ValueArgs[0];
        }
        if (v8ValueArg == null) {
            v8ValueArg = eventLoop.getV8Runtime().createV8ValueNull();
        } else {
            v8ValueArg = v8ValueArg.toClone();
        }
        final V8Value v8ValueResult = v8ValueArg;
        final V8ValuePromise v8ValuePromiseResolver = eventLoop.getV8Runtime().createV8ValuePromise();
        Scheduler scheduler = Schedulers.from(eventLoop.getExecutorService());
        eventLoop.incrementBlockingEventCount();
        Observable.timer(DEFAULT_DELAY, TimeUnit.MILLISECONDS, scheduler)
                .subscribe(t -> {
                    try {
                        if (!isClosed()) {
                            v8ValuePromiseResolver.resolve(v8ValueResult);
                        }
                    } finally {
                        JavetResourceUtils.safeClose(v8ValueResult, v8ValuePromiseResolver);
                        eventLoop.decrementBlockingEventCount();
                    }
                });
        return v8ValuePromiseResolver.getPromise();
    }
}
