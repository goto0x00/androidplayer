
package dai.android.virtual.utility;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.UiThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class Reflect {

    public static Object sActivityThread;
    public static Object sLoadedApk;
    public static Instrumentation sInstrumentation;

    public static Object getField(Class clazz, Object target, String name) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    public static Object getFieldNoException(Class clazz, Object target, String name) {
        try {
            return Reflect.getField(clazz, target, name);
        } catch (Exception e) {
            //ignored.
        }

        return null;
    }

    public static void setField(Class clazz,
                                Object target,
                                String name,
                                Object value) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    public static void setFieldNoException(Class clazz,
                                           Object target,
                                           String name,
                                           Object value) {
        try {
            Reflect.setField(clazz, target, name, value);
        } catch (Exception e) {
            //ignored.
        }
    }

    @SuppressWarnings("unchecked")
    public static Object invoke(Class clazz, Object target, String name, Object... args)
            throws Exception {
        Class[] parameterTypes = null;
        if (args != null) {
            parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
        }

        Method method = clazz.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    @SuppressWarnings("unchecked")
    public static Object invoke(Class clazz,
                                Object target,
                                String name,
                                Class[] parameterTypes,
                                Object... args)
            throws Exception {
        Method method = clazz.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    @SuppressWarnings("unchecked")
    public static Object invokeNoException(Class clazz,
                                           Object target,
                                           String name,
                                           Class[] parameterTypes,
                                           Object... args) {
        try {
            return invoke(clazz, target, name, parameterTypes, args);
        } catch (Exception e) {
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object invokeConstructor(Class clazz,
                                           Class[] parameterTypes,
                                           Object... args) throws Exception {
        Constructor constructor = clazz.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    @UiThread
    public static Object getActivityThread(Context base) {
        if (sActivityThread == null) {
            try {
                Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
                Object activityThread = null;
                try {
                    activityThread = Reflect.getField(
                            activityThreadClazz,
                            null,
                            "sCurrentActivityThread");
                } catch (Exception e) {
                    // ignored
                }

                if (activityThread == null) {
                    activityThread = ((ThreadLocal<?>) Reflect.getField(
                            activityThreadClazz,
                            null,
                            "sThreadLocal")).get();
                }
                sActivityThread = activityThread;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sActivityThread;
    }

    public static Instrumentation getInstrumentation(Context base) {
        if (getActivityThread(base) != null) {
            try {
                sInstrumentation = (Instrumentation) Reflect.invoke(
                        sActivityThread.getClass(),
                        sActivityThread,
                        "getInstrumentation");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sInstrumentation;
    }

    /**
     * reflect the android.app.Instrumentation
     *
     * @param activityThread
     * @param instrumentation
     */
    public static void setInstrumentation(Object activityThread, Instrumentation instrumentation) {
        try {
            Reflect.setField(activityThread.getClass(),
                    activityThread,
                    "mInstrumentation",
                    instrumentation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getPackageInfo(Context base) {
        if (sLoadedApk == null) {
            try {
                sLoadedApk = Reflect.getField(base.getClass(), base, "mPackageInfo");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sLoadedApk;
    }

    public static void setHandlerCallback(Context base, Handler.Callback callback) {
        try {
            Object activityThread = getActivityThread(base);
            Handler mainHandler = (Handler) Reflect.invoke(
                    activityThread.getClass(),
                    activityThread,
                    "getHandler",
                    (Object[]) null);
            Reflect.setField(Handler.class, mainHandler, "mCallback", callback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
