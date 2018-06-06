package dai.android.core.router.action;


import org.json.JSONException;
import org.json.JSONObject;

public final class ActionResult {

    public static final int CODE_SUCCESS = 0x0000;
    public static final int CODE_ERROR = 0x0001;
    public static final int CODE_NOT_FOUND = 0x0002;
    public static final int CODE_INVALID = 0x0003;
    public static final int CODE_ROUTER_NOT_REGISTER = 0x0004;
    public static final int CODE_CANNOT_BIND_LOCAL = 0x0005;
    public static final int CODE_REMOTE_EXCEPTION = 0x0006;
    public static final int CODE_CANNOT_BIND_WIDE = 0x0007;
    public static final int CODE_TARGET_IS_WIDE = 0x0008;
    public static final int CODE_WIDE_STOPPING = 0x0009;
    public static final int CODE_NOT_IMPLEMENT = 0x000A;

    private int _code;
    private String _message;
    private String _data;
    private Object _object;

    private ActionResult(Builder builder) {
        _code = builder.mCode;
        _message = builder.mMessage;
        _data = builder.mData;
        _object = builder.mObject;
    }

    public int code() {
        return _code;
    }

    public String data() {
        return _data;
    }

    public String message() {
        return _message;
    }

    public Object object() {
        return _object;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", _code);
            jsonObject.put("message", _message);
            jsonObject.put("data", _data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static class Builder {

        private int mCode;
        private String mMessage;
        private String mData;
        private Object mObject;

        public Builder() {
            mCode = CODE_ERROR;
            mMessage = "";
            mObject = null;
            mData = null;
        }

        public Builder resultString(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                this.mCode = jsonObject.getInt("code");
                this.mMessage = jsonObject.getString("message");
                this.mData = jsonObject.getString("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder code(int code) {
            this.mCode = code;
            return this;
        }

        public Builder message(String msg) {
            this.mMessage = msg;
            return this;
        }

        public Builder data(String data) {
            this.mData = data;
            return this;
        }

        public Builder object(Object object) {
            this.mObject = object;
            return this;
        }

        public ActionResult build() {
            return new ActionResult(this);
        }

    }
}
