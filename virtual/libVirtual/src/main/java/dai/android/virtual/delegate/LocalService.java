
package dai.android.virtual.delegate;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

import dai.android.virtual.PluginManager;
import dai.android.virtual.internal.LoadedPlugin;
import dai.android.virtual.utility.Plugin;
import dai.android.virtual.utility.Reflect;


public class LocalService extends Service {
    private static final String TAG = LocalService.class.getSimpleName();

    /**
     * The target service, usually it's a plugin service intent
     */
    public static final String EXTRA_TARGET = "target";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_PLUGIN_LOCATION = "plugin_location";

    public static final int EXTRA_COMMAND_START_SERVICE = 1;
    public static final int EXTRA_COMMAND_STOP_SERVICE = 2;
    public static final int EXTRA_COMMAND_BIND_SERVICE = 3;
    public static final int EXTRA_COMMAND_UNBIND_SERVICE = 4;


    private PluginManager mPluginManager;

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPluginManager = PluginManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent || !intent.hasExtra(EXTRA_TARGET) || !intent.hasExtra(EXTRA_COMMAND)) {
            return START_STICKY;
        }

        Intent target = intent.getParcelableExtra(EXTRA_TARGET);
        int command = intent.getIntExtra(EXTRA_COMMAND, 0);
        if (null == target || command <= 0) {
            return START_STICKY;
        }

        ComponentName component = target.getComponent();
        LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
        // ClassNotFoundException when unmarshalling in Android 5.1
        target.setExtrasClassLoader(plugin.getClassLoader());
        switch (command) {
            case EXTRA_COMMAND_START_SERVICE: {
                ActivityThread mainThread = (ActivityThread) Reflect.getActivityThread(getBaseContext());
                IApplicationThread appThread = mainThread.getApplicationThread();
                Service service;

                if (this.mPluginManager.getComponentsHandler().isServiceAvailable(component)) {
                    service = this.mPluginManager.getComponentsHandler().getService(component);
                } else {
                    try {
                        service = (Service) plugin.getClassLoader()
                                .loadClass(component.getClassName())
                                .newInstance();

                        Application app = plugin.getApplication();
                        IBinder token = appThread.asBinder();
                        Method attach = service.getClass().getMethod(
                                "attach",
                                Context.class,
                                ActivityThread.class,
                                String.class,
                                IBinder.class,
                                Application.class,
                                Object.class
                        );
                        IActivityManager am = mPluginManager.getActivityManager();

                        attach.invoke(
                                service,
                                plugin.getPluginContext(),
                                mainThread,
                                component.getClassName(),
                                token,
                                app,
                                am
                        );
                        service.onCreate();
                        this.mPluginManager.getComponentsHandler().rememberService(component, service);
                    } catch (Throwable t) {
                        return START_STICKY;
                    }
                }

                service.onStartCommand(
                        target,
                        0,
                        this.mPluginManager
                                .getComponentsHandler()
                                .getServiceCounter(service)
                                .getAndIncrement());
                break;
            }
            case EXTRA_COMMAND_BIND_SERVICE: {
                ActivityThread mainThread = (ActivityThread) Reflect.getActivityThread(getBaseContext());
                IApplicationThread appThread = mainThread.getApplicationThread();
                Service service = null;

                if (this.mPluginManager.getComponentsHandler().isServiceAvailable(component)) {
                    service = this.mPluginManager.getComponentsHandler().getService(component);
                } else {
                    try {
                        service = (Service) plugin.getClassLoader()
                                .loadClass(component.getClassName())
                                .newInstance();

                        Application app = plugin.getApplication();
                        IBinder token = appThread.asBinder();
                        Method attach = service.getClass().getMethod(
                                "attach",
                                Context.class,
                                ActivityThread.class,
                                String.class,
                                IBinder.class,
                                Application.class,
                                Object.class
                        );
                        IActivityManager am = mPluginManager.getActivityManager();

                        attach.invoke(
                                service,
                                plugin.getPluginContext(),
                                mainThread,
                                component.getClassName(),
                                token,
                                app,
                                am);
                        service.onCreate();
                        this.mPluginManager.getComponentsHandler().rememberService(component, service);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                try {
                    IBinder binder = service.onBind(target);
                    IBinder serviceConnection = Plugin.getBinder(intent.getExtras(), "sc");
                    IServiceConnection iServiceConnection = IServiceConnection.Stub.asInterface(serviceConnection);
                    if (Build.VERSION.SDK_INT >= 26) {
                        Reflect.invokeNoException(
                                IServiceConnection.class,
                                iServiceConnection,
                                "connected",
                                new Class[]{
                                        ComponentName.class,
                                        IBinder.class,
                                        boolean.class
                                },
                                new Object[]{
                                        component,
                                        binder,
                                        false
                                }
                        );
                    } else {
                        iServiceConnection.connected(component, binder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case EXTRA_COMMAND_STOP_SERVICE: {
                Service service = this.mPluginManager.getComponentsHandler().forgetService(component);
                if (null != service) {
                    try {
                        service.onDestroy();
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to stop service " + service + ": " + e.toString());
                    }
                } else {
                    Log.i(TAG, component + " not found");
                }
                break;
            }
            case EXTRA_COMMAND_UNBIND_SERVICE: {
                Service service = this.mPluginManager.getComponentsHandler().forgetService(component);
                if (null != service) {
                    try {
                        service.onUnbind(target);
                        service.onDestroy();
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to unbind service " + service + ": " + e.toString());
                    }
                } else {
                    Log.i(TAG, component + " not found");
                }
                break;
            }
        }

        return START_STICKY;
    }

}
