package dai.android.player.data;

import android.os.Parcel;

public class Employee extends Person {

    private String mOrgName;

    private Employee() {
    }

    public Employee(String name, int age, String orgName) {
        super(name, age);
        mOrgName = orgName;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Employee[");
        stringBuilder.append(hashCode());
        stringBuilder.append("]\n");
        stringBuilder.append("mOrgName: ");
        stringBuilder.append(mOrgName);
        stringBuilder.append("\n");
        stringBuilder.append(super.toString());

        return stringBuilder.toString();
    }

    protected Employee(Parcel in) {
        super(in);
    }

    protected void readFromParcel(Parcel in) {
        /// mOrgName = in.readString();
        super.readFromParcel(in);

        mOrgName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ///dest.writeString(mOrgName);

        super.writeToParcel(dest, flags);

        dest.writeString(mOrgName);
    }

    public static final Creator<Employee> CREATOR = new Creator<Employee>() {
        @Override
        public Employee createFromParcel(Parcel source) {
            return new Employee(source);
        }

        @Override
        public Employee[] newArray(int size) {
            return new Employee[size];
        }
    };
}
