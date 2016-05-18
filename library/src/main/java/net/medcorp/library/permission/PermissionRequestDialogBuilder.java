package net.medcorp.library.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.medcorp.library.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karl-john on 18/5/16.
 */
public class PermissionRequestDialogBuilder implements MaterialDialog.SingleButtonCallback{

    private String title;
    private String text;

    private List<String> requestedPermissions;
    private List<String> notGrantedPermissions;

    private Context context;

    private Activity activity;
    private int resultId = 1;

    public PermissionRequestDialogBuilder(Context context) {
        this.context = context;
        requestedPermissions = new ArrayList<>();
        notGrantedPermissions = new ArrayList<>();
        setTitle(R.string.med_library_permission_title);
        setText(R.string.med_library_permission_text);
    }

    public void addPermission(String permission){
        requestedPermissions.add(permission);
    }

    public void clearList(){
        requestedPermissions.clear();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTitle(int titleId) {
        this.title = context.getString(titleId);
    }

    public void setText(int textId) {
        this.text = context.getString(textId);
    }

    public void askForPermission(Activity activity, int resultId){
        this.activity = activity;
        this.resultId = resultId;
        for (String permission: requestedPermissions) {
            if (ActivityCompat.checkSelfPermission(context, String.valueOf(permission)) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermissions.add(permission);
            }
        }
        if(!notGrantedPermissions.isEmpty()) {
            new MaterialDialog.Builder(context)
                    .title(title)
                    .content(text)
                    .positiveText(context.getString(android.R.string.ok))
                    .onPositive(this)
                    .cancelable(false)
                    .show();
        }
    }


    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if(activity != null) {
            String[] array = notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]);
            ActivityCompat.requestPermissions(activity, array, resultId);
        }
        notGrantedPermissions.clear();
    }

    public static boolean requiredToAskPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}

