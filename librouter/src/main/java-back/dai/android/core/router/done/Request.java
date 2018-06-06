package dai.android.core.router.done;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dai.android.core.router.remote.Info;
import dai.android.core.debug.utility.Process;

public class Request {
    private static final String TAG = "routerRequest";

    private String _from;
    private String _domain;
    private String _provider;
    private String _action;
    private Object _object;
    private HashMap<String, String> _datas;

    private AtomicBoolean _isIdle = new AtomicBoolean(true);

    /**
     * this request is idle
     *
     * @return AtomicBoolean object instance
     */
    public AtomicBoolean idle() {
        return _isIdle;
    }

    /**
     * get the from string
     *
     * @return from string
     */
    public String from() {
        return _from;
    }

    /**
     * get the domain string
     *
     * @return domain string
     */
    public String domain() {
        return _domain;
    }

    /**
     * get the provider string
     *
     * @return provider string
     */
    public String provider() {
        return _provider;
    }

    /**
     * set the provider name
     *
     * @param provider the provider name
     * @return this object
     */
    public Request provider(String provider) {
        _provider = provider;
        return this;
    }

    /**
     * get action
     *
     * @return action string
     */
    public String action() {
        return _action;
    }

    /**
     * set the action
     *
     * @param action action string
     * @return this object
     */
    public Request action(String action) {
        _action = action;
        return this;
    }

    /**
     * get the request object instance and clean
     *
     * @return request Object instance
     */
    public Object object() {
        Object tmp = _object;
        _object = null;
        return tmp;
    }

    /**
     * set the request Object instance
     *
     * @param object a object instance
     * @return this request object
     */
    public Request object(Object object) {
        _object = object;
        return this;
    }

    /**
     * get the map of data
     *
     * @return HashMap
     */
    public HashMap<String, String> datas() {
        return _datas;
    }

    /**
     * set key and value HashMap
     *
     * @param key   a string key
     * @param value a string value
     * @return this request object instance
     */
    public Request data(String key, String value) {
        if (null != _datas) {
            _datas.put(key, value);
        }
        return this;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Info.FROM, _from);
            jsonObject.put(Info.DOMAIN, _domain);
            jsonObject.put(Info.PROVIDER, _provider);
            jsonObject.put(Info.ACTION, _action);

            try {
                JSONObject jsonData = new JSONObject();
                for (Map.Entry<String, String> entry : _datas.entrySet()) {
                    jsonData.put(entry.getKey(), entry.getValue());
                }
                jsonObject.put(Info.DATA, jsonData);
            } catch (Exception e) {
                jsonObject.put(Info.DATA, "{}");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * set a json request, the json string like:
     * // {
     * //     from: xxx
     * //     domain: xxx,
     * //     provider: xxx,
     * //     action: xxx,
     * //     data {
     * //         data1: xxx,
     * //         data2: xxx
     * //      }
     * // }
     *
     * @param requestJson a json request string
     * @return request object instance
     */
    public Request json(String requestJson) {
        try {
            JSONObject jsonObject = new JSONObject(requestJson);
            _from = jsonObject.getString(Info.FROM);
            _domain = jsonObject.getString(Info.DOMAIN);
            _provider = jsonObject.getString(Info.PROVIDER);
            _action = jsonObject.getString(Info.ACTION);
            try {
                JSONObject jsonData = new JSONObject(jsonObject.getString(Info.DATA));
                Iterator it = jsonData.keys();
                while (it.hasNext()) {
                    String key = String.valueOf(it.next());
                    String value = (String) jsonData.get(key);
                    _datas.put(key, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
                _datas = new HashMap<>();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * the url string encode type
     * TYPE_ENCODE_NONE: not encode any string
     * TYPE_ENCODE_DATA_VALUE:encode data value
     * TYPE_ENCODE_ALL: encode all data
     */
    public static final int TYPE_ENCODE_NONE = 0;
    public static final int TYPE_ENCODE_DATA_VALUE = 1;
    public static final int TYPE_ENCODE_ALL = 2;

    /**
     * get a router request instance use URL string, eg:
     * xxDomain/xxProvider/xxAction?data1=xxx&data2=xxx
     *
     * @param url    the url string
     * @param encode encode type,must one of follow value:
     *               * TYPE_ENCODE_NONE: not encode any string
     *               * TYPE_ENCODE_DATA_VALUE:encode data value
     *               * TYPE_ENCODE_ALL: encode all data, and this default
     * @return this request object instance
     */
    public Request url(String url, int encode) {
        if (!TextUtils.isEmpty(url)) {
            String[] urlParts = url.split("\\?");
            // this url string exist only one or no '?'
            if (1 == urlParts.length || 2 == urlParts.length) {
                // deal the left part of '?'
                boolean next = false;
                // deal the url main data: xxDomain/xxProvider/xxAction
                String[] targets = urlParts[0].split("/");
                if (3 == targets.length) {
                    switch (encode) {
                        case TYPE_ENCODE_NONE:
                        case TYPE_ENCODE_DATA_VALUE: {
                            _domain = targets[0];
                            _provider = targets[1];
                            _action = targets[2];
                            next = true;
                            break;
                        }
                        case TYPE_ENCODE_ALL:
                        default: {
                            try {
                                _domain = URLDecoder.decode(targets[0], "utf-8");
                                _provider = URLDecoder.decode(targets[1], "utf-8");
                                _action = URLDecoder.decode(targets[2], "utf-8");
                                next = true;
                            } catch (UnsupportedEncodingException e) {
                                next = false;
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }

                // deal the right part of '?'
                if (next && urlParts.length == 2) {
                    String[] arrayData = urlParts[1].split("&");
                    if (arrayData.length > 0 && null == _datas) {
                        _datas = new HashMap<>(arrayData.length);
                    }
                    for (String DATA : arrayData) {
                        String kv[] = DATA.split("=");
                        String k = kv[0];
                        String v = kv.length == 1 ? "" : kv[1];
                        try {
                            switch (encode) {
                                case TYPE_ENCODE_DATA_VALUE: {
                                    v = URLDecoder.decode(v, "utf-8");
                                    break;
                                }
                                case TYPE_ENCODE_ALL:
                                default: {
                                    k = URLDecoder.decode(k, "utf-8");
                                    v = URLDecoder.decode(v, "utf-8");
                                    break;
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                            continue;
                        }
                        _datas.put(k, v);
                    }
                }
            }
        }

        return this;
    }

    /**
     * get a router request instance use URL string, eg:
     * xxDomain/xxProvider/xxAction?data1=xxx&data2=xxx
     * But this url only encode data.value part
     *
     * @param url a url only data.value part encode
     * @return this request object instance
     */
    public Request url(String url) {
        return url(url, TYPE_ENCODE_DATA_VALUE);
    }


    private static final int ARRAY_LENGTH = 64;
    private static final int RESET_NUMBER = 1000;

    private static AtomicInteger sIndex = new AtomicInteger(0);
    private static volatile Request[] sTables = new Request[ARRAY_LENGTH];

    static {
        for (int I = 0; I < ARRAY_LENGTH; ++I) {
            sTables[I] = new Request();
        }
    }

    public static Request obtain(Context context) {
        return obtain(context, 0);
    }

    private static Request obtain(Context context, int retryTime) {
        int index = sIndex.getAndIncrement();
        if (index > RESET_NUMBER) {
            sIndex.compareAndSet(index, 0);
            if (index > RESET_NUMBER * 2) {
                sIndex.set(0);
            }
        }

        int num = index & (ARRAY_LENGTH - 1);
        Request target = sTables[num];
        if (target._isIdle.compareAndSet(true, false)) {
            target._from = getProcess(context);
            target._domain = getProcess(context);
            target._provider = "";
            target._action = "";
            if (null != target._datas) {
                target._datas.clear();
            }
        } else {
            if (retryTime < 5) {
                return obtain(context, retryTime++);
            } else {
                return new Request(context);
            }
        }
        return target;
    }


    private static volatile String DEF_PROCESS = "";

    private static String getProcess(Context context) {
        if (TextUtils.isEmpty(DEF_PROCESS) || Process.UNKNOWN_PROCESS_NAME.equals(DEF_PROCESS)) {
            DEF_PROCESS = Process.getProcessName(context, android.os.Process.myPid());
        }
        return DEF_PROCESS;
    }

    private Request() {
        _from = DEF_PROCESS;
        _domain = DEF_PROCESS;
        _provider = "";
        _action = "";
        _datas = new HashMap<>();
    }

    private Request(Context context) {
        _from = getProcess(context);
        _domain = getProcess(context);
        _provider = "";
        _action = "";
        _datas = new HashMap<>();
    }


}
