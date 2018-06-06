package dai.android.core.remote;

import android.os.Parcel;

public class Response extends IObject {

    private int mCallId;
    private int mCode;

    public Response() {
    }

    public Response(int callId, int code) {
        this(callId, code, null);
    }

    public Response(int callId, int code, Object value) {
        super(value);

        mCode = code;
        mCallId = callId;
    }


    public void setCallId(int callId) {
        mCallId = callId;
    }

    public int getCallId() {
        return mCallId;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallId);
        dest.writeInt(mCode);
        super.writeToParcel(dest, flags);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        mCallId = in.readInt();
        mCode = in.readInt();
        super.readFromParcel(in);
    }

    protected Response(Parcel in) {
        super(in);
    }

    public static final Creator<Response> CREATOR = new Creator<Response>() {
        @Override
        public Response createFromParcel(Parcel in) {
            return new Response(in);
        }

        @Override
        public Response[] newArray(int size) {
            return new Response[size];
        }
    };


}
