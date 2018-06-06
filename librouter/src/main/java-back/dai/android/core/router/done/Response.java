package dai.android.core.router.done;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import dai.android.core.router.remote.Info;

public class Response {

    private static final int TIME_OUT = 1000 * 30;

    private long mTimeOut = 0;
    private boolean mHasGot = false;
    private boolean mIsAsync = true;

    private int mCode = -1;
    private String mMessage = "";
    private String mData;
    private Object mObject;

    // ActionResult3.toString
    private String mResultString;

    private Future<String> mAsyncResponse;

    /**
     * constructor function
     *
     * @param timeout the time out
     */
    public Response(long timeout) {
        if (timeout > 2 * TIME_OUT || timeout < 0) {
            timeout = TIME_OUT;
        }

        mTimeOut = timeout;
    }

    /**
     * the default constructor function
     * the time out TIME_OUT
     */
    public Response() {
        this(TIME_OUT);
    }

    /**
     * this an asynchronous call
     *
     * @return true: this an asynchronous, false: this not asynchronous
     */
    public boolean isAsync() {
        return mIsAsync;
    }

    /**
     * set this call is asynchronous
     *
     * @param aync
     */
    public void setAync(boolean aync) {
        mIsAsync = aync;
    }

    /**
     * the the response code
     *
     * @return the int response code
     * @throws Exception
     */
    public int code() throws Exception {
        if (!mHasGot) {
            get();
        }
        return mCode;
    }

    /**
     * the response message
     *
     * @return
     * @throws Exception
     */
    public String message() throws Exception {
        if (!mHasGot) {
            get();
        }
        return mMessage;
    }

    /**
     * get the response Object
     *
     * @return
     * @throws Exception
     */
    public Object object() throws Exception {
        if (!mHasGot) {
            get();
        }
        return mObject;
    }

    /**
     * set the response object
     *
     * @param obj
     */
    public void object(Object obj) {
        mObject = obj;
    }

    /**
     * get the response data
     *
     * @return response data string
     * @throws Exception
     */
    public String data() throws Exception {
        if (!mHasGot) {
            get();
        }
        return mData;
    }

    /**
     * get the call response result string
     *
     * @return response result string
     * @throws Exception
     */
    public String get() throws Exception {
        if (mIsAsync) {
            mResultString = mAsyncResponse.get(mTimeOut, TimeUnit.MILLISECONDS);
        }
        pauseResult();
        return mResultString;
    }

    /**
     * set the result string
     *
     * @param result
     */
    public void setResultString(String result) {
        mResultString = result;
    }

    /**
     * set the asynchronous run task
     *
     * @param submit
     */
    public void asyncResponse(Future<String> submit) {
        mAsyncResponse = submit;
    }

    private void pauseResult() {
        if (!mHasGot) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(mResultString);
                mCode = jsonObject.getInt(Info.CODE);
                mMessage = jsonObject.getString(Info.MESSAGE);
                mData = jsonObject.getString(Info.DATA);

                mHasGot = true;
            } catch (JSONException e) {
                mHasGot = false;
                e.printStackTrace();
            }
        }
    }

}
