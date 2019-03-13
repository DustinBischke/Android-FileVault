package ca.bischke.apps.filevault;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VaultActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private Permissions permissions;
    private FileManager fileManager;
    private Encryption encryption;
    private final String TAG = "FileVault";
    private boolean sortByName = true;
    private Uri cameraImageUri;
    private final int CAMERA_PERMISSION_CODE = 22;
    private final int REQUEST_IMAGE_CAPTURE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasStoragePermission())
        {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_vault);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fileManager = new FileManager();
        encryption = new Encryption(this);

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //decryptVault();
                listFiles();
            }
        });

        //decryptVault();
        //listFiles();
    }

    /*@Override
    public void onResume()
    {
        super.onResume();
        decryptVault();
        listFiles();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        encryptVault();
        finish();
    }

    /*@Override
    public void onStop()
    {
        super.onStop();
        encryptVault();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Adds Menu to the Toolbar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles Menu item clicks
        switch(id)
        {
            case R.id.action_lock:
                encryptVault();
                finish();
            case R.id.action_settings:
                break;
            case R.id.action_by_name:
                sortByName = true;
                listFiles();
                break;
            case R.id.action_by_date:
                sortByName = false;
                listFiles();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles NavigationView item clicks
        switch(id)
        {
            case R.id.nav_file_explorer:
                startFileExplorer();
                break;
            case R.id.nav_settings:
                break;
            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            // Get path of captured picture
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(cameraImageUri, projection, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String cameraImagePath = cursor.getString(columnIndex);

            // Set new file name
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmss", Locale.getDefault());
            String date = simpleDateFormat.format(new Date());
            String fileName = "IMG_" + date + ".jpg";

            // Move picture into Vault
            File picture = new File(cameraImagePath);
            fileManager.moveFileToVault(picture, fileName);

            // TODO Optimize adding of file
            listFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case CAMERA_PERMISSION_CODE:
            {
                // If permissions have been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startCamera();
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    public void buttonStartFileExplorer(View view)
    {
        startFileExplorer();
    }

    public void buttonStartCamera(View view)
    {
        if (AndroidVersion.isM())
        {
            if (permissions.hasCameraPermission())
            {
                startCamera();
            }
            else
            {
                // Request the permissions
                ActivityCompat.requestPermissions(this, permissions.getCameraPermission(), CAMERA_PERMISSION_CODE);
            }
        }
    }

    private void listFiles()
    {
        // Get Files in Directory and Sort them
        File vault = fileManager.getVaultDirectory();
        ArrayList<File> files = fileManager.getFilesInDirectory(vault);
        files = fileManager.getSortedFiles(files, sortByName);

        // Set up Adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(12);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Add margin between all RecyclerView items
        GridMarginDecoration itemDecoration = new GridMarginDecoration(this, R.dimen.grid_margin);
        recyclerView.addItemDecoration(itemDecoration);

        ArrayList<FileData> fileDataList = new ArrayList<>();

        for (File file : files)
        {
            fileDataList.add(new FileData(file));
        }

        FileAdapter fileAdapter = new FileAdapter(this, fileDataList);
        fileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fileAdapter);
    }

    private void encryptVault()
    {
        try
        {
            File vault = fileManager.getVaultDirectory();
            encryption.encryptDirectory("SHIBA", vault);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void decryptVault()
    {
        try
        {
            File vault = fileManager.getVaultDirectory();
            encryption.decryptDirectory("SHIBA", vault);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void startFileExplorer()
    {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        startActivity(intent);
        finish();
    }

    private void startCamera()
    {
        ContentValues contentValues = new ContentValues();
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }
}
