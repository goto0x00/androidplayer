
package dai.android.virtual;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Singleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dai.android.virtual.delegate.ActivityManagerProxy;
import dai.android.virtual.delegate.IContentProviderProxy;
import dai.android.virtual.internal.ComponentsHandler;
import dai.android.virtual.internal.LoadedPlugin;
import dai.android.virtual.internal.PluginContentResolver;
import dai.android.virtual.internal.VInstrumentation;
import dai.android.virtual.utility.Plugin;
import dai.android.virtual.utility.Reflect;
import dai.android.virtual.utility.Run;

public class PluginManager {

    public static final String TAG = PluginManager.class.getSimpleName();

    private static volatile PluginManager sInstance = null;

    // Context of host app
    private Context mContext;
    private ComponentsHandler mComponentsHandler;
    private Map<String, LoadedPlugin> mPlugins = new ConcurrentHashMap<>();

    private Instrumentation mInstrumentation; // Hooked instrumentation
    private IActivityManager mActivityManager; // Hooked IActivityManager binder
    private IContentProvider mIContentProvider; // Hooked IContentProvider binder

    public static PluginManager getInstance(Context base) {
        if (sInstance == null) {
            synchronized (PluginManager.class) {
                if (sInstance == null)
                    sInstance = new PluginManager(base);
            }
        }

        return sInstance;
    }

    private PluginManager(Context context) {
        Context app = context.getApplicationContext();
        if (app == null) {
            this.mContext = context;
        } else {
            this.mContext = ((Application) app).getBaseContext();
        }
        prepare();
    }

    private void prepare() {
        Systems.sHostContext = getHostContext();
        this.hookInstrumentationAndHandler();
        if (Build.VERSION.SDK_INT >= 26) {
            this.hookAMSForO();
        } else {
            this.hookSystemServices();
        }
    }

    public void init() {
        mComponentsHandler = new ComponentsHandler(this);
        Run.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                doInWorkThread();
            }
        });
    }

    private void doInWorkThread() {
    }

    /**
     * hookSystemServices, but need to compatible with Android O in future.
     */
    private void hookSystemServices() {
        try {
            Singleton<IActivityManager> defaultSingleton =
                    (Singleton<IActivityManager>) Reflect.getField(
                            ActivityManagerNative.class,
                            null,
                            "gDefault"
                    );

            IActivityManager activityManagerProxy =
                    ActivityManagerProxy.newInstance(this, defaultSingleton.get());

            // Hook IActivityManager from ActivityManagerNative
            Reflect.setField(
                    defaultSingleton.getClass().getSuperclass(),
                    defaultSingleton,
                    "mInstance",
                    activityManagerProxy
            );

            if (defaultSingleton.get() == activityManagerProxy) {
                this.mActivityManager = activityManagerProxy;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hookAMSForO() {
        try {
            Singleton<IActivityManager> defaultSingleton =
                    (Singleton<IActivityManager>) Reflect.getField(
                            ActivityManager.class,
                            null,
                            "IActivityManagerSingleton"
                    );
            IActivityManager activityManagerProxy =
                    ActivityManagerProxy.newInstance(this, defaultSingleton.get());
            Reflect.setField(
                    defaultSingleton.getClass().getSuperclass(),
                    defaultSingleton,
                    "mInstance",
                    activityManagerProxy
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hookInstrumentationAndHandler() {
        try {
            Instrumentation baseInstrumentation = Reflect.getInstrumentation(this.mContext);
            if (baseInstrumentation.getClass().getName().contains("lbe")) {
                // reject executing in paralell space, for example, lbe.
                System.exit(0);
            }

            final VInstrumentation instrumentation = new VInstrumentation(this, baseInstrumentation);
            Object activityThread = Reflect.getActivityThread(this.mContext);
            Reflect.setInstrumentation(activityThread, instrumentation);
            Reflect.setHandlerCallback(this.mContext, instrumentation);
            this.mInstrumentation = instrumentation;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hookIContentProviderAsNeeded() {
        Uri uri = Uri.parse(PluginContentResolver.getUri(mContext));
        mContext.getContentResolver().call(uri, "wakeup", null, null);
        try {
            Field authority = null;
            Field mProvider = null;
            ActivityThread activityThread = (ActivityThread) Reflect.getActivityThread(mContext);
            Map mProviderMap = (Map) Reflect.getField(
                    activityThread.getClass(),
                    activityThread,
                    "mProviderMap"
            );
            Iterator iter = mProviderMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                String auth;
                if (key instanceof String) {
                    auth = (String) key;
                } else {
                    if (authority == null) {
                        authority = key.getClass().getDeclaredField("authority");
                        authority.setAccessible(true);
                    }
                    auth = (String) authority.get(key);
                }
                if (auth.equals(PluginContentResolver.getAuthority(mContext))) {
                    if (mProvider == null) {
                        mProvider = val.getClass().getDeclaredField("mProvider");
                        mProvider.setAccessible(true);
                    }
                    IContentProvider rawProvider = (IContentProvider) mProvider.get(val);
                    IContentProvider proxy = IContentProviderProxy.newInstance(mContext, rawProvider);
                    mIContentProvider = proxy;
                    Log.d(TAG, "hookIContentProvider succeed : " + mIContentProvider);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * load a plugin into memory, then invoke it's Application.
     *
     * @param apk the file of plugin, should end with .apk
     * @throws Exception
     */
    public void loadPlugin(File apk) throws Exception {
        if (null == apk) {
            throw new IllegalArgumentException("error : apk is null.");
        }

        if (!apk.exists()) {
            throw new FileNotFoundException(apk.getAbsolutePath());
        }

        LoadedPlugin plugin = LoadedPlugin.create(this, this.mContext, apk);
        if (null != plugin) {
            this.mPlugins.put(plugin.getPackageName(), plugin);
            // try to invoke plugin's application
            plugin.invokeApplication();
        } else {
            throw new RuntimeException("Can't load plugin which is invalid: " + apk.getAbsolutePath());
        }
    }

    public LoadedPlugin getLoadedPlugin(Intent intent) {
        ComponentName component = Plugin.getComponent(intent);
        return getLoadedPlugin(component.getPackageName());
    }

    public LoadedPlugin getLoadedPlugin(ComponentName component) {
        return this.getLoadedPlugin(component.getPackageName());
    }

    public LoadedPlugin getLoadedPlugin(String packageName) {
        return this.mPlugins.get(packageName);
    }

    public List<LoadedPlugin> getAllLoadedPlugins() {
        List<LoadedPlugin> list = new ArrayList<>();
        list.addAll(mPlugins.values());
        return list;
    }

    public Context getHostContext() {
        return this.mContext;
    }

    public Instrumentation getInstrumentation() {
        return this.mInstrumentation;
    }

    public IActivityManager getActivityManager() {
        return this.mActivityManager;
    }

    public synchronized IContentProvider getIContentProvider() {
        if (mIContentProvider == null) {
            hookIContentProviderAsNeeded();
        }

        return mIContentProvider;
    }

    public ComponentsHandler getComponentsHandler() {
        return mComponentsHandler;
    }

    public ResolveInfo resolveActivity(Intent intent) {
        return this.resolveActivity(intent, 0);
    }

    public ResolveInfo resolveActivity(Intent intent, int flags) {
        for (LoadedPlugin plugin : this.mPlugins.values()) {
            ResolveInfo resolveInfo = plugin.resolveActivity(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }
        }

        return null;
    }

    public ResolveInfo resolveService(Intent intent, int flags) {
        for (LoadedPlugin plugin : this.mPlugins.values()) {
            ResolveInfo resolveInfo = plugin.resolveService(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }
        }

        return null;
    }

    public ProviderInfo resolveContentProvider(String name, int flags) {
        for (LoadedPlugin plugin : this.mPlugins.values()) {
            ProviderInfo providerInfo = plugin.resolveContentProvider(name, flags);
            if (null != providerInfo) {
                return providerInfo;
            }
        }

        return null;
    }

    /**
     * used in PluginPackageManager, do not invoke it from outside.
     */
    @Deprecated
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

        for (LoadedPlugin plugin : this.mPlugins.values()) {
            List<ResolveInfo> result = plugin.queryIntentActivities(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }

    /**
     * used in PluginPackageManager, do not invoke it from outside.
     */
    @Deprecated
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

        for (LoadedPlugin plugin : this.mPlugins.values()) {
            List<ResolveInfo> result = plugin.queryIntentServices(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }

    /**
     * used in PluginPackageManager, do not invoke it from outside.
     */
    @Deprecated
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<>();

        for (LoadedPlugin plugin : this.mPlugins.values()) {
            List<ResolveInfo> result = plugin.queryBroadcastReceivers(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }

}