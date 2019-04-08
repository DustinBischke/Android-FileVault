package ca.bischke.apps.filevault;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences
{
    private SharedPreferences sharedPreferences;

    public Preferences(Context context)
    {
        String file = context.getString(R.string.app_name);
        sharedPreferences = context.getSharedPreferences(file, Context.MODE_PRIVATE);
    }

    public boolean exists(String key)
    {
        return sharedPreferences.contains(key);
    }

    public boolean getBoolean(String key)
    {
        if (exists(key))
        {
            return sharedPreferences.getBoolean(key, false);
        }

        return false;
    }

    public void setBoolean(String key, boolean value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key)
    {
        if (exists(key))
        {
            return sharedPreferences.getString(key, null);
        }

        return null;
    }

    public void setString(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
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
}
