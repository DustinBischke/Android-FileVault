package ca.bischke.apps.filevault;

import android.content.Intent;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
{
    private final String TAG = "FileVault";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Preferences preferences;
    private boolean setup;

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

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null)
        {
            if (extras.containsKey("Email"))
            {
                editTextEmail.setText(extras.getString("Email"));
            }

            if (extras.containsKey("Password"))
            {
                editTextPassword.setText(extras.getString("Password"));
            }

            if (extras.containsKey("SETUP"))
            {
                setup = true;
                preferences = new Preferences(this);

                Button buttonRegister = findViewById(R.id.button_register);
                buttonRegister.setVisibility(View.GONE);
            }
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        // Handles Toolbar back button click event
        onBackPressed();
        return true;
    }

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
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("Email", email);
        intent.putExtra("Password", password);

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
                        if (task.isSuccessful())
                        {
                            if (firebaseAuth.getCurrentUser().isEmailVerified())
                            {
                                Toast toast = Toast.makeText(getApplicationContext(), "Logged in", Toast.LENGTH_SHORT);
                                toast.show();

                                Log.d(TAG, "Logged in");

                                if (setup)
                                {
                                    firebaseUser = firebaseAuth.getCurrentUser();
                                    String reference = "user/" + firebaseUser.getUid();
                                    databaseReference = firebaseDatabase.getReference(reference);

                                    restoreValue("key", getString(R.string.preference_pass));
                                    restoreValue("salt", getString(R.string.preference_salt));
                                    restoreValue("iv", getString(R.string.preference_iv));

                                    Intent intent = new Intent(getApplicationContext(), SetupCompleteActivity.class);
                                    startActivity(intent);
                                }

                                finish();
                            }
                            else
                            {
                                Toast toast = Toast.makeText(getApplicationContext(), "You must verify your email", Toast.LENGTH_SHORT);
                                toast.show();

                                Log.d(TAG, "You must verify your email");
                            }
                        }
                        else
                        {
                            Toast toast = Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT);
                            toast.show();

                            Log.d(TAG, "Login failed");
                        }
                    }
                });
    }

    private void restoreValue(final String child, final String preference)
    {
        databaseReference.child(child).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String value = dataSnapshot.getValue().toString();
                    preferences.setString(preference, value);
                }
                else
                {
                    Log.d(TAG, "Value not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d(TAG, databaseError.toString());
            }
        });
    }
}
