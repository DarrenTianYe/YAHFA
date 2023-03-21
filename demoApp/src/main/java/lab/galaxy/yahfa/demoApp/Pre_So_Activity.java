package lab.galaxy.yahfa.demoApp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Pre_So_Activity extends Activity {
    private Pre_So_Activity thisInstance;

    AutoCompleteTextView autoCompleteTextView;
    EditText libPath;
    TextView console;
    Button githubButton;
    Button injectButton;
    Button uninjectButton;

    ArrayAdapter<String> adapterItems;

    public String packageName = "com.hoperun.intelligenceportal";
    public String finalLibPath = "";

    private boolean hasRootAccess = false;

    //Setup libsu
    static {
        Shell.enableVerboseLogging = true;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_copy_activity_main);
        thisInstance = this;

        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        libPath = findViewById(R.id.path_to_lib);
        githubButton = findViewById(R.id.github_button);
        injectButton = findViewById(R.id.inject_button);
        uninjectButton = findViewById(R.id.uninject_button);
        console = findViewById(R.id.console);

        //Set installed packages
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, getInstalledApps());
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                packageName = item;
                console.append("Package Name: " + item + "\n");
            }
        });

        //libPath.setText("/data/local/tmp/libchrome_core.so"); //Set default path
        libPath.setText("/data/local/tmp/libfingerCore32.so"); //Set default path


        injectButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (hasRootAccess) {
                    checkLibPath();
                    if (packageName.equals("") || finalLibPath.equals("")) {
                        console.append("Please fill out all the fields\n");
                    } else {
                        injectLibrary();
                    }
                } else {
                    console.append("Bind root service failed: root access not granted\n");
                }
            }
        });

        uninjectButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (packageName.isEmpty()) {
                    console.append("Cannot uninject without a package name\n");
                } else {
                    uninjectLibrary();
                }
            }
        });

        githubButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("xxx"));
                startActivity(browserIntent);
            }
        });

        //Root perm window
        Shell.getShell(shell -> {
            console.append("Injector launched\n");

            //Set SELinux to Permissive
            Shell.cmd("setenforce 0").exec();

            hasRootAccess = true;
        });
    }

    private void injectLibrary() {

        Log.e("darren", "injectLibrary:"+finalLibPath);
        Shell.cmd("chmod a+rx " + finalLibPath).exec();
        Toast.makeText(thisInstance, "copy ! The game might take longer to load", Toast.LENGTH_LONG).show();
    }

    private void uninjectLibrary() {
        String command = "resetprop --delete wrap." + packageName;
        Shell.cmd(command).exec();
        Toast.makeText(thisInstance, "Uninjected!", Toast.LENGTH_LONG).show();
    }

    private void checkLibPath() {
        String path = libPath.getText().toString();
        File file = new File(path);
        finalLibPath = "/data/data/com.hoperun.intelligenceportal/" + file.getName();
        String cmd = "cp " + path + " /data/data/com.hoperun.intelligenceportal/" + file.getName();
        Log.e("darren", "cmd:"+cmd);
        Shell.cmd(cmd).exec();
    }

    private List<String> getInstalledApps() {
        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> ret = new ArrayList<String>();

        for (ApplicationInfo s : packages) {
            //Filter system apps and this app
            if (s.sourceDir.startsWith("/data") && !s.sourceDir.contains("com.reveny.ldpreload.injector") ) {


                if (s.packageName.contains("com.system.installer") ||
                        s.packageName.contains("com.hoperun.intelligenceportal") ||
                        s.packageName.contains("com.example.nativecpp")
                ){
                    ret.add(s.packageName);
                }
            }
        }

        return ret;
    }
}