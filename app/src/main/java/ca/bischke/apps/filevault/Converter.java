package ca.bischke.apps.filevault;

import android.util.Base64;

public class Converter
{
    public static byte[] getByteArrayFromString(String string)
    {
        return Base64.decode(string, Base64.DEFAULT);
    }

    public static String getStringFromByteArray(byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
