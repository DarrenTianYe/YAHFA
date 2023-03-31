package lab.galaxy.yahfa.demoApp;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflect {
//    private static final String TAG = "Reflect";


public static int rmHideApi(){

    if (SDK_INT < Build.VERSION_CODES.P) {
        return 1;
    }
    try {
        Method forName = Class.class.getDeclaredMethod("forName", String.class);
        Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
        Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
        Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
        Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
        Object sVmRuntime = getRuntime.invoke(null);
        setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});

        return 2;
    } catch (Throwable e) {
        Log.e("[error]", "reflect bootstrap failed:", e);
    }
    return 0;
}


    public static Class<?> findClass(String clsName, Context context) {
        Class<?> cls = null;
        try {
            cls = Class.forName(clsName, true, context.getClassLoader());
        } catch (Throwable e) {
            LogUtils.error(e);
        }
        if (cls == null) {
            try {
                cls = Class.forName(clsName);
            } catch (Throwable e) {
                LogUtils.error(e);
            }
        }
        return cls;
    }

    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        return getDeclaredMethod(object.getClass(), methodName, parameterTypes);
    }
//
    public static Method getDeclaredMethod(Context context, String clsName, String methodName, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredMethod(findClass(clsName, context), methodName, parameterTypes);
    }

    public static Method getDeclaredMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        try {
            for (; cls != Object.class; cls = cls.getSuperclass()) {
                try {
                    Method m = cls.getDeclaredMethod(methodName, parameterTypes);
                    if (!m.isAccessible()) m.setAccessible(true);
                    return m;
                } catch (Throwable ignore) {
                }
            }
        } catch (Throwable e) {
           e.printStackTrace();
        }
        return null;
    }

    public static Field getDeclaredField(Object object, String fieldName) {
        return getDeclaredField(object.getClass(), fieldName);
    }
//
    public static Field getDeclaredField(Context context,String clsName, String fieldName) throws Throwable {
        return getDeclaredField(findClass(clsName, context), fieldName);
    }

    public static Field getDeclaredField(Class<?> cls, String fieldName) {
        for (; cls != Object.class; cls = cls.getSuperclass()) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                if (!f.isAccessible()) f.setAccessible(true);
                return f;
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    public static Object getStaticFieldValue(Context context, String clsName, String name) {
        try {
            return getStaticFieldValue(findClass(clsName, context), name);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getStaticFieldValue(Class<?> cls, String name) {
        return getFieldValue(cls, null, name);
    }

    public static Object getFieldValue(Object obj, String name) {
        if (null != obj) {
            return getFieldValue(obj.getClass(), obj, name);
        }
        return null;
    }

    public static Object getFieldValue(Class<?> cls, Object org, String name) {
        try {
            Field f = getDeclaredField(cls, name);
            if (null != f) {
                return f.get(org);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void setStaticFieldValue(String clsName, String name, Object val) {
//        try {
//            setStaticFieldValue(findClass(clsName), name, val);
//        } catch (Throwable e) {
//            //ZNativeCall.e(e);
//            e.printStackTrace();
//        }
//    }

    public static void setStaticFieldValue(Class<?> cls, String name, Object val) {
        try {
            setFieldValue(cls, name, null, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setFieldValue(Object obj, String name, Object val) {
        try {
            setFieldValue(obj.getClass(), name, obj, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void setFieldValue(Class<?> cls, String name, Object org, Object val) throws Throwable {
        Field f = getDeclaredField(cls, name);
        if (null != f) {
            f.set(org, val);
        }
    }

//    public static Constructor<?> getDeclaredConstructor(String clsName, Class<?>... parameterTypes) throws Throwable {
//        return getDeclaredConstructor(findClass(clsName), parameterTypes);
//    }

    public static Constructor<?> getDeclaredConstructor(Object obj, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredConstructor(obj.getClass(), parameterTypes);
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> cls, Class<?>... parameterTypes) throws Throwable {
        Constructor<?> constructor = cls.getDeclaredConstructor(parameterTypes);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor;
    }

}
