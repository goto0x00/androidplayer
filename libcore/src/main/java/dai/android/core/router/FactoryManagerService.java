package dai.android.core.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import dai.android.core.BaseApplication;
import dai.android.core.debug.logger;
import dai.android.core.remote.TokenInfo;

class FactoryManagerService extends Service {

    private static final String TAG = FactoryManagerService.class.getSimpleName();

    protected FactoryManager mFactoryManager = null;

    public FactoryManagerService() {
    }

    @Override
    public void onCreate() {
        logger.d(TAG, "onCreate");

        mFactoryManager = FactoryManager.Get(BaseApplication.GetThis());

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.d(TAG, "onBind");

        if (null != intent && null != intent.getExtras()) {
            TokenInfo info = TokenInfo.parse(intent.getExtras());
            if (null != info) {
                onClientArrive(info);
            }
        }

        if (null != mFactoryManager) {
            return mFactoryManager;
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.d(TAG, "onStartCommand");

        if (null != intent && null != intent.getExtras()) {
            TokenInfo info = TokenInfo.parse(intent.getExtras());
            if (null != info) {
                onClientArrive(info);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        logger.d(TAG, "onDestroy");

        super.onDestroy();

        mFactoryManager.mClients.clear();
    }

    private void onClientArrive(final TokenInfo info) {
        logger.d(TAG, "onClientArrive: TokenInfo:" + info);

        Client client = mFactoryManager.mClients.get(info);
        if (null != client) {
            if (client.token.binder != info.binder) {
                onClientLost(info);
            } else {
                logger.d(TAG, "this TokenInfo has exist.");
                return;
            }
        }

        if (null != info.binder) {
            client = new Client(info) {
                @Override
                public void binderDied() {
                    onClientLost(info);
                }
            };

            try {
                info.binder.linkToDeath(client, 0);
            } catch (RemoteException e) {
                // do nothing
            }
        } else {
            client = msClient;
        }

        mFactoryManager.mClients.put(info, client);
    }

    private void onClientLost(TokenInfo info) {
        logger.d(TAG, "onClientLost: TokenInfo:" + info);
        Client client = mFactoryManager.mClients.remove(info);
        if (null != client) {
            if (client != msClient) {
                msClient.token.binder.unlinkToDeath(client, 0);
            }
        }
    }


    abstract static class Client implements IBinder.DeathRecipient {
        TokenInfo token;

        Client(TokenInfo info) {
            token = info;
        }
    }

    private static Client msClient = new Client(null) {
        @Override
        public void binderDied() {
        }
    };
}
