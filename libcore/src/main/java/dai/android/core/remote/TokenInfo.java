package dai.android.core.remote;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Locale;

import dai.android.core.utility.Process;

public final class TokenInfo implements Parcelable {

    private static final String TAG = TokenInfo.class.getSimpleName();

    private static final String KEY = TokenInfo.class.getName();

    public static Bundle make(String domain, IBinder binder) {
        Bundle token = new Bundle();
        token.putParcelable(KEY, new TokenInfo(domain, binder));
        return token;
    }

    public static TokenInfo parse(Bundle bundle) {
        return bundle.getParcelable(KEY);
    }


    public String domain = null;
    public int pid = Process.getPid();
    public int uid = Process.getUid();
    public IBinder binder = new Binder();

    public TokenInfo() {
    }

    public TokenInfo(String domain) {
        this.domain = domain;
    }

    public TokenInfo(String domain, IBinder binder) {
        this.domain = domain;

        if (null != binder) {
            this.binder = binder;
        }
    }

    @Override
    public boolean equals(Object thiz) {
        if (this == thiz) {
            return true;
        }

        if (thiz instanceof TokenInfo) {
            TokenInfo info = (TokenInfo) thiz;
            if (TextUtils.equals(info.domain, domain)) {
                return pid == info.pid && uid == info.uid;
            }
        }

        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("\t domain = ");
        sb.append(domain);

        sb.append("\n");
        sb.append("\t uid    = ");
        sb.append(uid);

        sb.append("\n");
        sb.append("\t pid    = ");
        sb.append(pid);

        if (null != binder) {
            sb.append("\n");
            sb.append("\t binder = ");
            sb.append("hashCode:");
            sb.append(binder.hashCode());
            sb.append(", alive:");
            sb.append(binder.isBinderAlive());
        }

        return sb.toString();
    }

    public String toSimpleString() {
        return String.format(Locale.CHINA, "%s:{[uid/pid]=[%d/%d]}", domain, uid, pid);
    }

    @Override
    public int hashCode() {
        return pid + uid;
    }

    protected TokenInfo(Parcel in) {
        domain = in.readString();
        uid = in.readInt();
        pid = in.readInt();
        binder = in.readStrongBinder();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(domain);
        dest.writeInt(uid);
        dest.writeInt(pid);
        dest.writeStrongBinder(binder);
    }

    public static final Creator<TokenInfo> CREATOR = new Creator<TokenInfo>() {
        @Override
        public TokenInfo createFromParcel(Parcel in) {
            return new TokenInfo(in);
        }

        @Override
        public TokenInfo[] newArray(int size) {
            return new TokenInfo[size];
        }
    };
}
