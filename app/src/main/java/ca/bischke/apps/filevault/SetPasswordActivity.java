package ca.bischke.apps.filevault;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SetPasswordActivity extends AppCompatActivity
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

        // Sets Activity Layout
        setContentView(R.layout.activity_set_password);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Displays Back Button in Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("");
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        // Handles Toolbar back button click event
        onBackPressed();
        return true;
    }

    public void buttonSetPassword(View view)
    {
        EditText textPassword = findViewById(R.id.edittext_password);
        String password = textPassword.getText().toString();
        EditText textPassword2 = findViewById(R.id.edittext_confirm_password);
        String password2 = textPassword2.getText().toString();

        if (!password.equals("") && !password2.equals(""))
        {
            if (password.equals(password2))
            {
                try
                {
                    // TODO Fix password not working with password length 5, 9, 13
                    String key = getString(R.string.preference_pass);
                    byte[] hashPassword = Hash.getHashBytes(password);

                    KeyStore keyStore = new KeyStore(this);
                    keyStore.setBytes(key, hashPassword);

                    Intent intent = new Intent(this, SetupCompleteActivity.class);
                    intent.putExtra("ENCRYPTION_KEY", password);
                    startActivity(intent);
                }
                catch (Exception ex)
                {
                    // TODO Log error
                }
            }
            else
            {
                Toast toast = Toast.makeText(this, getString(R.string.password_no_match), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
