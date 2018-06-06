package dai.android.core.router;

import dai.android.core.BaseApplication;

public abstract class RApplication extends BaseApplication {

    private static final String TAG = RApplication.class.getSimpleName();

    // single mode get this Application
    private static RApplication _thiz = null;

    /**
     * get this class instance object
     *
     * @return this class instance
     */
    public static RApplication Get() {
        return _thiz;
    }

    public abstract void initAllProcessRouter();

    protected abstract void initLogic();

    /**
     * is this application run in more than one process
     *
     * @return true/false
     */
    public abstract boolean multipleProcess();
}
