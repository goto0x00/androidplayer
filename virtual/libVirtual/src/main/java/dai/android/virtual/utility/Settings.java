
package dai.android.virtual.utility;

import android.content.Context;
import android.content.SharedPreferences;


public class Settings {

    private static final String FILE_NAME = "dai.android.virtual.settings";

    public static void setSoVersion(Context context, String name, int version) {
        SharedPreferences preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(name, version);
        editor.apply();
    }

    public static int getSoVersion(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(name, 0);
    }

}
