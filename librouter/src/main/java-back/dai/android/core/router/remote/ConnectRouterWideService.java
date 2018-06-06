package dai.android.core.router.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import dai.android.core.router.RApplication;

public class ConnectRouterWideService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!(getApplication() instanceof RApplication)) {
            throw new RuntimeException("The Application must instance of 'RApplication'");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
