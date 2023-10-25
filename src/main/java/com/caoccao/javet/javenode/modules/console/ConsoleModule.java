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

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.javenode.modules.BaseJNModule;
import com.caoccao.javet.utils.V8ValueUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueBoolean;
import com.caoccao.javet.values.primitive.V8ValueString;
import com.caoccao.javet.values.reference.V8ValueObject;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsoleModule extends BaseJNModule {
    public static final String NAME = "console";
    protected static final String DEFAULT_LABEL = "default";
    protected static final String EMPTY = "";
    protected static final String ERROR_FORMAT_IS_NULL = "Format is null.";
    protected static final String ERROR_FORMAT_IS_UNDEFINED = "Format is undefined.";
    protected static final int MILLIS_PER_HOUR = 1000 * 60 * 60;
    protected static final int MILLIS_PER_MINUTE = 1000 * 60;
    protected static final int MILLIS_PER_SECOND = 1000;
    protected static final String UNDEFINED = "undefined";
    protected Map<String, AtomicInteger> countMap;
    protected Map<String, Long> timeMap;

    public ConsoleModule(JNEventLoop eventLoop) {
        super(eventLoop);
        countMap = Collections.synchronizedMap(new HashMap<>());
        timeMap = Collections.synchronizedMap(new HashMap<>());
    }

    protected static String getTimeString(long totalMillis) {
        if (totalMillis >= MILLIS_PER_HOUR) {
            final long hours = TimeUnit.MILLISECONDS.toHours(totalMillis);
            final long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) - TimeUnit.HOURS.toMinutes(hours);
            final long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) - TimeUnit.HOURS.toSeconds(hours)
                    - TimeUnit.MINUTES.toSeconds(minutes);
            final long millis = totalMillis - TimeUnit.HOURS.toMillis(hours)
                    - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);
            return String.format("%d:%02d:%02d.%03d (h:mm:ss.mmm)", hours, minutes, seconds, millis);
        } else if (totalMillis >= MILLIS_PER_MINUTE) {
            final long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
            final long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) - TimeUnit.MINUTES.toSeconds(minutes);
            final long millis = totalMillis - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);
            return String.format("%d:%02d.%03d (m:ss.mmm)", minutes, seconds, millis);
        } else if (totalMillis >= MILLIS_PER_SECOND) {
            final long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis);
            final long millis = (totalMillis - TimeUnit.SECONDS.toMillis(seconds));
            return String.format("%d.%ds", seconds, millis);
        } else {
            return String.format("%dms", totalMillis);
        }
    }

    @Override
    public void bind() throws JavetException {
        try (V8ValueObject v8ValueObject = getV8Runtime().createV8ValueObject()) {
            bind(v8ValueObject);
            getV8Runtime().getGlobalObject().set(NAME, v8ValueObject);
        }
    }

    @V8Function(name = "assert")
    public void consoleAssert(V8Value... v8Values) {
        final int length = v8Values.length;
        if (length == 0) {
            getLogger().error("Assertion failed");
        } else {
            V8Value v8Value = v8Values[0];
            if (v8Value instanceof V8ValueBoolean && !((V8ValueBoolean) v8Value).getValue()) {
                if (length == 1) {
                    getLogger().error("Assertion failed");
                } else {
                    getLogger().logError("Assertion failed: {0}",
                            V8ValueUtils.concat(" ", Arrays.copyOfRange(v8Values, 1, length)));
                }
            }
        }
    }

    @V8Function(name = "clear")
    public void consoleClear(V8Value... v8Values) {
        // It does nothing by default.
    }

    @V8Function(name = "debug")
    public void consoleDebug(V8Value... v8Values) {
        getLogger().debug(getMessage(v8Values));
    }

    @V8Function(name = "error")
    public void consoleError(V8Value... v8Values) {
        getLogger().error(getMessage(v8Values));
    }

    @V8Function(name = "info")
    public void consoleInfo(V8Value... v8Values) {
        getLogger().info(getMessage(v8Values));
    }

    @V8Function(name = "log")
    public void consoleLog(V8Value... v8Values) {
        getLogger().info(getMessage(v8Values));
    }

    @V8Function(name = "trace")
    public void consoleTrace(V8Value... v8Values) {
        getLogger().info(getMessage(v8Values));
    }

    @V8Function(name = "warn")
    public void consoleWarn(V8Value... v8Values) {
        getLogger().warn(getMessage(v8Values));
    }

    @V8Function
    public void count(V8Value... v8Values) {
        String label = getLabel(v8Values);
        AtomicInteger counter = countMap.get(label);
        if (counter == null) {
            counter = new AtomicInteger(0);
            countMap.put(label, counter);
        }
        getLogger().logInfo("{0}: {1}", label, Integer.toString(counter.incrementAndGet()));
    }

    @V8Function
    public void countReset(V8Value... v8Values) {
        String label = getLabel(v8Values);
        AtomicInteger counter = countMap.remove(label);
        if (counter == null) {
            throw new IllegalArgumentException(
                    String.format("Warning: Count for ''%s'' does not exist", label));
        }
    }

    protected String getLabel(V8Value... v8Values) {
        if (v8Values.length > 0) {
            V8Value v8Value = v8Values[0];
            if (v8Value instanceof V8ValueString) {
                String label = ((V8ValueString) v8Value).getValue();
                if (!label.isEmpty()) {
                    return label;
                }
            }
        }
        return DEFAULT_LABEL;
    }

    protected String getMessage(V8Value... v8Values) {
        final int length = v8Values.length;
        if (length == 0) {
            return EMPTY;
        } else if (length == 1) {
            final V8Value v8Value = v8Values[0];
            if (v8Value == null) {
                return UNDEFINED;
            } else {
                return v8Value.toString();
            }
        } else {
            try {
                final V8Value v8Value = v8Values[0];
                if (v8Value == null) {
                    return ERROR_FORMAT_IS_UNDEFINED;
                } else {
                    String format = v8Value.toString();
                    if (format == null || format.length() == 0) {
                        return ERROR_FORMAT_IS_NULL;
                    } else {
                        List<Object> objectArgs = new ArrayList<>();
                        for (int i = 1; i < length; ++i) {
                            objectArgs.add(getV8Runtime().toObject(v8Values[i]));
                        }
                        return String.format(format, objectArgs.toArray());
                    }
                }
            } catch (Throwable t) {
                return t.getMessage();
            }
        }
    }

    @Override
    public JNModuleType getType() {
        return JNModuleType.CONSOLE;
    }

    @V8Function
    public void time(V8Value... v8Values) {
        String label = getLabel(v8Values);
        if (timeMap.containsKey(label)) {
            throw new IllegalArgumentException(
                    String.format("Warning: Label ''%s'' already exists for console.time()", label));
        }
        timeMap.put(label, System.currentTimeMillis());
    }

    @V8Function
    public void timeEnd(V8Value... v8Values) {
        String label = getLabel(v8Values);
        Long startTimeMillis = timeMap.remove(label);
        if (startTimeMillis == null) {
            throw new IllegalArgumentException(
                    String.format("Warning: No such label ''%s'' for console.timeEnd()", label));
        }
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        getLogger().logInfo("{0}: {1}", label, getTimeString(elapsedMillis));
    }

    @V8Function
    public void timeLog(V8Value... v8Values) {
        String label = getLabel(v8Values);
        Long startTimeMillis = timeMap.get(label);
        if (startTimeMillis == null) {
            throw new IllegalArgumentException(
                    String.format("Warning: No such label ''%s'' for console.timeLog()", label));
        }
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        final int length = v8Values.length;
        if (length <= 1) {
            getLogger().logInfo("{0}: {1}", label, getTimeString(elapsedMillis));
        } else {
            getLogger().logInfo("{0}: {1} {2}",
                    label, getTimeString(elapsedMillis),
                    V8ValueUtils.concat(" ", Arrays.copyOfRange(v8Values, 1, length)));
        }
    }

    @Override
    public void unbind() throws JavetException {
        getV8Runtime().getGlobalObject().delete(NAME);
    }
}
