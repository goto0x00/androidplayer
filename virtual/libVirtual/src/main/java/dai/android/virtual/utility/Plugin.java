
package dai.android.virtual.utility;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dai.android.virtual.PluginManager;
import dai.android.virtual.internal.Constants;
import dai.android.virtual.internal.LoadedPlugin;

public class Plugin {

    public static String getTargetActivity(Intent intent) {
        return intent.getStringExtra(Constants.KEY_TARGET_ACTIVITY);
    }

    public static ComponentName getComponent(Intent intent) {
        return new ComponentName(intent.getStringExtra(Constants.KEY_TARGET_PACKAGE),
                intent.getStringExtra(Constants.KEY_TARGET_ACTIVITY));
    }

    public static boolean isIntentFromPlugin(Intent intent) {
        return intent.getBooleanExtra(Constants.KEY_IS_PLUGIN, false);
    }

    public static int getTheme(Context context, Intent intent) {
        return Plugin.getTheme(context, Plugin.getComponent(intent));
    }

    public static int getTheme(Context context, ComponentName component) {
        LoadedPlugin loadedPlugin = PluginManager.getInstance(context).getLoadedPlugin(component);

        if (null == loadedPlugin) {
            return 0;
        }

        ActivityInfo info = loadedPlugin.getActivityInfo(component);
        if (null == info) {
            return 0;
        }

        if (0 != info.theme) {
            return info.theme;
        }

        ApplicationInfo appInfo = info.applicationInfo;
        if (null != appInfo && appInfo.theme != 0) {
            return appInfo.theme;
        }

        return Plugin.selectDefaultTheme(0, Build.VERSION.SDK_INT);
    }

    public static int selectDefaultTheme(final int curTheme, final int targetSdkVersion) {
        return selectSystemTheme(curTheme, targetSdkVersion,
                android.R.style.Theme,
                android.R.style.Theme_Holo,
                android.R.style.Theme_DeviceDefault,
                android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
    }

    private static int selectSystemTheme(final int curTheme,
                                         final int targetSdkVersion,
                                         final int orig,
                                         final int holo,
                                         final int dark,
                                         final int deviceDefault) {
        if (curTheme != 0) {
            return curTheme;
        }

        if (targetSdkVersion < 11 /* Build.VERSION_CODES.HONEYCOMB */) {
            return orig;
        }

        if (targetSdkVersion < 14 /* Build.VERSION_CODES.ICE_CREAM_SANDWICH */) {
            return holo;
        }

        if (targetSdkVersion < 24 /* Build.VERSION_CODES.N */) {
            return dark;
        }

        return deviceDefault;
    }

    public static void hookActivityResources(Activity activity, String packageName) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isVivo(activity.getResources())) {
            // for 5.0+ vivo
            return;
        }

        // designed for 5.0 - only, but some bad phones not work, eg:letv
        try {
            Context base = activity.getBaseContext();
            final LoadedPlugin plugin = PluginManager.getInstance(activity).getLoadedPlugin(packageName);
            final Resources resources = plugin.getResources();
            if (resources != null) {
                Reflect.setField(base.getClass(), base, "mResources", resources);

                // copy theme
                Resources.Theme theme = resources.newTheme();
                theme.setTo(activity.getTheme());
                int themeResource = (int) Reflect.getField(
                        ContextThemeWrapper.class,
                        activity,
                        "mThemeResource");
                theme.applyStyle(themeResource, true);
                Reflect.setField(ContextThemeWrapper.class, activity, "mTheme", theme);
                Reflect.setField(ContextThemeWrapper.class, activity, "mResources", resources);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final boolean isLocalService(final ServiceInfo serviceInfo) {
        return TextUtils.isEmpty(serviceInfo.processName) ||
                serviceInfo.applicationInfo.packageName.equals(serviceInfo.processName);
    }

    public static boolean isVivo(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.VivoResources");
    }

    public static void putBinder(Bundle bundle, String key, IBinder value) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bundle.putBinder(key, value);
        } else {
            try {
                Reflect.invoke(
                        Bundle.class,
                        bundle,
                        "putIBinder",
                        new Class[]{
                                String.class,
                                IBinder.class
                        },
                        key,
                        value);

            } catch (Exception e) {
            }
        }
    }

    public static IBinder getBinder(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return bundle.getBinder(key);
        } else {
            try {
                return (IBinder) Reflect.invoke(Bundle.class, bundle, "getIBinder", key);
            } catch (Exception e) {
            }

            return null;
        }
    }

    public static void copyNativeLib(File apk,
                                     Context context,
                                     PackageInfo packageInfo,
                                     File nativeLibDir) {
        try {
            String cpuArch;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cpuArch = Build.SUPPORTED_ABIS[0];
            } else {
                cpuArch = Build.CPU_ABI;
            }
            boolean findSo = false;

            ZipFile zipfile = new ZipFile(apk.getAbsolutePath());
            ZipEntry entry;
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory())
                    continue;
                if (entry.getName().endsWith(".so") && entry.getName().contains("lib/" + cpuArch)) {
                    findSo = true;
                    break;
                }
            }
            e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".so"))
                    continue;
                if ((findSo && entry.getName().contains("lib/" + cpuArch)) ||
                        (!findSo && entry.getName().contains("lib/armeabi/"))) {
                    String[] temp = entry.getName().split("/");
                    String libName = temp[temp.length - 1];
                    System.out.println("verify so " + libName);
                    File libFile = new File(nativeLibDir.getAbsolutePath() + File.separator + libName);
                    String key = packageInfo.packageName + "_" + libName;
                    if (libFile.exists()) {
                        int VersionCode = Settings.getSoVersion(context, key);
                        if (VersionCode == packageInfo.versionCode) {
                            System.out.println("skip existing so : " + entry.getName());
                            continue;
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(libFile);
                    System.out.println("copy so " + entry.getName() + " of " + cpuArch);
                    copySo(zipfile.getInputStream(entry), fos);
                    Settings.setSoVersion(context, key, packageInfo.versionCode);
                }

            }

            zipfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copySo(InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        int count;
        byte data[] = new byte[8192];
        while ((count = bufferedInput.read(data, 0, 8192)) != -1) {
            bufferedOutput.write(data, 0, count);
        }
        bufferedOutput.flush();
        bufferedOutput.close();
        output.close();
        bufferedInput.close();
        input.close();
    }

}
