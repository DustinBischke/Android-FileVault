package ca.bischke.apps.filevault;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class Permissions
{
    private Context context;
    private final String[] permissions = new String[]
            {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public Permissions(Context context)
    {
        this.context = context;
    }

    public String[] getPermissions()
    {
        return permissions;
    }

    public boolean hasPermissions()
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
