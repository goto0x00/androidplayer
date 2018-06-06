package dai.android.core.router.action;

import android.content.Context;

import java.util.HashMap;

public class BadAction extends Action {

    private static final String DEFAULT_MESSAGE = "Something was wrong.";
    private int mCode;
    private String mMessage;
    private boolean mAsync;

    public BadAction(boolean async, int code, String message) {
        this.mCode = code;
        this.mMessage = message;
        this.mAsync = async;
    }


    @Override
    public boolean isThisAsync(Context context, HashMap<String, String> request) {
        return mAsync;
    }

    @Override
    public ActionResult invoke(Context context, HashMap<String, String> request) {
        ActionResult result = new ActionResult.Builder()
                .code(mCode)
                .message(mMessage)
                .data(null)
                .object(null)
                .build();
        return result;
    }
}
