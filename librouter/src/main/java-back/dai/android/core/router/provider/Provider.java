package dai.android.core.router.provider;

import java.util.HashMap;

import dai.android.core.router.action.Action;

public abstract class Provider {

    private boolean _isValid = true;
    private HashMap<String, Action> _actions;

    public Provider() {
        _actions = new HashMap<>();
        register();
    }

    /**
     * register an action with name
     *
     * @param name   the name of this action
     * @param action the action instance object
     */
    protected void register(String name, Action action) {
        if (null != _actions) {
            _actions.put(name, action);
        }
    }

    /**
     * this override by sub Provider class
     */
    protected abstract void register();

    /**
     * find the action instance object by name
     *
     * @param name action name
     * @return the store action instance object
     */
    public Action find(String name) {
        if (null == _actions) return null;

        return _actions.get(name);
    }

}
