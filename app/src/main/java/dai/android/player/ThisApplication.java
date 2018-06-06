package dai.android.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import dai.android.core.BaseApplication;
import dai.android.core.debug.logger;
import dai.android.core.utility.Process;
import dai.android.player.service.FactoryTestService;
import dai.android.virtual.PluginManager;

public class ThisApplication extends BaseApplication {

    private static final String TAG = "hello";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        long startTime = System.currentTimeMillis();
        PluginManager.getInstance(base).init();

        logger.d(TAG, "use time: " + (System.currentTimeMillis() - startTime));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String processName = Process.getProcessName(this, Process.getPid());
        logger.d(TAG, "Process Name:" + processName);
        if (TextUtils.equals(processName, "dai.android.player")) {
            logger.d(TAG, "hello world");
            Intent intent = new Intent(this, FactoryTestService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
