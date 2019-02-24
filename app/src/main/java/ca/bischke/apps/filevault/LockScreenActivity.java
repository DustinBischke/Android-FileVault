package ca.bischke.apps.filevault;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class LockScreenActivity extends AppCompatActivity
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

        // Sets Activity Layout
        setContentView(R.layout.activity_lockscreen);

        fileEncryption = new FileEncryption(this);
    }

    public void buttonUnlock(View view)
    {
        EditText textPassword = findViewById(R.id.text_password);
        String password = textPassword.getText().toString();
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
