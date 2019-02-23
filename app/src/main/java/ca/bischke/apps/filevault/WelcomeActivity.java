package ca.bischke.apps.filevault;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.concurrent.locks.Lock;

public class WelcomeActivity extends AppCompatActivity
{
    private FileEncryption fileEncryption;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Switch to PermissionsActivity if permissions are not granted
        if (!hasPermissions())
        {
            startPermissionsActivity();
            finish();
            return;
        }

        fileEncryption = new FileEncryption(this);

        // If IV and Salt already exist, switch to Lockscreen
        if (fileEncryption.sharedPreferenceExists(getString(R.string.preference_iv)) &&
                fileEncryption.sharedPreferenceExists(getString(R.string.preference_salt)))
        {
            Intent intent = new Intent(this, LockScreenActivity.class);
            startActivity(intent);
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_welcome);
    }

    public void buttonStart(View view)
    {
        Intent intent = new Intent(this, SetPasswordActivity.class);
        startActivity(intent);
    }

    private boolean hasPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }

        return true;
    }

    private void startPermissionsActivity()
    {
        Intent intent = new Intent(this, PermissionsActivity.class);
        startActivity(intent);
    }
}
