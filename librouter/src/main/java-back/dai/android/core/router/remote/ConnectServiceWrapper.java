package dai.android.core.router.remote;

public class ConnectServiceWrapper {

    public Class<? extends ConnectRouterLocalService> target = null;

    public ConnectServiceWrapper(Class<? extends ConnectRouterLocalService> clazz) {
        target = clazz;
    }
}
