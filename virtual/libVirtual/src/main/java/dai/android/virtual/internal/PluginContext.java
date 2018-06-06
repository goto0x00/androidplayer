
package dai.android.virtual.internal;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;


class PluginContext extends ContextWrapper {

    private final LoadedPlugin mPlugin;

    public PluginContext(LoadedPlugin plugin) {
        super(plugin.getPluginManager().getHostContext());
        this.mPlugin = plugin;
    }

    @Override
    public Context getApplicationContext() {
        return this.mPlugin.getApplication();
    }

//    @Override
//    public ApplicationInfo getApplicationInfo() {
//        return this.mPlugin.getApplicationInfo();
//    }

    private Context getHostContext() {
        return getBaseContext();
    }

    @Override
    public ContentResolver getContentResolver() {
        return new PluginContentResolver(getHostContext());
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.mPlugin.getClassLoader();
    }

//    @Override
//    public String getPackageName() {
//        return this.mPlugin.getPackageName();
//    }

//    @Override
//    public String getPackageResourcePath() {
//        return this.mPlugin.getPackageResourcePath();
//    }

//    @Override
//    public String getPackageCodePath() {
//        return this.mPlugin.getCodePath();
//    }

    @Override
    public PackageManager getPackageManager() {
        return this.mPlugin.getPackageManager();
    }

    @Override
    public Object getSystemService(String name) {
        // intercept CLIPBOARD_SERVICE,NOTIFICATION_SERVICE
        if (name.equals(Context.CLIPBOARD_SERVICE)) {
            return getHostContext().getSystemService(name);
        } else if (name.equals(Context.NOTIFICATION_SERVICE)) {
            return getHostContext().getSystemService(name);
        }

        return super.getSystemService(name);
    }

    @Override
    public Resources getResources() {
        return this.mPlugin.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return this.mPlugin.getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        return this.mPlugin.getTheme();
    }

    @Override
    public void startActivity(Intent intent) {
        ComponentsHandler componentsHandler = mPlugin.getPluginManager().getComponentsHandler();
        componentsHandler.transformIntentToExplicitAsNeeded(intent);
        super.startActivity(intent);
    }

}
