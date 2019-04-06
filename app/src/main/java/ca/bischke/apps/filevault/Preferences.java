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
}
