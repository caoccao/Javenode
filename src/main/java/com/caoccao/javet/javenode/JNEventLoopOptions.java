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

package com.caoccao.javet.javenode;

import io.vertx.core.VertxOptions;

import java.util.Objects;

public final class JNEventLoopOptions {
    public static final int DEFAULT_THREAD_POOL_SIZE = 4;
    private boolean gcBeforeClosing;
    private VertxOptions vertxOptions;

    public JNEventLoopOptions() {
        this(new VertxOptions().setWorkerPoolSize(DEFAULT_THREAD_POOL_SIZE));
    }

    public JNEventLoopOptions(VertxOptions vertxOptions) {
        this.gcBeforeClosing = true;
        this.vertxOptions = Objects.requireNonNull(vertxOptions);
    }

    public VertxOptions getVertxOptions() {
        return vertxOptions;
    }

    public boolean isGcBeforeClosing() {
        return gcBeforeClosing;
    }

    public JNEventLoopOptions setGcBeforeClosing(boolean gcBeforeClosing) {
        this.gcBeforeClosing = gcBeforeClosing;
        return this;
    }
}
