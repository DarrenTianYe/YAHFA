adb shell am force-stop com.hoperun.intelligenceportal
cd /Users/darren/work/code/github/YAHFA/library/build/intermediates/library_jni/debug/jni/armeabi-v7a
adb push libfingerCore.so /sdcard/
adb shell su -c rm -rf /data/local/tmp/libfingerCore32.so
adb shell su -c cp /sdcard/libfingerCore.so /data/local/tmp/libfingerCore32.so

adb shell su -c cp  /data/local/tmp/libfingerCore32.so  /data/data/com.ccb.longjiLife/libfingerCore32.so
adb shell su -c chmod a+rx /data/data/com.ccb.longjiLife/libfingerCore32.so
adb shell su -c ls -la /data/local/tmp/libfingerCore32.so
adb shell su -c ls -la /data/data/com.ccb.longjiLife/libfingerCore32.so