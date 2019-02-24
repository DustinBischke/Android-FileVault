package ca.bischke.apps.filevault;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SetPasswordActivity extends AppCompatActivity
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
        setContentView(R.layout.activity_setpassword);

        fileEncryption = new FileEncryption(this);
    }

    public void buttonSetPassword(View view)
    {
        EditText textPassword = findViewById(R.id.text_password);
        String password = textPassword.getText().toString();

        if (!password.equals(""))
        {
            try
            {
                String key = getString(R.string.preference_pass);
                byte[] hashPassword = SHA.getHashBytes(password);

                fileEncryption.saveSharedPreference(key, hashPassword);

                Intent intent = new Intent(this, SetupCompleteActivity.class);
                startActivity(intent);
            }
            catch (Exception ex)
            {
                // TODO Log error
            }
        }
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
