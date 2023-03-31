package lab.galaxy.yahfa.demoApp;

import android.content.Context;

import com.android.flinger.yafya.YafyaMain;


import java.lang.reflect.Method;

public class SystemPropertyPook {


    private static String getProperty_hook(String key, String value) {

        //android.net.Proxy.getHost()
        if (key != null && key.contains("proxyHost")){
            key = "proxyHostppppp";
        }
        //android.net.Proxy.getHost()
        if (key != null && key.contains("proxyPort")){
            key = "proxyHostppppp";
        }
        LogUtils.error("getProperty_hook hooked in:", key+"values:"+key);
        return getProperty_backup(key, value);
    }

    private static String getProperty_backup(String bArr, String ptr1) {

        return null;
    }

    public static void HelloPookSystemProps(Context contextsrc) {

        Class<?> systemcls = System.class;

        Method getProperty = Reflect.getDeclaredMethod(systemcls, "getProperty", String.class,String.class);
        Method getProperty_hook = Reflect.getDeclaredMethod(SystemPropertyPook.class, "getProperty_hook", String.class,String.class);
        Method getProperty_backup = Reflect.getDeclaredMethod(SystemPropertyPook.class, "getProperty_backup", String.class ,String.class);

        LogUtils.error("getProperty", ""+getProperty);
        LogUtils.error("getProperty_hook", ""+getProperty_hook);
        LogUtils.error("getProperty_backup", ""+getProperty_backup);

        String methodSig = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
        YafyaMain.findAndBackupAndHook(systemcls, "getProperty", methodSig, getProperty_hook, getProperty_backup);
        Hook_Log_e.target("HelloPookSystemProps", "end");

    }




    private static String getProperty_hook_1(String key) {

        String tmp =  getProperty_backup_1(key);
        //android.net.Proxy.getHost()
        if (key != null && key.contains("proxyHost")){
            key = "proxyHostppppp";

            return null;
        }
        //android.net.Proxy.getHost()
        if (key != null && key.contains("proxyPort")){
            key = "proxyHostppppp";

            return null;
        }
        LogUtils.error("getProperty_hook hooked in ", key);
       return tmp;
    }

    private static String getProperty_backup_1(String bArr) {

        return null;
    }

    public static void HelloPookSystemProps_1(Context contextsrc) {

        Class<?> systemcls = System.class;

        Method getProperty = Reflect.getDeclaredMethod(systemcls, "getProperty", String.class);
        Method getProperty_hook = Reflect.getDeclaredMethod(SystemPropertyPook.class, "getProperty_hook_1", String.class);
        Method getProperty_backup = Reflect.getDeclaredMethod(SystemPropertyPook.class, "getProperty_backup_1", String.class);

        LogUtils.error("getProperty", ""+getProperty);
        LogUtils.error("getProperty_hook", ""+getProperty_hook);
        LogUtils.error("getProperty_backup", ""+getProperty_backup);

        String methodSig = "(Ljava/lang/String;)Ljava/lang/String;";
        YafyaMain.findAndBackupAndHook(systemcls, "getProperty", methodSig, getProperty_hook, getProperty_backup);
        Hook_Log_e.target("HelloPookSystemProps", "end");

    }

}
