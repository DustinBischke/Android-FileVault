package ca.bischke.apps.filevault;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.PopupMenu;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VaultActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FileGridListener
{
    private final String TAG = "FileVault";
    private Permissions permissions;
    private FileManager fileManager;
    private Encryption encryption;
    private RecyclerView recyclerView;
    private ArrayList<FileGridData> fileDataList;
    private FileGridAdapter fileAdapter;
    private boolean sortByName = true;
    private final int CAMERA_PERMISSION_CODE = 22;
    private final int REQUEST_IMAGE_CAPTURE = 23;
    private Uri cameraImageUri;

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

        // Sets Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sets Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Sets Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fileManager = new FileManager();
        fileManager.createVault();

        encryption = new Encryption(this);

        // Setup Recycler View
        recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Add Margin between all RecyclerView Items
        GridMarginDecoration itemDecoration = new GridMarginDecoration(this, R.dimen.grid_margin);
        recyclerView.addItemDecoration(itemDecoration);

        // Recycler View Caching
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(12);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Setup File Adapter
        fileDataList = new ArrayList<>();
        fileAdapter = new FileGridAdapter(this, fileDataList, this);
        fileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fileAdapter);

        listFiles();
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
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String cameraImagePath = cursor.getString(dataIndex);

            // Set new file name
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmss", Locale.getDefault());
            String date = simpleDateFormat.format(new Date());
            String fileName = "IMG_" + date + ".jpg";

            // Move picture into Vault
            File file = new File(cameraImagePath);
            fileManager.moveFileToVault(file, fileName);

            displayFile(file);
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
        scrollToTop();
        fileDataList.clear();

        File vault = fileManager.getVaultDirectory();
        ArrayList<File> files = fileManager.getFilesInDirectory(vault);
        files = fileManager.getSortedFiles(files, sortByName);

        for (File file : files)
        {
            displayFile(file);
        }
    }

    private void displayFile(File file)
    {
        new FileAsyncTask(file).execute();
    }

    private void scrollToTop()
    {
        recyclerView.scrollToPosition(0);
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

    @Override
    public void onFileClick(int position)
    {
        FileGridData fileData = fileAdapter.getDataFromPosition(position);
        String filePath = fileData.getFilePath();

        if (fileData.isImage())
        {
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("FILE_PATH", filePath);
            startActivity(intent);
        }
        else if (fileData.isVideo())
        {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("FILE_PATH", filePath);
            startActivity(intent);
        }
    }

    @Override
    public void onMenuClick(int position)
    {
        FileGridViewHolder fileViewHolder = (FileGridViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        ImageButton menuButton = fileViewHolder.getButtonFileMenu();

        final PopupMenu popupMenu = new PopupMenu(this, menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.file, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                int id = menuItem.getItemId();

                // TODO: Setup Menu buttons
                switch(id)
                {
                    case R.id.file_open:
                        break;
                    case R.id.file_rename:
                        break;
                    case R.id.file_remove:
                        break;
                    case R.id.file_delete:
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        popupMenu.show();
    }

    private class FileAsyncTask extends AsyncTask<Void, Void, Void>
    {
        File file;

        FileAsyncTask(File file)
        {
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            fileDataList.add(new FileGridData(file));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            fileAdapter.notifyDataSetChanged();
        }
    }
}
