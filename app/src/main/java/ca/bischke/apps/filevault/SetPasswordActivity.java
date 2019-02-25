package ca.bischke.apps.filevault;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SetPasswordActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Permissions permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasPermissions())
        {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_setpassword);
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
                byte[] hashPassword = Hash.getHashBytes(password);

                KeyStore keyStore = new KeyStore(this);
                keyStore.setBytes(key, hashPassword);

                Intent intent = new Intent(this, SetupCompleteActivity.class);
                startActivity(intent);
            }
            catch (Exception ex)
            {
                // TODO Log error
            }
        }
    }
}
