package dai.android.core.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dai.android.core.debug.logger;
import dai.android.core.router.action.Action;
import dai.android.core.router.action.ActionResult;
import dai.android.core.router.action.BadAction;
import dai.android.core.router.done.Request;
import dai.android.core.router.done.Response;
import dai.android.core.router.provider.Provider;
import dai.android.core.router.remote.ConnectRouterLocalService;
import dai.android.core.router.remote.ConnectRouterWideService;
import dai.android.core.router.remote.Info;
import dai.android.core.debug.utility.Process;

import static android.content.Context.BIND_AUTO_CREATE;

public class RouterLocal {
    private static final String TAG = RouterLocal.class.getSimpleName();

    // single mode get this class instance
    private static RouterLocal _thiz = null;

    public static RouterLocal Get(@NonNull RApplication context) {
        if (null == _thiz) {
            synchronized (RouterLocal.class) {
                if (null == _thiz) {
                    _thiz = new RouterLocal(context);
                }
            }
        }
        return _thiz;
    }


    // define and init a thread pool
    private static ExecutorService _threadPool = null;

    private static synchronized ExecutorService getThreadPool() {
        if (null == _threadPool) {
            _threadPool = Executors.newCachedThreadPool();
        }
        return _threadPool;
    }

    private String mProcessName = Process.UNKNOWN_PROCESS_NAME;
    private Map<String, Provider> mProviders = new HashMap<>();
    private RApplication mContext;
    private IWideRouter mWideRouter;

    private ServiceConnection mSvcConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mWideRouter = IWideRouter.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWideRouter = null;
        }
    };


    private RouterLocal(RApplication context) {
        mContext = context;
        mProcessName = Process.getProcessName(context, android.os.Process.myPid());
        if (context.multipleProcess() && !RouterWide.PROCESS_NAME.equals(mProcessName)) {
            connectWideRouter();
        }
    }

    /**
     * if this application run at multiple process
     * different module run at different process
     * connect the wide router first
     */
    public void connectWideRouter() {
        Intent intent = new Intent(mContext, ConnectRouterWideService.class);
        intent.putExtra(Info.DOMAIN, mProcessName);
        mContext.bindService(intent, mSvcConnection, BIND_AUTO_CREATE);
    }


    /**
     * if this application run at multiple process
     * different module run at different process
     * disconnect connected wide router after used
     */
    public void disconnectWideRouter() {
        if (null == mSvcConnection) return;

        mContext.unbindService(mSvcConnection);
        mWideRouter = null;
    }

    /**
     * register a provider with name
     *
     * @param name     the name of the provider
     * @param provider the provider object instance
     */
    public void registerProvider(String name, Provider provider) {
        if (null == mProviders) return;

        mProviders.put(name, provider);
    }

    /**
     * the wide router connection is okay
     * true the connect is good
     * false connect is bad
     *
     * @return true/false
     */
    public boolean isWideRouterConnectOkay() {
        return null != mWideRouter;
    }

    /**
     * answer this call wide need async or not
     *
     * @param request
     * @return
     * @throws Exception
     */
    public boolean answerWiderAsync(@NonNull Request request) throws Exception {
        if (mProcessName.equals(request.domain()) && isWideRouterConnectOkay()) {
            Action targetAction = findRequestAction(request);
            if (null == targetAction || targetAction instanceof BadAction) {
                throw new Exception("No action about request:\n" + request);
            }
            return targetAction.isThisAsync(mContext, request.datas());
        } else {
            return true;
        }
    }

    /**
     * stop service of self
     *
     * @param clazz the class
     * @return
     */
    public boolean stopSelf(Class<? extends ConnectRouterLocalService> clazz) {
        if (isWideRouterConnectOkay()) {
            try {
                return mWideRouter.stopRouter(mProcessName);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            mContext.stopService(new Intent(mContext, clazz));
            return true;
        }
    }

    /**
     * stop the wide router
     */
    public void stopWideRouter() {
        if (isWideRouterConnectOkay()) {
            try {
                mWideRouter.stopRouter(RouterWide.PROCESS_NAME);
            } catch (RemoteException e) {
                logger.e(TAG, "stop wide route failed.", e);
                e.printStackTrace();
            }
        }
    }

    /**
     * the core function route
     *
     * @param context Context
     * @param request the request object instance
     * @return
     * @throws Exception
     */
    public Response route(Context context, @NonNull Request request) throws Exception {
        Response response = new Response();

        if (null == request || null == context) {
            throw new Exception("the input param is null");
        }

        if (mProcessName.equals(request.domain())) {
            HashMap<String, String> params = new HashMap<>();
            Object attachmentObject = request.object();
            params.putAll(request.datas());

            Action targetAction = findRequestAction(request);
            if (targetAction instanceof BadAction) {
                logger.d(TAG, "can not get the right action");
                return response;
            }
            request.idle().set(true);

            response.setAync(null == attachmentObject ?
                    targetAction.isThisAsync(context, params) :
                    targetAction.isThisAsync(context, params, attachmentObject)
            );

            if (!response.isAsync()) {
                ActionResult result = null == attachmentObject ?
                        targetAction.invoke(context, params) :
                        targetAction.invoke(context, params, attachmentObject);
            } else {
                TaskLocal task = new TaskLocal(
                        context,
                        targetAction,
                        params,
                        attachmentObject,
                        response);
                response.asyncResponse(getThreadPool().submit(task));

            }
        } else if (!mContext.multipleProcess()) {
            throw new Exception("");
        }
        // this IPC call
        else {
            String domain = request.domain();
            String strRequest = request.toString();
            request.idle().set(true);

            // connect remote router
            if (isWideRouterConnectOkay()) {
                response.setAync(mWideRouter.checkResponseAsync(domain, strRequest));
            } else {
                response.setAync(true);
                //Response response, String domain, String request
                TaskConnectWide task = new TaskConnectWide(response, domain, strRequest);
                response.asyncResponse(getThreadPool().submit(task));
                return response;
            }

            // do remote route
            if (!response.isAsync()) {
                response.setResultString(mWideRouter.route(domain, strRequest));
            } else {
                TaskWide task = new TaskWide(domain, strRequest);
                response.asyncResponse(getThreadPool().submit(task));
            }
        }

        return response;
    }


    //------------------------------------------------------------------
    private Action findRequestAction(Request request) {
        BadAction badAction = null;
        if (null == request) {
            badAction = new BadAction(
                    false,
                    ActionResult.CODE_NOT_FOUND,
                    "not found the request action, null request"
            );
        } else {
            badAction = new BadAction(
                    false,
                    ActionResult.CODE_NOT_FOUND,
                    "not found the request action @request:\n" + request.toString()
            );
        }

        if (null == mProcessName || mProviders.isEmpty()) {
            logger.i(TAG, "null or empty provider store");
            return badAction;
        }

        Provider targetProvider = mProviders.get(request.provider());
        if (null == targetProvider) {
            logger.i(TAG, "can not find provider:" + request.provider());
            return badAction;
        }

        Action target = targetProvider.find(request.action());
        if (null == target) {
            return badAction;
        }
        return target;
    }

    //
    private class TaskLocal implements Callable<String> {
        private Context mContext;
        private Action mAction;
        private Object mObject;
        private Response mResponse;
        private HashMap<String, String> mReqDatas;

        public TaskLocal(Context context,
                         Action action,
                         HashMap<String, String> requestData,
                         Object object,
                         Response response) {
            mContext = context;
            mAction = action;
            mObject = object;
            mReqDatas = requestData;
            mResponse = response;
        }

        @Override
        public String call() throws Exception {
            ActionResult result = mObject == null ?
                    mAction.invoke(mContext, mReqDatas) :
                    mAction.invoke(mContext, mReqDatas, mObject);
            return result.toString();
        }
    }


    private class TaskWide implements Callable<String> {

        private String mDomain;
        private String mRequestString;

        public TaskWide(String domain, String strRequest) {
            mDomain = domain;
            mRequestString = strRequest;
        }

        @Override
        public String call() throws Exception {
            String result = mWideRouter.route(mDomain, mRequestString);
            return result;
        }
    }

    private class TaskConnectWide implements Callable<String> {
        private Response mResponse;
        private String mDomain;
        private String mRequestString;

        public TaskConnectWide(Response response, String domain, String request) {
            mResponse = response;
            mDomain = domain;
            mRequestString = request;
        }

        @Override
        public String call() throws Exception {
            connectWideRouter();

            int time = 0;
            while (true) {
                if (null == mWideRouter) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        logger.d(TAG, "thread sleep failed", e);
                    }
                    time++;
                } else {
                    break;
                }
                if (time >= 600) {
                    BadAction defaultNotFoundAction = new BadAction(
                            true,
                            ActionResult.CODE_CANNOT_BIND_WIDE,
                            "bind wide router time out. can not bind wide router."
                    );
                    ActionResult result = defaultNotFoundAction.invoke(mContext, new HashMap<String, String>());
                    mResponse.setResultString(result.toString());
                    return result.toString();
                }
            }

            String result = mWideRouter.route(mDomain, mRequestString);
            return result;
        }
    }


}
