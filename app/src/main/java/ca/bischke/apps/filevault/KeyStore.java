package ca.bischke.apps.filevault;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyStore
{
    private SharedPreferences sharedPreferences;

    public KeyStore(Context context)
    {
        String file = context.getString(R.string.app_name);
        sharedPreferences = context.getSharedPreferences(file, Context.MODE_PRIVATE);
    }

    public boolean exists(String key)
    {
        return sharedPreferences.contains(key);
    }

    public byte[] getBytes(String key)
    {
        if (exists(key))
        {
            String valueString = sharedPreferences.getString(key, null);
            return Converter.getByteArrayFromString(valueString);
        }

        return null;
    }

    public void setBytes(String key, byte[] valueBytes)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String value = Converter.getStringFromByteArray(valueBytes);
        editor.putString(key, value);
        editor.apply();
    }

    public void remove(String key)
    {
        if (exists(key))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    public void clear()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
