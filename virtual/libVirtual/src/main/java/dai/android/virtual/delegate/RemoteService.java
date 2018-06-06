package dai.android.virtual.delegate;

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;

import dai.android.virtual.PluginManager;
import dai.android.virtual.internal.LoadedPlugin;


public class RemoteService extends LocalService {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        Intent target = intent.getParcelableExtra(EXTRA_TARGET);
        if (target != null) {
            String pluginLocation = intent.getStringExtra(EXTRA_PLUGIN_LOCATION);
            ComponentName component = target.getComponent();
            LoadedPlugin plugin = PluginManager.getInstance(this).getLoadedPlugin(component);
            if (plugin == null && pluginLocation != null) {
                try {
                    PluginManager.getInstance(this).loadPlugin(new File(pluginLocation));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

}
