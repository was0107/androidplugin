package com.b5m.plugin.activityloader;

import java.lang.reflect.Field;

/**
 * Created by boguang on 14-12-8.
 */
public class Smith<T> {

    private Object object;
    private String fieldName;
    private boolean inited;
    private Field field;

    public Smith(Object object, String filedName) {
        if (null == object) {
            throw new IllegalArgumentException("object can not be null");
        }
        this.object = object;
        this.fieldName = filedName;
    }

    private void prepare() {
        if (inited)
            return;
        inited = true;
        Class<?> c = this.object.getClass();
        while (null != c) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                field = f;
                return;
            } catch (Exception e) {

            } finally {
                c = c.getSuperclass();
            }
        }
    }


    public void set(T val) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
        prepare();
        if (null == object)
            throw new NoSuchFieldException();
        field.set(object, val);
    }

    public T get() throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {

        prepare();

        if (null == object) {
            throw new NoSuchFieldException();
        }

        try {

            T result = (T) field.get(object);
            return result;

        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("unable to cast object");
        }
    }
}
