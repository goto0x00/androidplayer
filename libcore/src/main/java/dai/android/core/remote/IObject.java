package dai.android.core.remote;

import android.os.BadParcelableException;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Array;

import dai.android.core.debug.logger;

public class IObject implements Parcelable {

    private static final String TAG = IObject.class.getSimpleName();

    private Object mValue = null;  // the instance object of your want put
    private Class<?> mClass = null; // the Object Class of your want put

    public IObject() {
        this(null);
    }

    /**
     * constructor
     *
     * @param object the call param object
     */
    public IObject(Object object) {
        setValue(object);
    }

    protected IObject(Parcel in) {
        readFromParcel(in);
    }

    /**
     * get the call result object instance: maybe from remote process
     *
     * @return
     */
    public Object getValue() {
        return mValue;
    }

    public void setValue(Object object) {
        if (null != object && checkoutObject(object)) {
            mValue = object;
            if (object instanceof Class) {
                mClass = (Class<?>) object;
            } else {
                mClass = object.getClass();
            }
        }
    }

    public static final Creator<IObject> CREATOR = new Creator<IObject>() {
        @Override
        public IObject createFromParcel(Parcel in) {
            return new IObject(in);
        }

        @Override
        public IObject[] newArray(int size) {
            return new IObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (null == mValue) {
            dest.writeString(null);
        } else {
            Class<?> clazz = mClass == null ? mValue.getClass() : mClass;
            dest.writeString(clazz.getName());
            writeValueToParcel(dest, mValue, mClass, flags);
        }
    }

    protected void readFromParcel(Parcel in) {
        String className = in.readString();
        if (!TextUtils.isEmpty(className)) {
            try {
                mClass = Class.forName(className);
                mValue = readValueFromParcel(in, mClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(TAG + " from Parcel:", e);
            }
        } else {
            mValue = null;
            mClass = null;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private static boolean checkoutObject(Object object) {
        return true;
    }

    // CAUTION: should be compatible to Parcel.writeValue/readValue
    //private static final int VAL_CLASSLOADER = -2;
    private static final int TAG_PARCELABLE = -4;
    private static final int TAG_PARCELABLE_ARRAY = -16;
    private static final int TAG_OBJECT_ARRAY = -17;
    private static final int TAG_CLASS = -64;
    private static final int TAG_SERIALIZABLE = 21;

    @SuppressWarnings("unchecked")
    public static void writeValueToParcel(Parcel dest,
                                          Object value,
                                          Class<?> clazz,
                                          int flags) {
        if (null == dest) {
            throw new RuntimeException("writeValueToParcel param 'dest' instance is null");
        }
        if (null == value) {
            throw new RuntimeException("writeValueToParcel param 'value' instance is null");
        }
        if (null == clazz) {
            throw new RuntimeException("writeValueToParcel param 'clazz' instance is null");
        }

        if (value instanceof Parcelable) {
            dest.writeInt(TAG_PARCELABLE);

            // this code must!
            // if not will crash at Parcel.readParcelableCreator
            dest.writeString(clazz.getName());
            ((Parcelable) value).writeToParcel(dest, flags);

        } else if (value instanceof Parcelable[]) {

            // write tag
            dest.writeInt(TAG_PARCELABLE_ARRAY);

            // the real component type of this array
            Class<? extends Parcelable> c =
                    (Class<? extends Parcelable>) value.getClass().getComponentType();

            // write the component type
            dest.writeString(c.getName());

            Parcelable[] tmpValue = (Parcelable[]) value;

            // write the array length
            dest.writeInt(tmpValue.length);

            // write one by one
            for (Parcelable tmp : tmpValue) {
                if (tmp == null) {
                    dest.writeString(null);
                } else {
                    //String className = c.getName();
                    String className = tmp.getClass().getName();
                    dest.writeString(className);
                    tmp.writeToParcel(dest, flags);
                }
            }

        } else if (value instanceof Object[]) {
            dest.writeInt(TAG_OBJECT_ARRAY);
            dest.writeString(value.getClass().getComponentType().getName());

            Object[] tmps = (Object[]) value;
            dest.writeInt(tmps.length);
            for (Object o : tmps) {
                dest.writeValue(o);
            }

        } else if (value instanceof Class) {
            dest.writeInt(TAG_CLASS);
            dest.writeString(((Class<?>) value).getName());

        } else {
            dest.writeValue(value);
        }
    }


    private static Object readValueFromParcel(Parcel source, Class<?> clazz) {
        if (null == source) {
            throw new RuntimeException("readValueFromParcel param 'source' is null");
        }
        if (null == clazz) {
            throw new RuntimeException("readValueFromParcel param 'clazz' is null");
        }

        // read the tag first
        int tag = source.readInt();
        //final ClassLoader loader = clazz.getClassLoader();
        if (tag >= -1) {
            if (tag == TAG_SERIALIZABLE && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return readSerializable(source, clazz);
            }

            // 4: sizeof the int(tag)
            source.setDataPosition(source.dataPosition() - 4);
            ClassLoader loader = clazz.getClassLoader();
            Object value = source.readValue(loader);
            //if (value instanceof Object[]) {
            //    value = copyArray(source.readString(), value, loader);
            //}
            return value;

        } else if (tag == TAG_PARCELABLE) {
            ClassLoader loader = clazz.getClassLoader();
            return source.readParcelable(loader);

        } else if (tag == TAG_PARCELABLE_ARRAY) {
            String componentType = source.readString();
            Class<?> componentClazz = null;
            try {
                componentClazz = Class.forName(componentType);
            } catch (ClassNotFoundException e) {
                logger.w(TAG, "Can not found component type of: " + componentType, e);
                return null;
            }

            Object[] src = source.readParcelableArray(componentClazz.getClassLoader());
            return copyArray(componentClazz, src);

        } else if (tag == TAG_OBJECT_ARRAY) {

            ClassLoader loader = clazz.getClassLoader();

            String type = source.readString();
            return copyArray(type, source.readArray(loader));

        } else if (tag == TAG_CLASS) {
            ClassLoader loader = clazz.getClassLoader();
            String type = source.readString();
            try {
                return Class.forName(type, true, loader);
            } catch (ClassNotFoundException e) {
                logger.e(TAG, "readValueFromParcel", e);
                throw new BadParcelableException("readValueFromParcel: invalid class name " + type);
            }
        } else {
            logger.e(TAG, "readValueFromParcel: invalid tag=" + tag);
            throw new ParcelFormatException("readValueFromParcel: invalid tag=" + tag);
        }
    }

    private static Serializable readSerializable(Parcel source, final Class<?> clazz) {
        byte[] serializedData = source.createByteArray();
        ByteArrayInputStream byteData = new ByteArrayInputStream(serializedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(byteData) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc)
                        throws IOException, ClassNotFoundException {
                    // try the custom classloader if provided
                    if (clazz != null) {
                        Class<?> c = Class.forName(desc.getName(), false, clazz.getClassLoader());
                        if (c != null) {
                            return c;
                        }
                    }
                    return super.resolveClass(desc);
                }
            };
            return (Serializable) ois.readObject();

        } catch (IOException ioe) {
            throw new RuntimeException(
                    "Parcelable encountered IOException reading a Serializable object[ " +
                            clazz.getName() + " ]", ioe);
        } catch (ClassNotFoundException notFound) {
            throw new RuntimeException(
                    "Parcelable encountered ClassNotFoundException reading a Serializable object[:",
                    notFound);
        }
    }

    private static Object[] copyArray(Class<?> clazz, Object value) {
        Object[] bad = (Object[]) value;
        Object[] good = (Object[]) Array.newInstance(clazz, bad.length);
        for (int i = 0; i < bad.length; ++i) {
            good[i] = bad[i];
        }
        return good;
    }

    private static Object[] copyArray(String type, Object value) {
        try {
            Class<?> componentType = Class.forName(type);
            Object[] bad = (Object[]) value;
            Object[] good = (Object[]) Array.newInstance(componentType, bad.length);
            for (int i = 0; i < bad.length; ++i) {
                good[i] = bad[i];
            }
            return good;

        } catch (ClassNotFoundException e) {
            logger.w(TAG, "copyArray not found class:", e);
        } catch (Exception e) {
            logger.w(TAG, "copyArray failed:", e);
        }

        return null;
    }

}
