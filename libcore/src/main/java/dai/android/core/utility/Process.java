package dai.android.core.utility;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class Process {

    public static final String UNKNOWN_PROCESS_NAME =
            "75679f4e06-698ac94669-e4591d8771-24780640dd";

    public static String getProcessName(int pid) {
        String processName = UNKNOWN_PROCESS_NAME;
        try {
            File file = new File("/proc/" + pid + "/" + "cmdline");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            processName = bufferedReader.readLine().trim();
            bufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (TextUtils.isEmpty(processName)) {
                processName = UNKNOWN_PROCESS_NAME;
            }
        }
        return processName;
    }

    public static String getProcessName(Context context, int pid) {
        String processName = getProcessName(pid);
        if (UNKNOWN_PROCESS_NAME.equals(processName)) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (null == am) {
                return UNKNOWN_PROCESS_NAME;
            }
            List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
            if (runningApps == null) {
                return UNKNOWN_PROCESS_NAME;
            }
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        } else {
            return processName;
        }
        return UNKNOWN_PROCESS_NAME;
    }

    public static int getpid(Context context, String name) {
        ActivityManager manager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.processName.equals(name)) {
                return process.pid;
            }
        }
        return -1;
    }

    public static int getPid() {
        return android.os.Process.myPid();
    }

    public static int getUid() {
        return android.os.Process.myUid();
    }

    public static int getTid() {
        return android.os.Process.myTid();
    }

}
