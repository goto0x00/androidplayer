package dai.android.core.router.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import dai.android.core.debug.logger;
import dai.android.core.router.ILocalRouter;
import dai.android.core.router.RApplication;
import dai.android.core.router.RouterLocal;
import dai.android.core.router.action.ActionResult;
import dai.android.core.router.done.Request;
import dai.android.core.router.done.Response;


public class ConnectRouterLocalService extends Service {

    private static final String TAG = "ConnectRouterLocalSvc";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.d(TAG, "ConnectRouterLocalService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        logger.d(TAG, "ConnectRouterLocalService onBind");
        return mStub;
    }

    ILocalRouter.Stub mStub = new ILocalRouter.Stub() {
        @Override
        public boolean checkResponseAsync(String request) throws RemoteException {
            Request myRequest = Request.obtain(getApplicationContext());
            myRequest.json(request);

            RouterLocal routerLocal = RouterLocal.Get(RApplication.Get());
            boolean result = false;
            try {
                result = routerLocal.answerWiderAsync(myRequest);
            } catch (Exception e) {
                throw new RemoteException(e.getMessage());
            }
            return result;
        }

        @Override
        public String route(String request) throws RemoteException {
            Request myRequest = Request.obtain(getApplicationContext());
            myRequest.json(request);

            try {
                Response response = RouterLocal.Get(RApplication.Get())
                        .route(ConnectRouterLocalService.this, myRequest);
                return response.get();
            } catch (Exception e) {
                //e.printStackTrace();
                logger.d(TAG, "call RouterLocal.route failed.", e);
                return new ActionResult.Builder().message(e.toString()).build().toString();
            }
        }

        @Override
        public void connectWideRouter() throws RemoteException {
            RouterLocal.Get(RApplication.Get()).connectWideRouter();
        }

        @Override
        public boolean stopWideRouter() throws RemoteException {
            RouterLocal.Get(RApplication.Get()).disconnectWideRouter();
            return true;
        }
    };
}
