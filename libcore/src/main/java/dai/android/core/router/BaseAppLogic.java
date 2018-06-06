package dai.android.core.router;

import android.content.res.Configuration;

import dai.android.core.BaseApplication;
import dai.android.core.debug.logger;
import dai.android.core.utility.Process;

public class BaseAppLogic {
    private static final String TAG = BaseAppLogic.class.getName();

    protected BaseApplication mApplication;

    protected BaseAppLogic() {
    }

    protected BaseAppLogic(BaseApplication application) {
        mApplication = application;
    }

    protected void setApplication(BaseApplication application) {
        mApplication = application;
    }

    public void onCreate() {
        println("onCreate");
    }

    public void onTerminate() {
        println("onTerminate");
    }

    public void onLowMemory() {
        println("onLowMemory");
    }

    public void onTrimMemory(int level) {
        println("onTrimMemory");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        println("onConfigurationChanged");
    }

    private void println(String message) {
        logger.d(TAG, message + ", Object:" + hashCode() + ", PID:" + Process.getPid());
    }
}
