package ca.bischke.apps.filevault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity
{
    private final String TAG = "FileVault";
    private FileManager fileManager;
    private Encryption encryption;
    private File file;
    private String encryptionKey;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Sets Activity Layout
        super.onCreate(savedInstanceState);

        Permissions permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasStoragePermission())
        {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_image_viewer);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Displays Back Button in Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get File and Encryption Key from Intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null)
        {
            if (extras.containsKey("ENCRYPTION_KEY"))
            {
                encryptionKey = intent.getExtras().getString("ENCRYPTION_KEY");
            }

            if (extras.containsKey("FILE_PATH"))
            {
                filePath = intent.getStringExtra("FILE_PATH");
            }
        }

        file = new File(filePath);

        // Sets Toolbar Title to the File name
        setTitle(file.getName());

        fileManager = new FileManager();
        encryption = new Encryption(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (fileManager.isFileInVault(file))
        {
            try
            {
                encryption.decryptFile(encryptionKey, file, file);
            }
            catch (Exception ex)
            {
                Toast toast = Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG, ex.getMessage());
            }
        }

        try
        {
            displayImage();
        }
        catch (Exception ex)
        {
            Toast toast = Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT);
            toast.show();

            Log.d(TAG, ex.getMessage());
            finish();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (fileManager.isFileInVault(file))
        {
            try
            {
                encryption.encryptFile(encryptionKey, file, file);
            }
            catch (Exception ex)
            {
                Toast toast = Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG, ex.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Adds Menu to the Toolbar
        getMenuInflater().inflate(R.menu.viewimage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles Menu button click events
        switch(id)
        {
            case R.id.action_encrypt:
                buttonEncrypt();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        // Handles Toolbar back button click event
        onBackPressed();
        return true;
    }

    private void buttonEncrypt()
    {
        if (!fileManager.isFileInVault(file))
        {
            String fileName = fileManager.getFileNameWithoutExtension(file);
            fileManager.moveFileToVault(file);

            File vault = fileManager.getVaultDirectory();
            String vaultPath = vault.getAbsolutePath();
            File directory = new File(vaultPath + File.separator + fileName);

            try
            {
                encryption.encryptDirectory(encryptionKey, directory);
                finish();
            }
            catch (Exception ex)
            {
                Toast toast = Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG, ex.getMessage());
            }
        }
        else
        {
            Toast toast = Toast.makeText(this, "File is already in vault", Toast.LENGTH_SHORT);
            toast.show();

            Log.d(TAG, "File is already in vault");
        }
    }

    private Bitmap getScaledBitmap(Bitmap bitmap)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float bitmapRatio = (float) bitmapWidth / (float) bitmapHeight;

        if (bitmapWidth > displayWidth || bitmapHeight > displayHeight)
        {
            int width;
            int height;

            if (bitmapRatio > 1)
            {
                width = displayWidth;
                height = Math.round(displayWidth / bitmapRatio);
            }
            else if (bitmapRatio < 1)
            {
                width = Math.round(displayHeight * bitmapRatio);
                height = displayHeight;
            }
            else
            {
                width = displayWidth;
                height = displayWidth;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }

        return bitmap;
    }

    private void displayImage()
            throws NullPointerException
    {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        if (bitmap != null)
        {
            bitmap = getScaledBitmap(bitmap);

            ImageView imageView = findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            throw new NullPointerException("Unable to get Bitmap from file");
        }
    }
}
