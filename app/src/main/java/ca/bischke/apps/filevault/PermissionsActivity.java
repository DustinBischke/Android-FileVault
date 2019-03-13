package ca.bischke.apps.filevault;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PermissionsActivity extends AppCompatActivity
{
    private Permissions permissions;
    private final int STORAGE_PERMISSION_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        permissions = new Permissions(this);

        // Switch to FileExplorer if permissions already granted
        if (permissions.hasStoragePermission())
        {
            startWelcomeActivity();
            finish();
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
                    startWelcomeActivity();
                }
            }
        }
    }

    public void buttonRequestPermissions(View view)
    {
        if (AndroidVersion.isM())
        {
            if (!permissions.hasStoragePermission())
            {
                // Request the permissions
                ActivityCompat.requestPermissions(this, permissions.getStoragePermission(), STORAGE_PERMISSION_CODE);
            }
        }
    }

    public void buttonExit(View view)
    {
        finish();
        System.exit(0);
    }

    private void startWelcomeActivity()
    {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}
