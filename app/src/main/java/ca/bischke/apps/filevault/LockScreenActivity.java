package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

public class LockScreenActivity extends AppCompatActivity
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
            return;
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_lockscreen);
    }

    public void buttonUnlock(View view)
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
                byte[] savedPassword = keyStore.getBytes(key);

                if (Arrays.equals(hashPassword, savedPassword))
                {
                    Intent intent = new Intent(this, VaultActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast toast = Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            catch (Exception ex)
            {
                // TODO Log error
            }
        }
    }
}
