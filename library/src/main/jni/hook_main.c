#include <jni.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "trampoline.h"
#include "xhook/xhook.h"
#include "xhook/xh_log.h"

int SDKVersion;
static uint32_t OFFSET_entry_point_from_interpreter_in_ArtMethod;
static uint32_t OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod;
static uint32_t OFFSET_ArtMehod_in_Object;
static uint32_t OFFSET_access_flags_in_ArtMethod;
static uint32_t kAccNative = 0x0100;
static uint32_t kAccCompileDontBother = 0x01000000;
static uint32_t kAccFastInterpreterToInterpreterInvoke = 0x40000000;
static uint32_t kAccPreCompiled = 0x00200000;

static jfieldID fieldArtMethod = NULL;


//com.android.flinger.yafya
//com_android_flinger_yafya

static char * __cdecl mystrstr(const char *str1, const char *str2)
{
    char *cp = (char *)str1;
    char *s1, *s2;
    if (!*str2)
        return((char *)str1);
    while (*cp)
    {
        s1 = cp;
        s2 = (char *)str2;
        while (*s2 && !(*s1 - *s2))
            s1++, s2++;
        if (!*s2)
            return(cp);
        cp++;
    }
    return NULL;
}



JNIEXPORT void JNICALL Java_com_android_flinger_yafya_YafyaMain_init(JNIEnv *env, jclass clazz, jint sdkVersion) {
    SDKVersion = sdkVersion;
    jclass classExecutable;
    LOGI("init to SDK %d", sdkVersion);
    switch (sdkVersion) {
        case __ANDROID_API_T__:
        case __ANDROID_API_S_L__:
        case __ANDROID_API_S__:
            kAccPreCompiled = 0x00800000;
        case __ANDROID_API_R__:
            classExecutable = (*env)->FindClass(env, "java/lang/reflect/Executable");
            fieldArtMethod = (*env)->GetFieldID(env, classExecutable, "artMethod", "J");
        case __ANDROID_API_Q__:
        case __ANDROID_API_P__:
            kAccCompileDontBother = 0x02000000;
            OFFSET_ArtMehod_in_Object = 0;
            OFFSET_access_flags_in_ArtMethod = 4;
            //OFFSET_dex_method_index_in_ArtMethod = 4*3;
            if (sdkVersion >= __ANDROID_API_S__)
                OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    roundUpToPtrSize(4 * 3 + 2 * 2) + pointer_size;
            else
                OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    roundUpToPtrSize(4 * 4 + 2 * 2) + pointer_size;
            break;
        case __ANDROID_API_O_MR1__:
            kAccCompileDontBother = 0x02000000;
        case __ANDROID_API_O__:
            OFFSET_ArtMehod_in_Object = 0;
            OFFSET_access_flags_in_ArtMethod = 4;
            OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    roundUpToPtrSize(4 * 4 + 2 * 2) + pointer_size * 2;
            break;
        case __ANDROID_API_N_MR1__:
        case __ANDROID_API_N__:
            OFFSET_ArtMehod_in_Object = 0;
            OFFSET_access_flags_in_ArtMethod = 4; // sizeof(GcRoot<mirror::Class>) = 4
            // ptr_sized_fields_ is rounded up to pointer_size in ArtMethod
            OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    roundUpToPtrSize(4 * 4 + 2 * 2) + pointer_size * 3;
            break;
        case __ANDROID_API_M__:
            OFFSET_ArtMehod_in_Object = 0;
            OFFSET_entry_point_from_interpreter_in_ArtMethod = roundUpToPtrSize(4 * 7);
            OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    OFFSET_entry_point_from_interpreter_in_ArtMethod + pointer_size * 2;
            break;
        case __ANDROID_API_L_MR1__:
            OFFSET_ArtMehod_in_Object = 4 * 2;
            OFFSET_entry_point_from_interpreter_in_ArtMethod = roundUpToPtrSize(
                    OFFSET_ArtMehod_in_Object + 4 * 7);
            OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    OFFSET_entry_point_from_interpreter_in_ArtMethod + pointer_size * 2;
            break;
        case __ANDROID_API_L__:
            OFFSET_ArtMehod_in_Object = 4 * 2;
            OFFSET_entry_point_from_interpreter_in_ArtMethod = OFFSET_ArtMehod_in_Object + 4 * 4;
            OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod =
                    OFFSET_entry_point_from_interpreter_in_ArtMethod + 8 * 2;
            break;
        default:
            LOGE("not compatible with SDK %d", sdkVersion);
            break;
    }

    setupTrampoline(OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod);

    init_dlopen();
}

static uint32_t getFlags(char *method) {
    uint32_t access_flags = read32(method + OFFSET_access_flags_in_ArtMethod);
    return access_flags;
}

static void setFlags(char *method, uint32_t access_flags) {
    write32(method + OFFSET_access_flags_in_ArtMethod, access_flags);
}

static void setNonCompilable(void *method) {
    if (SDKVersion < __ANDROID_API_N__) {
        return;
    }
    uint32_t access_flags = getFlags(method);
    uint32_t old_flags = access_flags;
    access_flags |= kAccCompileDontBother;
    if (SDKVersion >= __ANDROID_API_R__) {
        access_flags &= ~kAccPreCompiled;
    }
    setFlags(method, access_flags);
    LOGI("setNonCompilable: change access flags from 0x%x to 0x%x", old_flags, access_flags);

}

static int replaceMethod(void *fromMethod, void *toMethod, int isBackup) {
    LOGI("replace method from %p to %p", fromMethod, toMethod);

    // replace entry point
    void *newEntrypoint = NULL;
    if(isBackup) {
        void *originEntrypoint = readAddr((char *) toMethod + OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod);
        // entry point hardcoded
        newEntrypoint = genTrampoline(toMethod, originEntrypoint);
    }
    else {
        // entry point from ArtMethod struct
        newEntrypoint = genTrampoline(toMethod, NULL);
    }

    LOGI("replace entry point from %p to %p",
         readAddr((char *) fromMethod + OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod),
         newEntrypoint
    );
    if (newEntrypoint) {
        writeAddr((char *) fromMethod + OFFSET_entry_point_from_quick_compiled_code_in_ArtMethod,
                newEntrypoint);
    } else {
        LOGE("failed to allocate space for trampoline of target method");
        return 1;
    }

    if (OFFSET_entry_point_from_interpreter_in_ArtMethod != 0) {
        void *interpEntrypoint = readAddr((char *) toMethod + OFFSET_entry_point_from_interpreter_in_ArtMethod);
        writeAddr((char *) fromMethod + OFFSET_entry_point_from_interpreter_in_ArtMethod,
                interpEntrypoint);
    }

    // set the target method to native so that Android O wouldn't invoke it with interpreter
    if(SDKVersion >= __ANDROID_API_O__) {
        uint32_t access_flags = getFlags(fromMethod);
        uint32_t old_flags = access_flags;
        if (SDKVersion >= __ANDROID_API_Q__) {
            // On API 29 whether to use the fast path or not is cached in the ART method structure
            access_flags &= ~kAccFastInterpreterToInterpreterInvoke;
        }
        // MakeInitializedClassesVisiblyInitialized is called explicitly
        // entry of jni methods would not be set to jni trampoline after hooked

        // do not set native flag
        // https://github.com/PAGalaxyLab/YAHFA/issues/151
        // hook would fail on debug mode though
        if (SDKVersion <= __ANDROID_API_Q__) {
            // We don't set kAccNative on R+ because they will try to load from real native method pointer instead of entry_point_from_quick_compiled_code_.
            // Ref: https://cs.android.com/android/platform/superproject/+/android-11.0.0_r3:art/runtime/art_method.h;l=844;bpv=1;bpt=1
            access_flags |= kAccNative;
        }
        setFlags(fromMethod, access_flags);
        LOGI("change access flags from 0x%x to 0x%x", old_flags, access_flags);
    }

    return 0;

}

static int doBackupAndHook(void *targetMethod, void *hookMethod, void *backupMethod) {
    LOGI("target method is at %p, hook method is at %p, backup method is at %p",
         targetMethod, hookMethod, backupMethod);

    int res = 0;

    // set kAccCompileDontBother for a method we do not want the compiler to compile
    // so that we don't need to worry about hotness_count_
    if (SDKVersion >= __ANDROID_API_N__) {
        setNonCompilable(targetMethod);
//        setNonCompilable(hookMethod);
        if(backupMethod) setNonCompilable(backupMethod);
    }

    if (backupMethod) {// do method backup
        // we use the same way as hooking target method
        // hook backup method and redirect back to the original target method
        // the only difference is that the entry point is now hardcoded
        // instead of reading from ArtMethod struct since it's overwritten
        res += replaceMethod(backupMethod, targetMethod, 1);
    }

    res += replaceMethod(targetMethod, hookMethod, 0);

    LOGI("hook and backup done ==%p, %p", targetMethod, hookMethod);
    return res;
}

static void *getArtMethod(JNIEnv *env, jobject jmethod) {
    void *artMethod = NULL;

    if(jmethod == NULL) {
        return artMethod;
    }

    if(SDKVersion >= __ANDROID_API_R__) {
        artMethod = (void *) (*env)->GetLongField(env, jmethod, fieldArtMethod);
    }
    else {
        artMethod = (void *) (*env)->FromReflectedMethod(env, jmethod);
    }

    LOGI("ArtMethod: %p", artMethod);
    return artMethod;

}

JNIEXPORT jobject JNICALL Java_com_android_flinger_yafya_YafyaMain_findMethodNative(JNIEnv *env, jclass clazz,
                                                        jclass targetClass, jstring methodName,
                                                        jstring methodSig) {
    const char *c_methodName = (*env)->GetStringUTFChars(env, methodName, NULL);
    const char *c_methodSig = (*env)->GetStringUTFChars(env, methodSig, NULL);
    jobject ret = NULL;


    //Try both GetMethodID and GetStaticMethodID -- Whatever works :)
    jmethodID method = (*env)->GetMethodID(env, targetClass, c_methodName, c_methodSig);
    if (!(*env)->ExceptionCheck(env)) {
        ret = (*env)->ToReflectedMethod(env, targetClass, method, JNI_FALSE);
    } else {
        (*env)->ExceptionClear(env);
        method = (*env)->GetStaticMethodID(env, targetClass, c_methodName, c_methodSig);
        if (!(*env)->ExceptionCheck(env)) {
            ret = (*env)->ToReflectedMethod(env, targetClass, method, JNI_TRUE);
        } else {
            (*env)->ExceptionClear(env);
        }
    }

    (*env)->ReleaseStringUTFChars(env, methodName, c_methodName);
    (*env)->ReleaseStringUTFChars(env, methodSig, c_methodSig);
    return ret;
}
JNIEXPORT jboolean JNICALL
 Java_com_android_flinger_yafya_YafyaMain_backupAndHookNative(JNIEnv *env, jclass clazz,
                                                            jobject target, jobject hook,
                                                            jobject backup) {



    if (!doBackupAndHook(
            getArtMethod(env, target),
            getArtMethod(env, hook),
            getArtMethod(env, backup)
    )) {
        (*env)->NewGlobalRef(env, hook); // keep a global ref so that the hook method would not be GCed
        if(backup) (*env)->NewGlobalRef(env, backup);
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}






/**
 * xhook start >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 *
 * */
#define TAG "xhooker_darren"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##__VA_ARGS__);
#define LOGD(FORMAT, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, FORMAT, ##__VA_ARGS__);
#define LOGV(FORMAT, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, FORMAT, ##__VA_ARGS__);
#define LOGW(FORMAT, ...) __android_log_print(ANDROID_LOG_WARN, TAG, FORMAT, ##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);

int (*old_open)(const char *pathname, int flags, mode_t mode);

FILE *(*old_fopen)(const char *path, const char *mode);

FILE *(*old_popen)(const char *path, const char *mode);

char *(*old_strcpy)(char *destination, const char *source);

int (*old_strcmp)(const char *str1, const char *str2);

int (*old_sscanf)(const char *s, const char *format, ...);

const char *(*old_strstr)(const char *str1, const char *str2);

size_t (*old_strlen)(const char *str);

void *(*old_memcpy)(void *destination, const void *source, size_t num);

void *(*old_mmap)(void *start, size_t length, int prot, int flags, int fd, off_t offset);

void *(*old_malloc)(size_t size);

pid_t (*old_fork)(void);

int (*old_sp_get)(const char *name, char *value);

void (*old_epic_memcpy)(JNIEnv *env, jclass, jlong src, jlong dest, jint length);

void (*old_epic_memput)(JNIEnv *env, jclass, jbyteArray src, jlong dest);

int new_open(const char *pathname, int flags, mode_t mode) {
    //LOGD("new_open pathname=%s flags=%d", pathname, flags);

    if (pathname != NULL){
        if (mystrstr(pathname, "magisk") ||
        mystrstr(pathname, "boot_count") ||
        mystrstr(pathname, "/su"))
        strcpy(pathname, "/data/data/local/tmp");
    }

    int rtn = old_open(pathname, flags, mode);
    //LOGD("new_open rtn=%d", rtn);
    return rtn;
}

FILE *new_fopen(const char *path, const char *mode) {
    //LOGD("new_fopen path=%s mode=%d", path, *mode);
    FILE *rtn = old_fopen(path, mode);
//    LOGD("new_fopen rtn=%d", (int)rtn);
    return rtn;
}

FILE *new_popen(const char *path, const char *mode) {
    LOGD("new_popen path=%s mode=%d", path, *mode);
    FILE *rtn = old_popen(path, mode);
//    LOGD("new_fopen rtn=%d", (int)rtn);
    return rtn;
}

char *new_strcpy(char *destination, const char *source) {
    LOGD("new_strcpy destination=%s source=%s", destination, source);
    char *rtn = old_strcpy(destination, source);
//    LOGD("new_strcpy rtn=%s", rtn);
    return rtn;
}

int new_strcmp(const char *str1, const char *str2) {
    LOGD("new_strcmp destination=%s source=%s", str1, str2);
    int rtn = old_strcmp(str1, str2);
    LOGD("new_strcmp rtn=%d", rtn);
    return rtn;
}

int new_sscanf(const char *s, const char *format, ...) {
    va_list args;
    va_start(args, format);
    int rtn = old_sscanf(s, format, args);
    LOGD("new_sscanf s=%s rtn=%d format=%s", s, rtn, format);
    va_end(args);
    return rtn;
}





const char *new_strstr(const char *str1, const char *str2) {

    if (mystrstr(str2, "libfingerCore")){
        strcpy(str2, "libcttt.so");
    }
    const char *rtn = old_strstr(str1, str2);

    //LOGD("new_strstr str1=%s str2=%s rtn=%s", str1, str2, rtn);
    return rtn;
}

size_t new_strlen(const char *str) {
    size_t rtn = old_strlen(str);
    LOGD("new_strlen str=%s rtn=%lld", str, (unsigned long long) rtn);
    return rtn;
}

void *new_memcpy(void *destination, const void *source, size_t num) {
    LOGD("new_memcpy destination=%s source=%s num=%d", destination, source, (int) num);
    old_memcpy(destination, source, num);
    return destination;
}

void *new_mmap(void *start, size_t length, int prot, int flags, int fd, off_t offset) {
    void *addr = old_mmap(start, length, prot, flags, fd, offset);
    LOGD("new_mmap length=%lld prot=%d flags=%d fd=%d", (unsigned long long) length, prot, flags,
         fd);
    return addr;
}

void *new_malloc(size_t size) {
    LOGD("new_malloc size=%lld", (unsigned long long) size);
    return old_malloc(size);
}

pid_t new_fork(void) {
    LOGD("new_fork");
    return old_fork();
}

int new_sp_get(const char *name, char *value) {
    LOGD("[+] new__system_property_get name=%s value=%s", name, value);
//    if (strstr(name, "ro.serialno")) {
//        return strlen(strcpy(value, "AABBCC"));
//    } else {
//        return old__system_property_get(name, value);
//    }
    return old_sp_get(name, value);
}

void new_epic_memcpy(JNIEnv *env, jclass jclz, jlong src, jlong dest, jint length) {
    LOGD("new_epic_memcpy src=%s dest=%s length=%d", (char *) src, (char *) dest, (int) length);
    old_epic_memcpy(env, jclz, src, dest, length);
}

void new_epic_memput(JNIEnv *env, jclass jclz, jbyteArray src, jlong dest) {
    LOGD("new_epic_memput");
    old_epic_memput(env, jclz, src, dest);
}


static int my_xhooker_log_print(int prio, const char *tag, const char *fmt, ...) {
    va_list ap;
    char buf[1024];
    int r;

    snprintf(buf, sizeof(buf), "[%s] %s", (NULL == tag ? "" : tag), (NULL == fmt ? "" : fmt));

    va_start(ap, fmt);
    r = __android_log_vprint(prio, "xhooker1", buf, ap);
    va_end(ap);
    return r;
}

int (*old_puts)(const char *) = NULL;

int new_puts(const char *string) {
    LOGD("DumpDex:hook puts success, input string:%s", string);
    return old_puts(string);
}

void put111(const char *string) {
    LOGD("DumpDex:put111 string %s", string);
}

void (*old_puts111)(const char *);

void new_puts111(const char *string) {
    LOGD("DumpDex:new_puts111 darren !!!! string=%s", string);
    old_puts111(string);
}

static void *new_thread_func(void *arg)
{
    (void)arg;
    unsigned int i = 0;

    while(1)
    {
        LOGD("DumpDex:new_thread_func put111 success. %u\n", i);
        put111("DumpDex:new_thread_func puts111 ***************");
        i++;
        sleep(1);
    }

    return NULL;
}

int (*old_IsMagicValid)(uint32_t magic) = NULL;
int new_IsMagicValid(uint32_t magic) {
    LOGD("new_IsMagicValid magic:%d", magic);
    return old_IsMagicValid(magic);
}



static int my_system_log_print(int prio, const char* tag, const char* fmt, ...)
{
    XH_LOG_ERROR("  my_system_log_print    %p\n",                                 tag);

    va_list ap;
    char buf[1024];
    int r;

    snprintf(buf, sizeof(buf), "[%s] %s", (NULL == tag ? "" : tag), (NULL == fmt ? "" : fmt));
    XH_LOG_ERROR("  my_system_log_print info:%s\n",                                 buf);

    va_start(ap, fmt);
    r = __android_log_vprint(prio, "xhook_system", buf, ap);
    va_end(ap);
    return r;
}

static int my_libtest_log_print(int prio, const char* tag, const char* fmt, ...)
{

    XH_LOG_ERROR("  my_libtest_log_print    %p\n",                                 tag);

    va_list ap;
    char buf[1024];
    int r;

    snprintf(buf, sizeof(buf), "[%s] %s", (NULL == tag ? "" : tag), (NULL == fmt ? "" : fmt));

    va_start(ap, fmt);
    r = __android_log_vprint(prio, "xhook_libtest", buf, ap);
    va_end(ap);
    return r;
}

JNIEXPORT void JNICALL Java_com_android_flinger_yafya_YafyaMain_start(JNIEnv *env, jclass obj, jint info){

    (void)env;
    (void)obj;

    XH_LOG_ERROR("  darren YafyaMain_start_version_1      %p\n",                                 obj);

//    xhook_register("^/system/.*\\.so$",  "__android_log_print", my_system_log_print,  NULL);
//    xhook_register("^/vendor/.*\\.so$",  "__android_log_print", my_system_log_print,  NULL);
//    xhook_register(".*/liblog\\.so$", "__android_log_print", my_libtest_log_print, NULL);

    //just for testing
//    xhook_ignore(".*/liblog\\.so$", "__android_log_print"); //ignore __android_log_print in liblog.so
//    xhook_ignore(".*/libjavacore\\.so$", NULL); //ignore all hooks in libjavacore.so
//

//    xhook_register(".*/libmainNative\\.so$",  "mmap", testmmap,  old_mmap);
//    xhook_register("^/system/.*\\.so$",  "__android_log_print", my_system_log_print,  NULL);
//    xhook_register("^/vendor/.*\\.so$",  "__android_log_print", my_system_log_print,  NULL);
//    xhook_register(".*/libxhooker\\.so$", "__android_log_print", my_xhooker_log_print, NULL);
//    xhook_refresh(0);    // 同步或一步处理。这里是同步处理。
//    LOGD("Java_com_lxzh123_hooker_XHooker_start");
//    xhook_register(".*/libDexHelper\\.so$", "open",   new_open,   (void **) (&old_open));
//    xhook_register(".*/libDexHelper\\.so$", "fopen",  new_fopen,  (void **) (&old_fopen));
//    xhook_register(".*/libDexHelper\\.so$", "strcpy", new_strcpy, (void **) (&old_strcpy));
//    xhook_register(".*/libDexHelper\\.so$", "strcmp", new_strcmp, (void **) (&old_strcmp));
//    xhook_register(".*/libDexHelper\\.so$", "memcpy", new_memcpy, (void **) (&old_memcpy));
//    xhook_register("^/system/.*\\.so$", "open", new_open, (void **) (&old_open));
//

    char *so1 = ".*/libDexHelper.so$";
    char *so2 = "/data/app/com.hoperun.intelligenceportal-UcG1U7_MibiR9Fsbl66F7Q==/lib/.*.so$";
    //xhook_register(so2, "open", new_open, (void **) (&old_open));
//    xhook_register(so1, "fopen", new_fopen, (void **) (&old_fopen));
//    xhook_register(so2, "fopen", new_fopen, (void **) (&old_fopen));
//    xhook_register(so2, "popen", new_popen, (void **) (&old_popen));
//    xhook_register(so2, "strcpy", new_strcpy, (void **) (&old_strcpy));
//    xhook_register(so2, "strcmp", new_strcmp, (void **) (&old_strcmp));
//    xhook_register(so2, "sscanf", new_sscanf, (void **) (&old_sscanf));
//    xhook_register(so2, "strstr", new_strstr, (void **) (&old_strstr));
//    xhook_register(so2, "strlen", new_strlen, (void **) (&old_strlen));
//    xhook_register(so2, "mmap", new_mmap, (void **) (&old_mmap));
//    xhook_register(so2, "fork", new_fork, (void **) (&old_fork));
//    xhook_register(so1, "__system_property_get", new_sp_get, (void **) (&old_sp_get));
//    xhook_register(so2, "epic_memcpy", new_epic_memcpy, (void **) (&old_epic_memcpy));
//    xhook_register(so2, "epic_memput", new_epic_memput, (void **) (&old_epic_memput));
//    xhook_register(so1, "malloc", new_malloc, (void **) (&old_malloc));
//    xhook_register(so2, "malloc", new_malloc, (void **) (&old_malloc));

  //  xhook_register(so1, "fopen", new_fopen, (void **) (&old_fopen));

//    xhook_register(".*.so$", "fork", new_fork, (void **) (&old_fork));
//    xhook_register(".*.so$", "__system_property_get", new_sp_get, (void **) (&old_sp_get));
//    xhook_register(".*.so$", "strstr", new_strstr, (void **) (&old_strstr));
//    xhook_register(".*.so$", "open", new_open, (void **) (&old_open));
//    xhook_register(".*.so$", "fopen", new_fopen, (void **) (&old_fopen));
//    xhook_register(".*.so$", "popen", new_popen, (void **) (&old_popen));
//     xhook_register(".*.so$", "strcpy", new_strcpy, (void **) (&old_strcpy));
//     xhook_register(".*.so$", "strcmp", new_strcmp, (void **) (&old_strcmp));
   //  xhook_register(".*.so$", "mmap", new_mmap, (void **) (&old_mmap));
     xhook_register(".*.so$", "fork", new_fork, (void **) (&old_fork));
//     xhook_register(".*.so$", "__system_property_get", new_system_property_get,
//                   (void **) (&old_system_property_get)));
//    xhook_register("^/system/.*\\.so$", "memcpy", new_fork, (void **) (&old_fork));
//    xhook_refresh(1);

   // xhook_register("^/system/.*\\.so$",  "fopen", my_system_log_print,  NULL);
    //xhook_register("^/system/.*\\.so$",  "fopen", my_system_log_print,  NULL);

    //xhook_register("^/system/.*\\.so$",  "__android_log_print", my_system_log_print,  NULL);
    //xhook_register("^/vendor/.*\\.so$",  "__android_log_print", my_system_log_print,  NULL);
    //xhook_register(".*/libtest\\.so$", "__android_log_print", my_libtest_log_print, NULL);

    //just for testing
    //xhook_ignore(".*/liblog\\.so$", "__android_log_print"); //ignore __android_log_print in liblog.so
    //xhook_ignore(".*/libjavacore\\.so$", NULL); //ignore all hooks in libjavacore.so


    XH_LOG_ERROR("DumpDex: hook start");
    //xhook_register(".*.so$", "put", new_puts, (void **) (&old_puts));
    //xhook_register(".*.so$", "put111", new_puts111, (void **) (&old_puts111));
    xhook_register(".*.so$", "_ZN3art13DexFileLoader12IsMagicValidEj", new_IsMagicValid, (void **) (&old_IsMagicValid));

    xhook_refresh(0);

    int fd = open("/sdcard/hello11111111111.txt", 1);
    XH_LOG_ERROR("  darren test:                  end           %p\n",                                 obj);

}

JNIEXPORT void JNICALL Java_com_qiyi_xhook_NativeHandler_test
        (JNIEnv *env, jobject obj) {
    (void) env;
    (void) obj;
    puts("DumpDex:load puts ***************");
    put111("DumpDex:load puts111 ***************");

    FILE* file = fopen("/sdcard/hello.txt","w+");
    if ( file != NULL){
        fclose(file);
    }

    int fd = open("/sdcard/hello11111111111.txt", 1);
    if (fd > 0){
        close(fd);
    }

    XH_LOG_ERROR("  Java_com_qiyi_xhook_NativeHandler_test   %p\n", obj);
}


