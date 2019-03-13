package ca.bischke.apps.filevault;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class Permissions
{
    private Context context;
    private final String[] cameraPermission = new String[]
            { Manifest.permission.CAMERA };
    private final String[] storagePermission = new String[]
            { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    public Permissions(Context context)
    {
        this.context = context;
    }

    public String[] getCameraPermission()
    {
        return cameraPermission;
    }

    public String[] getStoragePermission()
    {
        return storagePermission;
    }

    public boolean hasCameraPermission()
    {
        return hasPermissions(cameraPermission);
    }

    public boolean hasStoragePermission()
    {
        return hasPermissions(storagePermission);
    }

    private boolean hasPermissions(String[] permissions)
    {
        for (String permission : permissions)
        {
            // Checks if each permission has been granted
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        return true;
    }
}
