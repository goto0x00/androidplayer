package dai.android.core.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import dai.android.core.BaseApplication;
import dai.android.core.debug.logger;

public abstract class AbstractFactoryService extends Service {

    private static final String TAG = AbstractFactoryService.class.getSimpleName();

    protected BaseApplication mBaseApp = BaseApplication.GetThis();

    protected Factory mFactory;

    protected IBinder mBinder = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mFactory = Factory.Get(mBaseApp);
        mFactory.connect(getCurrentServiceClass().getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.d(TAG, "onBind");

        mBinder = getBinder();

        if (mBinder instanceof IFactory) {
            throw new RuntimeException("this Binder must implement '" +
                    IFactory.class.getName() + "'");
        }

        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mFactory) {
            mFactory.release();
            mFactory = null;
        }
    }

    protected abstract Class getCurrentServiceClass();

    protected abstract IBinder getBinder();
}
