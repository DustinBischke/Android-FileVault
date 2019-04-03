package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayerActivity extends AppCompatActivity
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

        setContentView(R.layout.activity_video_player);

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
                Log.d(TAG, ex.getMessage());
            }
        }

        playVideo();
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
                Log.d(TAG, ex.getMessage());
            }
        }
        else
        {
            // TODO Display message that file is already in Vault
            Log.d(TAG, "File already in vault");
        }
    }

    private void playVideo()
    {
        VideoView videoView = findViewById(R.id.videoview);

        // Set MediaController for play, pause, scrolling controls
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Play the video
        videoView.setVideoPath(filePath);
        videoView.requestFocus();
        videoView.start();
    }
}
