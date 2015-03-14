package com.sandbox.runtime.js.converters;

import com.sandbox.runtime.js.models.JSError;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.arrays.ArrayData;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by nickhoughton on 8/08/2014.
 * Terrible hacks live here, need to fix. TODO
 */
public class NashornConverter {

    static NashornConverter converter = null;
    static Class nativeArrayClass;
    static Constructor nativeArrayConstructor;
    static Class scriptEngineClass;
    static Method getGlobalMethod;

    public static NashornConverter instance() throws Exception {
        if(converter == null) {
            converter = new NashornConverter();
            nativeArrayClass = Class.forName("jdk.nashorn.internal.objects.NativeArray");
            nativeArrayConstructor = nativeArrayClass.getDeclaredConstructor(ArrayData.class, Global.class);
            nativeArrayConstructor.setAccessible(true);

            scriptEngineClass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngine");
            getGlobalMethod = scriptEngineClass.getDeclaredMethod("getNashornGlobalFrom", ScriptContext.class);
            getGlobalMethod.setAccessible(true);

        }

        return converter;
    }

    public Object convert(ScriptEngine engine, Object object) throws Exception{
        Global global = (Global) getGlobalMethod.invoke(engine, engine.getContext());
        Object result = convert(global, object);
        return result;

    }

    private Object convert(Global global, Object object) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        //if its an array, object or primitive go thru and fix the values
        if(object != null && (object instanceof List || object.getClass().isArray() )) {
            List listObject = null;
            //if an array but not a list (String[] etc), convert to list to use same logic
            if(object instanceof List){
                listObject = (List) object;
            }else{
                listObject = getListFromPrimitive(object);
            }

            for (int listIndex = 0; listIndex < listObject.size(); listIndex++) {
                listObject.set(listIndex, convert(global, listObject.get(listIndex)));

            }

            ArrayData arrayData = ArrayData.allocate(listObject.toArray());
            return nativeArrayConstructor.newInstance(arrayData, global);

        }else if(object instanceof Map){
            ScriptObject nativeObject = global.newObject();

            Map mapObject = (Map)object;
            for (Object hashKey : mapObject.keySet()){
                nativeObject.set(hashKey, convert(global, mapObject.get(hashKey)), false);
            }

            return nativeObject;

        }else if(object == null || object.getClass().isPrimitive() || object instanceof String || object instanceof Number || object instanceof Boolean || object instanceof Byte || object instanceof Character  || object instanceof JSError || object.getClass().getCanonicalName().startsWith("jdk.")){
            return object;
        }else{
            throw new RuntimeException("Unsupported object! - " + object.getClass());
        }

    }

    private static List getListFromPrimitive(Object primitiveArray){
        if(primitiveArray instanceof boolean[]) return Arrays.asList((boolean[])primitiveArray);
        if(primitiveArray instanceof byte[]) return Arrays.asList((byte[])primitiveArray);
        if(primitiveArray instanceof short[]) return Arrays.asList((short[])primitiveArray);
        if(primitiveArray instanceof char[]) return Arrays.asList((char[])primitiveArray);
        if(primitiveArray instanceof int[]) return Arrays.asList((int[])primitiveArray);
        if(primitiveArray instanceof long[]) return Arrays.asList((long[])primitiveArray);
        if(primitiveArray instanceof float[]) return Arrays.asList((float[])primitiveArray);
        if(primitiveArray instanceof double[]) return Arrays.asList((double[])primitiveArray);
        if(primitiveArray instanceof Object[]) return Arrays.asList((Object[])primitiveArray);

        throw new RuntimeException("Couldn't convert primitive array to list =(");
    }
}
