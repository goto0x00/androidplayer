
package dai.android.virtual;

import android.content.Context;


/**
 * This is God class, you should not know where it's from and any details.
 */
public class Systems {

    static Context sHostContext;

    /**
     * get a Context object anywhere you want.
     *
     * @return a Context object
     */
    public static Context getContext() {
        return sHostContext;
    }

}
