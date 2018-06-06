package dai.android.core;

import android.app.Application;

public class BaseApplication extends Application {

    private static final String TAG = BaseApplication.class.getName();

    private static BaseApplication msThis;

    public static BaseApplication GetThis() {
        if (null == msThis) {
            throw new RuntimeException(TAG + " or as super class defined at your AndroidManifest");
        }

        return msThis;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        msThis = this;
    }

    /**
     * set this project work at multiple process
     *
     * @return false for default
     */
    public boolean workAtMultipleProcess() {
        return false;
    }
}
