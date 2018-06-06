package dai.android.core.router;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import dai.android.core.remote.TokenInfo;

public final class Factory {

    private static Factory msInstance = null;

    public static Factory Get() {
        return msInstance;
    }

    public static Factory Get(Application app) {
        if (null == msInstance) {
            synchronized (Factory.class) {
                if (null == msInstance) {
                    msInstance = new Factory(app);
                }
            }
        }
        return msInstance;
    }


    private Application mApplication;
    private IFactoryManager mIFactoryManager;
    private IBinder mBinder;

    private Factory(Application app) {
        mApplication = app;
    }

    protected void release() {
    }

    void setBinder(IBinder binder) {
        mBinder = binder;
    }

    void connect(String domain) {
        Intent intent = new Intent(mApplication.getBaseContext(), FactoryManagerService.class);
        intent.putExtras(TokenInfo.make(domain, mBinder));
        //mApplication.startService(intent);
        mApplication.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIFactoryManager = IFactoryManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (null != mIFactoryManager) {
                mIFactoryManager = null;
            }
        }
    };


}
