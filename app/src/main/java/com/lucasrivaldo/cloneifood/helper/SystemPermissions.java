package com.lucasrivaldo.cloneifood.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SystemPermissions {

    public static String[] neededPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    public static boolean validatePermissions(Activity context, int requesCode){

        if( Build.VERSION.SDK_INT >= 23){

            List<String> permissionList = new ArrayList<>();

            for(String permission: neededPermissions){
               boolean havePermission = ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
                   if (!havePermission) permissionList.add(permission);
            }

            if(permissionList.isEmpty())return true;

            String[] neededPermissions = new String[permissionList.size()];
            permissionList.toArray(neededPermissions);

            ActivityCompat.requestPermissions(context, neededPermissions, requesCode);
        }
        return true;
    }
}
