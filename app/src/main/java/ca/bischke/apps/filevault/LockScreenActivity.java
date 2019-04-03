package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class LockScreenActivity extends AppCompatActivity
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
            return;
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_lock_screen);
    }

    public void buttonUnlock(View view)
    {
        EditText textPassword = findViewById(R.id.text_password);
        final String password = textPassword.getText().toString();

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
                    LinearLayout layout = findViewById(R.id.layout_decrypting);
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                    layout.setVisibility(View.VISIBLE);
                    layout.startAnimation(fadeIn);

                    ImageView image = findViewById(R.id.image_decrypting);
                    Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                    image.startAnimation(rotate);

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Intent intent = new Intent(LockScreenActivity.this, VaultActivity.class);
                            intent.putExtra("ENCRYPTION_KEY", password);
                            startActivity(intent);
                            finish();
                        }
                    }).start();
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
