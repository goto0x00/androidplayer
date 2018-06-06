package dai.android.core.remote;

import android.os.Parcel;

public class Request extends IObject {

    public Request() {
    }

    public Request(Object value) {
        super(value);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected Request(Parcel in) {
        super(in);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };
}
