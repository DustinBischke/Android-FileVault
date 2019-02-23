package ca.bischke.apps.filevault;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PermissionsActivity extends AppCompatActivity
{
    private final int STORAGE_PERMISSION_CODE = 23;
    private final String[] permissions = new String[]
            {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Switch to FileExplorer if permissions already granted
        if (hasPermissions())
        {
            startFileExplorerActivity();
            finish();
            return;
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case STORAGE_PERMISSION_CODE:
            {
                // If permissions have been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startFileExplorerActivity();
                }
            }
        }
    }

    public void buttonRequestPermissions(View view)
    {
        if (isAndroidM())
        {
            if (!hasPermissions())
            {
                // Request the permissions
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
            }
        }
    }

    public void buttonExit(View view)
    {
        finish();
        System.exit(0);
    }

    private boolean isAndroidM()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean hasPermissions()
    {
        for (String permission : permissions)
        {
            // Checks if each permission has been granted
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        return true;
    }

    private void startFileExplorerActivity()
    {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        startActivity(intent);
    }
}
