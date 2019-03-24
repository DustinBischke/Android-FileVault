package ca.bischke.apps.filevault;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity
{
    private final String TAG = "FileVault";
    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;

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
        setContentView(R.layout.activity_login);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Displays Back Button in Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(R.string.login);

        editTextEmail = findViewById(R.id.edittext_email);
        editTextPassword = findViewById(R.id.edittext_password);

        editTextEmail.addTextChangedListener(new EditTextWatcher(editTextEmail));
        editTextPassword.addTextChangedListener(new EditTextWatcher(editTextPassword));

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        // Handles Toolbar back button click event
        onBackPressed();
        return true;
    }

    // TODO Improve error display layout
    public void buttonLogin(View view)
    {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            editTextEmail.setError("Email cannot be blank");
            return;
        }

        if (TextUtils.isEmpty(password))
        {
            editTextPassword.setError("Password cannot be blank");
            return;
        }

        login(email, password);
    }

    public void buttonRegister(View view)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void login(String email, String password)
    {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        // TODO Switch to Activity / Display error
                        if (task.isSuccessful())
                        {
                            if (firebaseAuth.getCurrentUser().isEmailVerified())
                            {
                                Log.d(TAG, "Login successful :)");

                                Intent intent = new Intent(getApplicationContext(), VaultActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Log.d(TAG, "Please verify your email");
                            }
                        }
                        else
                        {
                            Log.d(TAG, "Login failed :(");
                        }
                    }
                });
    }
}
