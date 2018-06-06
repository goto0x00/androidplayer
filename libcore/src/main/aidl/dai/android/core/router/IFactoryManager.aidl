
// IFactoryManager.aidl
package dai.android.core.router;

interface IFactoryManager {

    boolean closeFactory( String factory );

    boolean registerFactory( String factory );

    void syncTask( String factory );
}
