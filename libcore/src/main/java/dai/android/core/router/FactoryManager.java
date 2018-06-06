package dai.android.core.router;

import android.app.Application;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dai.android.core.remote.TokenInfo;

public final class FactoryManager extends IFactoryManager.Stub {

    private static FactoryManager msFactoryManager = null;

    public static FactoryManager Get() {
        return msFactoryManager;
    }

    public static FactoryManager Get(Application app) {
        if (null == msFactoryManager) {
            synchronized (FactoryManager.class) {
                if (null == msFactoryManager) {
                    msFactoryManager = new FactoryManager(app);
                }
            }
        }
        return msFactoryManager;
    }

    private Application mApplication;
    private ThreadPoolExecutor mThreadPool;

    Map<TokenInfo, FactoryManagerService.Client> mClients = new HashMap<>();



    private FactoryManager(Application app) {
        mApplication = app;

        mThreadPool = new ThreadPoolExecutor(2, 4, 1, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(16));
    }

    @Override
    public boolean closeFactory(String factory) throws RemoteException {
        return false;
    }

    @Override
    public boolean registerFactory(String factory) throws RemoteException {
        return false;
    }

    @Override
    public void syncTask(String factory) throws RemoteException {
    }
}
