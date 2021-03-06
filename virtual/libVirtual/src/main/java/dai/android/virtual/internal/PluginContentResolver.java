
package dai.android.virtual.internal;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Keep;

import java.lang.reflect.Method;

import dai.android.virtual.PluginManager;
import dai.android.virtual.delegate.RemoteContentProvider;


public class PluginContentResolver extends ContentResolver {
    private ContentResolver mBase;
    private PluginManager mPluginManager;
    private static Method sAcquireProvider;
    private static Method sAcquireExistingProvider;
    private static Method sAcquireUnstableProvider;

    static {
        try {
            sAcquireProvider = ContentResolver.class.getDeclaredMethod(
                    "acquireProvider",
                    new Class[]{
                            Context.class,
                            String.class}
            );
            sAcquireProvider.setAccessible(true);

            sAcquireExistingProvider = ContentResolver.class.getDeclaredMethod(
                    "acquireExistingProvider",
                    new Class[]{
                            Context.class,
                            String.class}
            );
            sAcquireExistingProvider.setAccessible(true);

            sAcquireUnstableProvider = ContentResolver.class.getDeclaredMethod(
                    "acquireUnstableProvider",
                    new Class[]{
                            Context.class,
                            String.class}
            );
            sAcquireUnstableProvider.setAccessible(true);
        } catch (Exception e) {
            //ignored
        }
    }

    public PluginContentResolver(Context context) {
        super(context);
        mBase = context.getContentResolver();
        mPluginManager = PluginManager.getInstance(context);
    }

    protected IContentProvider acquireProvider(Context context, String auth) {
        try {
            if (mPluginManager.resolveContentProvider(auth, 0) != null) {
                return mPluginManager.getIContentProvider();
            }

            return (IContentProvider) sAcquireProvider.invoke(mBase, context, auth);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected IContentProvider acquireExistingProvider(Context context, String auth) {
        try {
            if (mPluginManager.resolveContentProvider(auth, 0) != null) {
                return mPluginManager.getIContentProvider();
            }

            return (IContentProvider) sAcquireExistingProvider.invoke(mBase, context, auth);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected IContentProvider acquireUnstableProvider(Context context, String auth) {
        try {
            if (mPluginManager.resolveContentProvider(auth, 0) != null) {
                return mPluginManager.getIContentProvider();
            }

            return (IContentProvider) sAcquireUnstableProvider.invoke(mBase, context, auth);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean releaseProvider(IContentProvider provider) {
        return true;
    }

    public boolean releaseUnstableProvider(IContentProvider icp) {
        return true;
    }

    public void unstableProviderDied(IContentProvider icp) {
    }

    public void appNotRespondingViaProvider(IContentProvider icp) {
    }

    /**
     * @hide
     */
    protected int resolveUserIdFromAuthority(String auth) {
        return 0;
    }

    @Keep
    public static Uri wrapperUri(LoadedPlugin loadedPlugin, Uri pluginUri) {
        String pkg = loadedPlugin.getPackageName();
        String pluginUriString = Uri.encode(pluginUri.toString());
        StringBuilder builder = new StringBuilder(PluginContentResolver.getUri(loadedPlugin.getHostContext()));
        builder.append("/?plugin=" + loadedPlugin.getLocation());
        builder.append("&pkg=" + pkg);
        builder.append("&uri=" + pluginUriString);
        Uri wrapperUri = Uri.parse(builder.toString());
        return wrapperUri;
    }

    @Deprecated
    public static String getAuthority(Context context) {
        return context.getPackageName() + ".VirtualAPK.Provider";
    }

    @Deprecated
    public static String getUri(Context context) {
        return "content://" + getAuthority(context);
    }

    @Keep
    public static Bundle getBundleForCall(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(RemoteContentProvider.KEY_WRAPPER_URI, uri.toString());
        return bundle;
    }

}
