package com.caoccao.javet.javenode.utils;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;

public final class V8ValueExUtils {
    private V8ValueExUtils() {
    }

    public static V8Value[] toClone(V8Value[] v8Values) throws JavetException {
        if (v8Values == null) {
            return null;
        }
        V8Value[] clonedV8Values = new V8Value[v8Values.length];
        for (int i = 0; i < v8Values.length; ++i) {
            clonedV8Values[i] = v8Values[i].toClone();
        }
        return clonedV8Values;
    }
}
