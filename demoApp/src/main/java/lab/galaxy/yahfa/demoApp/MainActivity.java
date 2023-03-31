package lab.galaxy.yahfa.demoApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Method;

import com.android.flinger.yafya.YafyaMain;



public class MainActivity extends Activity {
    private static final String TAG = "origin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.hookBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Method hook = Hook_Log_e.class.getDeclaredMethod("hook", String.class, String.class);
                    Method target = Hook_Log_e.class.getDeclaredMethod("target", String.class, String.class);

                    Method backup = Hook_Log_e.class.getDeclaredMethod("backup", String.class, String.class);

                    YafyaMain.findAndBackupAndHook(Hook_Log_e.class, "target", Hook_Log_e.methodSig, hook, backup);


                    try{
                        SystemPropertyPook.HelloPookSystemProps(getApplication());
                        SystemPropertyPook.HelloPookSystemProps_1(getApplication());
                        Log.e("darren:", "HelloPookSystemProps end");
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                    YafyaMain.start(1);

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 Hook_Log_e.target("src_123", "dest_456");

                 System.getProperty("xxxxx");
                System.getProperty("xxxxx","yyyyy");

                 Log.e("darren", "test end  "+ getPackageName());
            }
        });

        findViewById(R.id.pre_Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent();
                intent.setClass(getApplication(), Pre_So_Activity.class);
                startActivity(intent);

            }
        });


    }

    void doWork() {
        Log.e("darren", "call Log.e()");
    }
}
