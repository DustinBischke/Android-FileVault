package ca.bischke.apps.filevault;

import android.os.Build;

public class AndroidVersion
{
    private static int version = Build.VERSION.SDK_INT;

    public static int getVersion()
    {
        return version;
    }

    public static boolean isM()
    {
        return version >= Build.VERSION_CODES.M;
    }
}
