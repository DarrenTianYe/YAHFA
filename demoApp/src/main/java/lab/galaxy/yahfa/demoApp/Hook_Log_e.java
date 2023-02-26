package lab.galaxy.yahfa.demoApp;

import android.util.Log;

public class Hook_Log_e {
    public static String className = "android.util.Log";

    public static String classNameDD = "lab.galaxy.yahfa.demoApp.Hook_Log_e";

    public static String methodName = "e";
    public static String methodSig = "(Ljava/lang/String;Ljava/lang/String;)I";

    public static int hook(String tag, String msg) {
        Log.e("HookTest", "hook====" + tag + ", " + msg);
        return backup(tag,msg);
    }

    public static int target(String tag, String msg) {
        Log.e("HookTest", "!!!!!!target!!!!!!!!!!!!: " + tag + ", " + msg);
        return 1;
    }

    public static int backup(String tag, String msg) {
        Log.e("HookTest", "backup should not be here");
        return 1;
    }
}
