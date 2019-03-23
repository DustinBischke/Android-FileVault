package ca.bischke.apps.filevault;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            TextView textViewEmail = findViewById(R.id.text_email);
            textViewEmail.setText(firebaseUser.getEmail());
        }
    }

    public void buttonLogout(View view)
    {
        if (firebaseUser != null)
        {
            firebaseAuth.signOut();
        }
    }
}
