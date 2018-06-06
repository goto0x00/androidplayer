package dai.android.core.router;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dai.android.core.debug.logger;
import dai.android.core.router.action.ActionResult;
import dai.android.core.router.done.Response;
import dai.android.core.router.remote.ConnectRouterLocalService;
import dai.android.core.router.remote.ConnectRouterWideService;
import dai.android.core.router.remote.ConnectServiceWrapper;
import dai.android.core.debug.utility.Process;

import static android.content.Context.BIND_AUTO_CREATE;

public class RouterWide {

    private static final String TAG = RouterWide.class.getSimpleName();

    /**
     * the process name of the wide router
     */
    public static final String PROCESS_NAME = "d_a_router_wide";

    // single mode get this class instance object
    private static RouterWide _thiz = null;

    /**
     * single mode get this class instance object
     *
     * @param context
     * @return
     */
    public static RouterWide Get(RApplication context) {
        if (null == _thiz) {
            synchronized (RouterWide.class) {
                if (null == _thiz) {
                    _thiz = new RouterWide(context);
                }
            }
        }
        return _thiz;
    }

    // ---------------------------------------------------------------------------------------------
    private static HashMap<String, ConnectServiceWrapper> sLocalRouterClasses = new HashMap<>();

    /**
     * register a local router
     *
     * @param processName the process name
     * @param targetClass
     */
    public static void registerLocalRouter(String processName, Class<? extends ConnectRouterLocalService> targetClass) {
        if (null == sLocalRouterClasses) {
            sLocalRouterClasses = new HashMap<>();
        }
        ConnectServiceWrapper connectServiceWrapper = new ConnectServiceWrapper(targetClass);
        sLocalRouterClasses.put(processName, connectServiceWrapper);
    }

    // ---------------------------------------------------------------------------------------------
    private RApplication mContext;

    private HashMap<String, ServiceConnection> mapLocalRouterConnect;
    private HashMap<String, ILocalRouter> mapLocalRouter;

    private boolean mIsStopping = false;


    private RouterWide(RApplication context) {
        mContext = context;

        final int pid = android.os.Process.myPid();
        String processName = Process.getProcessName(context, pid);
        logger.d(TAG, "current process[" + pid + ":" + processName + "]");
        if (!PROCESS_NAME.equals(processName)) {
            throw new RuntimeException("You must initialize the RouterWide in process:" + PROCESS_NAME);
        }

        if (null != sLocalRouterClasses) {
            sLocalRouterClasses.clear();
        }

        mapLocalRouterConnect = new HashMap<>();
        mapLocalRouter = new HashMap<>();
    }

    /**
     * check an gave domain local router has registered
     *
     * @param domain
     * @return
     */
    public boolean hasThisLocalRouteRegistered(final String domain) {
        ConnectServiceWrapper connectServiceWrapper = sLocalRouterClasses.get(domain);
        if (null == connectServiceWrapper) {
            return false;
        }

        Class<? extends ConnectRouterLocalService> clazz = connectServiceWrapper.target;
        return null != clazz;
    }

    /**
     * connect to 'domain' local router
     *
     * @param domain
     * @return
     */
    public boolean connectLocalRouter(final String domain) {
        if (!hasThisLocalRouteRegistered(domain)) {
            logger.d(TAG, "not registered: " + domain);
            return false;
        }

        if (null == mapLocalRouter) {
            logger.d(TAG, "store local router map is null");
            return false;
        }

        if (null == mapLocalRouterConnect) {
            logger.d(TAG, "store local router connect map is null");
            return false;
        }

        Class clazz = sLocalRouterClasses.get(domain).target;
        Intent binderIntent = new Intent(mContext, clazz);
        Bundle bundle = new Bundle();
        binderIntent.putExtras(bundle);
        final ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                logger.d(TAG, "try to connect: " + domain);
                ILocalRouter tmp = mapLocalRouter.get(domain);
                if (null == tmp) {
                    ILocalRouter localRouter = ILocalRouter.Stub.asInterface(service);
                    mapLocalRouter.put(domain, localRouter);
                    mapLocalRouterConnect.put(domain, this);

                    try {
                        localRouter.connectWideRouter();
                    } catch (RemoteException e) {
                        //e.printStackTrace();
                        logger.d(TAG, "connect to domain:" + domain + " router failed", e);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                logger.d(TAG, "disconnect: " + domain);
                mapLocalRouterConnect.remove(domain);
                mapLocalRouter.remove(domain);
            }
        };
        mContext.bindService(binderIntent, connection, BIND_AUTO_CREATE);

        return true;
    }

    /**
     * disconnect 'domain' local router
     *
     * @param domain
     * @return
     */
    public boolean disconnectLocalRouter(String domain) {
        if (TextUtils.isEmpty(domain)) {
            return false;
        } else if (PROCESS_NAME.equals(domain)) {
            stopSelf();
            return true;
        } else if (null == mapLocalRouterConnect.get(domain)) {
            return false;
        } else {
            logger.d(TAG, "try to disconnect: " + domain);
            ILocalRouter router = mapLocalRouter.get(domain);
            if (null != router) {
                try {
                    router.stopWideRouter();
                } catch (RemoteException e) {
                    //e.printStackTrace();
                    logger.d(TAG, "stop wide router failed", e);
                }
            }
            mContext.unbindService(mapLocalRouterConnect.get(domain));
            mapLocalRouterConnect.remove(domain);
            mapLocalRouter.remove(domain);
            return true;
        }
    }

    /**
     * stop the wide router process
     */
    public void stopSelf() {
        mIsStopping = true;
        Thread stopThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (null == mapLocalRouter || mapLocalRouter.isEmpty()) {
                    logger.d(TAG, "no local router store at map");
                    return;
                }

                List<String> domains = new ArrayList<>();
                domains.addAll(mapLocalRouter.keySet());
                for (String domain : domains) {
                    logger.d(TAG, "try to stop: " + domain);
                    ILocalRouter router = mapLocalRouter.get(domain);
                    if (null != router) {
                        try {
                            router.stopWideRouter();
                        } catch (RemoteException e) {
                            //e.printStackTrace();
                            logger.d(TAG, "stop wide router:" + domain + " failed.", e);
                        }
                    }
                    mContext.unbindService(mapLocalRouterConnect.get(domain));
                    mapLocalRouterConnect.remove(domain);
                    mapLocalRouter.remove(domain);
                }

                try {
                    Thread.sleep(1000);
                    mContext.stopService(new Intent(mContext, ConnectRouterWideService.class));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }, "WideRouterStopThread");
        stopThread.start();
    }


    /**
     * tell the caller this request is async
     *
     * @param domain
     * @param strRequest
     * @return
     */
    public boolean answerLocalAsync(String domain, String strRequest) {
        if (null == mapLocalRouter || mapLocalRouter.isEmpty()) {
            logger.d(TAG, "answerLocalAsync: null or empty mapLocalRouter");
            return false;
        }

        ILocalRouter router = mapLocalRouter.get(domain);
        if (null == router) {
            return hasThisLocalRouteRegistered(domain);
        } else {
            try {
                return router.checkResponseAsync(strRequest);
            } catch (RemoteException e) {
                //e.printStackTrace();
                logger.d(TAG, "check response async failed", e);
                return true;
            }
        }
    }

    public Response route(String domain, String strRequest) {
        Response response = new Response();

        if (mIsStopping) {
            ActionResult result = new ActionResult.Builder()
                    .code(ActionResult.CODE_WIDE_STOPPING)
                    .message("wide router is stopping")
                    .build();
            response.setAync(true);
            response.setResultString(result.toString());
            return response;
        }

        if (PROCESS_NAME.equals(domain)) {
            ActionResult result = new ActionResult.Builder()
                    .code(ActionResult.CODE_TARGET_IS_WIDE)
                    .message("domain can not be: " + PROCESS_NAME)
                    .build();
            response.setAync(true);
            response.setResultString(result.toString());
            return response;
        }

        ILocalRouter router = mapLocalRouter.get(domain);
        if (null == router) {
            if (!connectLocalRouter(domain)) {
                ActionResult result = new ActionResult.Builder()
                        .code(ActionResult.CODE_ROUTER_NOT_REGISTER)
                        .message("not register domain : " + domain)
                        .build();
                response.setAync(false);
                response.setResultString(result.toString());
                return response;
            }
        } else {
            int tryTimes = 0;
            while (true) {
                router = mapLocalRouter.get(domain);
                if (null == router) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    tryTimes++;
                } else {
                    // the domain router registered
                    break;
                }

                if (tryTimes >= 600) {
                    ActionResult result = new ActionResult.Builder()
                            .code(ActionResult.CODE_CANNOT_BIND_LOCAL)
                            .message("can not bind: " + domain + ", is timeout")
                            .build();
                    response.setResultString(result.toString());
                    return response;
                }
            }
        }

        try {
            String strResult = router.route(strRequest);
            response.setResultString(strResult);
        } catch (RemoteException e) {
            ActionResult result = new ActionResult.Builder()
                    .code(ActionResult.CODE_REMOTE_EXCEPTION)
                    .message(e.getMessage())
                    .build();
            response.setResultString(result.toString());
            return response;
        }

        return response;
    }

}
