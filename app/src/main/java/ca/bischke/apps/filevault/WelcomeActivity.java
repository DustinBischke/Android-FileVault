package ca.bischke.apps.filevault;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Permissions permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasStoragePermission())
        {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }

        Preferences preferences = new Preferences(this);

        // If Password already exists, switch to Lockscreen
        if (preferences.exists(getString(R.string.preference_pass)))
        {
            Intent intent = new Intent(this, LockScreenActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_welcome);
    }

    public void buttonNewUser(View view)
    {
        Intent intent = new Intent(this, SetPasswordActivity.class);
        startActivity(intent);
    }

    public void buttonLogin(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("SETUP", true);
        startActivity(intent);
    }
}
