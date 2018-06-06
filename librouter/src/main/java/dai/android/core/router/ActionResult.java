package dai.android.core.router;

import android.os.Parcel;

public class ActionResult extends ActionData {

    private static final String TAG = ActionResult.class.getSimpleName();


    //----------------------------------------------------------------------------------------------
    /**
     * this call is OK
     */
    public static final int RESULT_OK = 0x00;

    /**
     * this call is failed
     */
    public static final int RESULT_FAILED = 0x01;


    //----------------------------------------------------------------------------------------------

    private int mCode;

    /**
     * constructor
     * default is call OK, and is a remote
     */
    public ActionResult() {
        this(true);
    }

    /**
     * constructor
     *
     * @param code the call result code
     */
    public ActionResult(int code) {
        this(code, true, null);
    }

    /**
     * constructor
     *
     * @param toRemote this ActionData will send to remote process
     */
    public ActionResult(boolean toRemote) {
        this(toRemote, null);
    }

    /**
     * constructor
     * default call OK result
     *
     * @param toRemote this ActionData will send to remote process
     * @param value    the call param object
     */
    public ActionResult(boolean toRemote, Object value) {
        this(RESULT_OK, toRemote, value);
    }


    public ActionResult(int code, boolean toRemote, Object value) {
        super(toRemote, value);
        mCode = code;
    }

    /**
     * get the call result code
     *
     * @return
     */
    public int getCode() {
        return mCode;
    }

    /**
     * set the call result code
     *
     * @param code
     */
    public void setCode(int code) {
        mCode = code;
    }

    protected ActionResult(Parcel in) {
        super(in);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        //dest.writeString(getClass().getName());
        dest.writeInt(mCode);
    }

    public static final Creator<ActionResult> CREATOR = new Creator<ActionResult>() {

        @Override
        public ActionResult createFromParcel(Parcel source) {
            return new ActionResult(source);
        }

        @Override
        public ActionResult[] newArray(int size) {
            return new ActionResult[size];
        }
    };
}
