
package dai.android.virtual.internal;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import dai.android.virtual.PluginManager;
import dai.android.virtual.utility.Reflect;


class ResourcesManager {

    public static synchronized Resources createResources(Context hostContext, String apk) {
        Resources hostResources = hostContext.getResources();
        Resources newResources = null;
        AssetManager assetManager;

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                assetManager = AssetManager.class.newInstance();
                Reflect.invoke(
                        AssetManager.class,
                        assetManager,
                        "addAssetPath",
                        hostContext.getApplicationInfo().sourceDir
                );
            } else {
                assetManager = hostResources.getAssets();
            }

            Reflect.invoke(AssetManager.class, assetManager, "addAssetPath", apk);
            List<LoadedPlugin> pluginList =
                    PluginManager.getInstance(hostContext).getAllLoadedPlugins();

            for (LoadedPlugin plugin : pluginList) {
                Reflect.invoke(
                        AssetManager.class,
                        assetManager,
                        "addAssetPath",
                        plugin.getLocation());
            }

            if (isMiUi(hostResources)) {
                newResources =
                        MiUiResourcesCompat.createResources(hostResources, assetManager);
            } else if (isVivo(hostResources)) {
                newResources =
                        VivoResourcesCompat.createResources(hostContext, hostResources, assetManager);
            } else if (isNubia(hostResources)) {
                newResources =
                        NubiaResourcesCompat.createResources(hostResources, assetManager);
            } else if (isNotRawResources(hostResources)) {
                newResources =
                        AdaptationResourcesCompat.createResources(hostResources, assetManager);
            } else {
                // is raw android resources
                newResources = new Resources(
                        assetManager,
                        hostResources.getDisplayMetrics(),
                        hostResources.getConfiguration()
                );
            }

            // lastly, sync all LoadedPlugin to newResources
            for (LoadedPlugin plugin : pluginList) {
                plugin.updateResources(newResources);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newResources;
    }

    @SuppressWarnings("unchecked")
    public static void hookResources(Context base, Resources resources) {
        try {
            Reflect.setField(base.getClass(), base, "mResources", resources);
            Object loadedApk = Reflect.getPackageInfo(base);
            Reflect.setField(loadedApk.getClass(), loadedApk, "mResources", resources);

            Object activityThread = Reflect.getActivityThread(base);
            Object resManager = Reflect.getField(
                    activityThread.getClass(),
                    activityThread,
                    "mResourcesManager"
            );

            if (Build.VERSION.SDK_INT < 24) {
                Map<Object, WeakReference<Resources>> map =
                        (Map<Object, WeakReference<Resources>>) Reflect.getField(
                                resManager.getClass(),
                                resManager,
                                "mActiveResources"
                        );

                Object key = map.keySet().iterator().next();
                map.put(key, new WeakReference<>(resources));
            } else {
                // still hook Android N Resources, even though it's unnecessary, then nobody will be strange.
                Map map = (Map) Reflect.getFieldNoException(
                        resManager.getClass(),
                        resManager,
                        "mResourceImpls"
                );

                Object key = map.keySet().iterator().next();
                Object resourcesImpl = Reflect.getFieldNoException(
                        Resources.class,
                        resources,
                        "mResourcesImpl"
                );

                map.put(key, new WeakReference<>(resourcesImpl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isMiUi(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.MiuiResources");
    }

    private static boolean isVivo(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.VivoResources");
    }

    private static boolean isNubia(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.NubiaResources");
    }

    private static boolean isNotRawResources(Resources resources) {
        return !resources.getClass().getName().equals("android.content.res.Resources");
    }


    private static final class MiUiResourcesCompat {
        private static Resources createResources(Resources hostResources,
                                                 AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.MiuiResources");
            Resources newResources = (Resources) Reflect.invokeConstructor(
                    resourcesClazz,
                    new Class[]{
                            AssetManager.class,
                            DisplayMetrics.class,
                            Configuration.class
                    },
                    new Object[]{
                            assetManager,
                            hostResources.getDisplayMetrics(),
                            hostResources.getConfiguration()}
            );
            return newResources;
        }
    }

    private static final class VivoResourcesCompat {
        private static Resources createResources(Context hostContext,
                                                 Resources hostResources,
                                                 AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.VivoResources");
            Resources newResources = (Resources) Reflect.invokeConstructor(
                    resourcesClazz,
                    new Class[]{
                            AssetManager.class,
                            DisplayMetrics.class,
                            Configuration.class
                    },
                    new Object[]{
                            assetManager,
                            hostResources.getDisplayMetrics(),
                            hostResources.getConfiguration()}
            );

            Reflect.invokeNoException(
                    resourcesClazz,
                    newResources,
                    "init",
                    new Class[]{String.class},
                    hostContext.getPackageName()
            );
            Object themeValues = Reflect.getFieldNoException(
                    resourcesClazz, hostResources, "mThemeValues");
            Reflect.setFieldNoException(resourcesClazz, newResources, "mThemeValues", themeValues);

            return newResources;
        }
    }

    private static final class NubiaResourcesCompat {
        private static Resources createResources(Resources hostResources,
                                                 AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.NubiaResources");
            Resources newResources = (Resources) Reflect.invokeConstructor(
                    resourcesClazz,
                    new Class[]{
                            AssetManager.class,
                            DisplayMetrics.class,
                            Configuration.class},
                    new Object[]{
                            assetManager,
                            hostResources.getDisplayMetrics(),
                            hostResources.getConfiguration()}
            );
            return newResources;
        }
    }

    private static final class AdaptationResourcesCompat {
        private static Resources createResources(Resources hostResources,
                                                 AssetManager assetManager) throws Exception {
            Resources newResources;
            try {
                Class resourcesClazz = hostResources.getClass();
                newResources = (Resources) Reflect.invokeConstructor(
                        resourcesClazz,
                        new Class[]{
                                AssetManager.class,
                                DisplayMetrics.class,
                                Configuration.class
                        },
                        new Object[]{
                                assetManager,
                                hostResources.getDisplayMetrics(),
                                hostResources.getConfiguration()}
                );
            } catch (Exception e) {
                newResources = new Resources(
                        assetManager,
                        hostResources.getDisplayMetrics(),
                        hostResources.getConfiguration()
                );
            }

            return newResources;
        }
    }

}
