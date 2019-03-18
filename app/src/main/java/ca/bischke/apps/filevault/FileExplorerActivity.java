package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

public class FileExplorerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FileListListener
{
    private final String TAG = "FileVault";
    private FileManager fileManager;
    private Encryption encryption;
    private RecyclerView recyclerView;
    private ArrayList<FileListData> fileDataList;
    private FileListAdapter fileAdapter;
    private boolean sortByName = true;

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
        setContentView(R.layout.activity_file_explorer);

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Recycler View Caching
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(12);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheEnabled(false);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Setup File Adapter
        fileDataList = new ArrayList<>();
        fileAdapter = new FileListAdapter(this, fileDataList, this);
        fileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fileAdapter);

        listFiles(fileManager.getCurrentDirectory());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        /*encryptVault();
        finish();*/
    }

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
                listFiles(fileManager.getCurrentDirectory());
                break;
            case R.id.action_by_date:
                sortByName = false;
                listFiles(fileManager.getCurrentDirectory());
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
            case R.id.nav_file_vault:
                Intent intent = new Intent(this, VaultActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_file_explorer:
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
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            if (!fileManager.currentDirectoryIsRoot())
            {
                listFiles(fileManager.getParentDirectory());
            }
        }
    }

    private void listFiles(File directory)
    {
        Log.d(TAG, "Listing Files in " + directory.getAbsolutePath());
        fileManager.setCurrentDirectory(directory);
        displayCurrentDirectory();
        scrollToTop();

        fileDataList.clear();

        // Get Files in Directory and Sort them
        ArrayList<File> files = fileManager.getFilesInDirectory(directory);
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

    private void displayCurrentDirectory()
    {
        if (fileManager.currentDirectoryIsRoot())
        {
            setTitle(getString(R.string.internal_storage));
            return;
        }

        setTitle(fileManager.getCurrentDirectory().getName());
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

    @Override
    public void onFileClick(int position)
    {
        FileListData fileData = fileAdapter.getDataFromPosition(position);
        File file = fileData.getFile();
        String filePath = fileData.getFilePath();

        if (file.isDirectory())
        {
            listFiles(file);
        }
        else
        {
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
            fileDataList.add(new FileListData(file));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            fileAdapter.notifyDataSetChanged();
        }
    }
}
