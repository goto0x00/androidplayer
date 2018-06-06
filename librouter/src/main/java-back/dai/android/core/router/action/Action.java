package dai.android.core.router.action;

import android.content.Context;

import java.util.HashMap;

public abstract class Action {

    public abstract boolean isThisAsync(Context context, HashMap<String, String> request);

    public boolean isThisAsync(Context context, HashMap<String, String> request, Object object) {
        return false;
    }

    public abstract ActionResult invoke(Context context, HashMap<String, String> request);

    public ActionResult invoke(Context context, HashMap<String, String> request, Object object) {
        return new ActionResult.Builder()
                .code(ActionResult.CODE_NOT_IMPLEMENT)
                .message("This method has not yet been implemented.")
                .build();
    }
}
