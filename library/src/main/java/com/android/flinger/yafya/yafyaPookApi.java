package com.android.flinger.yafya;

import static com.android.flinger.yafya.YafyaMain.findMethodNative;
import static com.android.flinger.yafya.YafyaMain.getThread;
import static com.android.flinger.yafya.YafyaMain.shouldVisiblyInit;
import static com.android.flinger.yafya.YafyaMain.visiblyInit;

import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class yafyaPookApi {


    private static boolean f149;
    private static boolean f148;
    static Map<Method, C0041> f150 = new ConcurrentHashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.lerist.lib.lhooker.LHooker$֏ */
    /* loaded from: classes.dex */
    public static class C0041 {

        /* renamed from: ֏ */
        String f151;

        /* renamed from: ؠ */
        Member f152;

        /* renamed from: ހ */
        Method f153;

        /* renamed from: ށ */
        Method f154;

        public C0041(String str, Member member, Method method, Method method2) {
            this.f151 = str;
            this.f152 = member;
            this.f153 = method;
            this.f154 = method2;
        }
    }

    public static void hook(String str, ClassLoader classLoader) {
        f247 = str;
        hookLocation();
        hookVMOS51Basestation();
    }

    private static void hookBasestation() {
        try {
            yafyaPookApi.m131(TelephonyManager.class, "getNetworkOperatorName", String.class, null, C0087.class, "getNetworkOperatorName");
            yafyaPookApi.m131(TelephonyManager.class, "getSimOperatorName", String.class, null, C0087.class, "getSimOperatorName");
            yafyaPookApi.m131(TelephonyManager.class, "getSimOperator", String.class, null, C0087.class, "getSimOperator");
            yafyaPookApi.m131(TelephonyManager.class, "getNetworkOperator", String.class, null, C0087.class, "getNetworkOperator");
            yafyaPookApi.m131(TelephonyManager.class, "getSimCountryIso", String.class, null, C0087.class, "getSimCountryIso");
            yafyaPookApi.m131(TelephonyManager.class, "getNetworkCountryIso", String.class, null, C0087.class, "getNetworkCountryIso");
            yafyaPookApi.m131(TelephonyManager.class, "getNeighboringCellInfo", List.class, null, C0087.class, "getNeighboringCellInfo");
            int i = Build.VERSION.SDK_INT;
            if (i > 16) {
                yafyaPookApi.m131(TelephonyManager.class, "getAllCellInfo", List.class, null, C0087.class, "getAllCellInfo");
                if (i >= 30) {
                    yafyaPookApi.m131(TelephonyManager.class, "getAllCellInfo", List.class, new Class[]{String.class, String.class}, C0087.class, "getAllCellInfo");
                }
                yafyaPookApi.m131(PhoneStateListener.class, "onCellInfoChanged", Void.TYPE, new Class[]{List.class}, C0087.class, "onCellInfoChanged");
            }
            yafyaPookApi.m131(TelephonyManager.class, "getCellLocation", CellLocation.class, null, C0087.class, "getCellLocation");
            yafyaPookApi.m131(PhoneStateListener.class, "onCellLocationChanged", Void.TYPE, new Class[]{CellLocation.class}, C0087.class, "onCellLocationChanged");
            Class cls = Integer.TYPE;
            yafyaPookApi.m131(TelephonyManager.class, "getNetworkType", cls, null, C0087.class, "getNetworkType");
            if (i > 23) {
                yafyaPookApi.m131(TelephonyManager.class, "getDataNetworkType", cls, null, C0087.class, "getDataNetworkType");
            }
            yafyaPookApi.m131(TelephonyManager.class, "getPhoneType", cls, null, C0087.class, "getPhoneType");
            yafyaPookApi.m131(TelephonyManager.class, "getCurrentPhoneType", cls, null, C0087.class, "getCurrentPhoneType");
        } catch (Throwable th) {
            th.printStackTrace();
            log(th.getMessage());
        }
    }

    public static void m131(Class cls, String str, Class cls2, Class[] clsArr, Class cls3, String str2) {
        m129(cls, str, cls2, clsArr, cls3, str2, str2 + "_bak", str2 + "_copy");
    }

    public static void m129(Class cls, String str, Class cls2, Class[] clsArr, Class cls3, String str2, String str3, String str4) {
        Method[] methods;
        Method method = null;
        Method method2 = null;
        Method method3 = null;
        for (Method method4 : cls3.getMethods()) {
            if (method4.getName().equals(str2)) {
                method = method4;
            }
            if (method4.getName().equals(str3)) {
                method2 = method4;
            }
            if (method4.getName().equals(str4)) {
                method3 = method4;
            }
        }
        try {
            m128(cls, str, cls2, clsArr, method, method2, method3);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void m133(Class cls, String str, String str2, Method method, Method method2, Method method3) {
        m138(m127(cls, str, str2), method, method2, method3);
    }

    private static void m134(Object obj, Method method, String str, String str2) {
        ArrayList arrayList;
        boolean z = obj instanceof Method;
        if (z) {
            arrayList = new ArrayList(Arrays.asList(((Method) obj).getParameterTypes()));
        } else if (obj instanceof Constructor) {
            arrayList = new ArrayList(Arrays.asList(((Constructor) obj).getParameterTypes()));
        } else {
            return;
        }
        ArrayList arrayList2 = new ArrayList(Arrays.asList(method.getParameterTypes()));
        if ((z && !Modifier.isStatic(((Method) obj).getModifiers())) || (obj instanceof Constructor)) {
            arrayList.add(0, Object.class);
        }
        if (!Modifier.isStatic(method.getModifiers())) {
            arrayList2.add(0, Object.class);
        }
        if (z && !method.getReturnType().isAssignableFrom(((Method) obj).getReturnType())) {
            return;
        }
        if ((!(obj instanceof Constructor) || !method.getReturnType().equals(Void.class)) && arrayList.size() == arrayList2.size()) {
            for (int i = 0; i < arrayList.size() && ((Class) arrayList2.get(i)).isAssignableFrom((Class) arrayList.get(i)); i++) {
            }
        }
    }

    public static void m138(Member member, Method method, Method method2, Method method3) {
        if (member == null) {
            throw new IllegalArgumentException("null target method");
        } else if (method == null) {
            throw new IllegalArgumentException("null hook method");
        } else if (Modifier.isStatic(method.getModifiers())) {
            m134(member, method, "Original", "Hook");
            if (method2 != null) {
                if (Modifier.isStatic(method2.getModifiers())) {
                    m134(member, method2, "Original", "Backup");
                } else {
                    throw new IllegalArgumentException("Backup must be a static method: " + method2);
                }
            }
            if (m125() != 0) {
                yafyaLog.error("LHooker", "initClass failed");
            }
            if (member instanceof Method) {
                ((Method) member).setAccessible(true);
            }
            f150.put(method, new C0041(member.getName(), member, method, method2));
            f149 = true;
            if (hookMethodNative(member, method, method2, method3)) {
                if (method2 != null && !method2.isAccessible()) {
                    method2.setAccessible(true);
                }
                f149 = false;
                return;
            }
            f149 = false;
            throw new RuntimeException("Failed to hook " + member + " with " + method);
        } else {
            throw new IllegalArgumentException("Hook must be a static method: " + method);
        }
    }

    private static Member m127(Class cls, String str, String str2) {
        if (cls == null) {
            throw new IllegalArgumentException("null class");
        } else if (str == null) {
            throw new IllegalArgumentException("null method name");
        } else if (str2 != null) {
            return (Member) findMethodNative(cls, str, str2);
        } else {
            throw new IllegalArgumentException("null method signature");
        }
    }

    public static int m125() {
        try {
            if (shouldVisiblyInit()) {
                return visiblyInit(getThread());
            }
            return 0;
        } catch (Throwable th) {
            th.printStackTrace();
            return -1;
        }
    }


    public static void m128(Class cls, String str, Class cls2, Class[] clsArr, Method method, Method method2, Method method3) {
        m133(cls, str, m126(cls2, clsArr), method, method2, method3);
    }

    public static String m126(Class cls, Class... clsArr) {
        String str;
        Class[] clsArr2 = clsArr;
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (clsArr2 != null) {
            int length = clsArr2.length;
            String str2 = "D";
            int i = 0;
            while (i < length) {
                Class cls2 = clsArr2[i];
                if (cls2 == null) {
                    length = length;
                } else {
                    String name = cls2.getName();
                    length = length;
                    if (name.equals("double")) {
                        str2 = str2;
                        sb.append(str2);
                        i++;
                        clsArr2 = clsArr;
                    } else if (name.equals("int")) {
                        sb.append("I");
                    } else if (name.equals("byte")) {
                        sb.append("B");
                    } else if (name.equals("char")) {
                        sb.append("C");
                    } else if (name.equals("long")) {
                        sb.append("J");
                    } else if (name.equals("boolean")) {
                        sb.append("Z");
                    } else if (name.equals("float")) {
                        sb.append("F");
                    } else if (!name.equals("short")) {
                        String replaceAll = name.replaceAll("\\.", "/");
                        if (replaceAll.contains("/")) {
                            if (!replaceAll.startsWith("L") && !replaceAll.startsWith("[")) {
                                replaceAll = "L" + replaceAll;
                            }
                            if (!replaceAll.endsWith(";")) {
                                replaceAll = replaceAll + ";";
                            }
                        }
                        sb.append(replaceAll);
                    } else {
                        sb.append("S");
                    }
                }
                str2 = str2;
                i++;
                clsArr2 = clsArr;
            }
            str = str2;
        } else {
            str = "D";
        }
        sb.append(")");
        String name2 = cls != null ? cls.getName() : "void";
        char c = 65535;
        switch (name2.hashCode()) {
            case -1325958191:
                if (name2.equals("double")) {
                    c = 0;
                    break;
                }
                break;
            case 104431:
                if (name2.equals("int")) {
                    c = 1;
                    break;
                }
                break;
            case 3039496:
                if (name2.equals("byte")) {
                    c = 2;
                    break;
                }
                break;
            case 3052374:
                if (name2.equals("char")) {
                    c = 3;
                    break;
                }
                break;
            case 3327612:
                if (name2.equals("long")) {
                    c = 4;
                    break;
                }
                break;
            case 3625364:
                if (name2.equals("void")) {
                    c = 5;
                    break;
                }
                break;
            case 64711720:
                if (name2.equals("boolean")) {
                    c = 6;
                    break;
                }
                break;
            case 97526364:
                if (name2.equals("float")) {
                    c = 7;
                    break;
                }
                break;
            case 109413500:
                if (name2.equals("short")) {
                    c = '\b';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                sb.append(str);
                break;
            case 1:
                sb.append("I");
                break;
            case 2:
                sb.append("B");
                break;
            case 3:
                sb.append("C");
                break;
            case 4:
                sb.append("J");
                break;
            case 5:
                str = "V";
                sb.append(str);
                break;
            case 6:
                sb.append("Z");
                break;
            case 7:
                sb.append("F");
                break;
            case '\b':
                sb.append("S");
                break;
            default:
                str = name2.replaceAll("\\.", "/");
                if (str.contains("/")) {
                    if (!str.startsWith("L") && !str.startsWith("[")) {
                        str = "L" + str;
                    }
                    if (!str.endsWith(";")) {
                        str = str + ";";
                    }
                }
                sb.append(str);
                break;
        }
        return sb.toString();
    }
}
