package dai.android.player.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class Person implements Parcelable, Externalizable {

    private String mName;
    private int mAge;

    public Person() {
    }

    public Person(String name, int age) {
        mName = name;
        mAge = age;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Person[");
        stringBuilder.append(hashCode());
        stringBuilder.append("]\n");
        stringBuilder.append("name: ");
        stringBuilder.append(mName);
        stringBuilder.append("\n");
        stringBuilder.append("age: ");
        stringBuilder.append(mAge);

        return stringBuilder.toString();
    }

    protected Person(Parcel in) {
        readFromParcel(in);
    }

    protected void readFromParcel(Parcel in) {
        mName = in.readString();
        mAge = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mAge);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeChars(mName);
        out.writeInt(mAge);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        mName = in.readUTF();
        mAge = in.readInt();
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            Person person = new Person();
            person.readFromParcel(in);
            return person;
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
