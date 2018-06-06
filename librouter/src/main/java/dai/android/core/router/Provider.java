package dai.android.core.router;


import android.text.TextUtils;

import java.util.Map;
import java.util.TreeMap;

public abstract class Provider {

    // this provider is valid or not
    private boolean mIsValid = true;

    private Map<String, Action> mActions = null;

    public Provider() {
        mActions = new TreeMap<>();
    }


    /**
     * this Provider is valid or not
     *
     * @return
     */
    public boolean isValid() {
        return mIsValid;
    }

    /**
     * register an action with a name
     *
     * @param name   then name of action
     * @param action class Action object instance
     */
    protected void registerAction(String name, Action action) {
        if (TextUtils.isEmpty(name) || null == action) return;

        if (null != mActions) {
            Action object = mActions.get(name);
            if (null == object) {
                mActions.put(name, action);
            }
        }
    }

    /**
     * find a class Action object instance by name
     *
     * @param name the action name
     * @return
     */
    public Action findAction(String name) {
        if (null != mActions) {
            Action object = mActions.get(name);
            return object;
        }
        return null;
    }


    /**
     * register all actions for this provider
     */
    protected abstract void registerActions();
}
