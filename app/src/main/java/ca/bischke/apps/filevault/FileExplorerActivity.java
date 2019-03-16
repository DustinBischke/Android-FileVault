package ca.bischke.apps.filevault;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.File;
import java.util.ArrayList;

public class FileExplorerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private FileManager fileManager;
    private Encryption encryption;
    private final String TAG = "FileVault";
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
        fileManager.createVault();
        listFiles(fileManager.getCurrentDirectory());

        encryption = new Encryption(this);
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

    // TODO: Setup Adapter
    private void listFiles(File directory)
    {
        Log.d(TAG, "Listing Files in " + directory.getAbsolutePath());
        fileManager.setCurrentDirectory(directory);
        displayCurrentDirectory();
        clearLayoutFiles();
        scrollToTop();

        // Get Files in Directory and Sort them
        ArrayList<File> files = fileManager.getFilesInDirectory(directory);
        files = fileManager.getSortedFiles(files, sortByName);

        final LinearLayout layoutFiles = findViewById(R.id.layout_files);

        for (File file : files)
        {
            final FileLayout fileLayout = new FileLayout(this, file);

            final File file1 = file;
            final boolean isDirectory = file.isDirectory();
            String currentPath = fileManager.getCurrentDirectory().getAbsolutePath();
            final File childDirectory = new File(currentPath + File.separator + file.getName());

            fileLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (isDirectory)
                    {
                        listFiles(childDirectory);
                    }
                    else
                    {
                        // TODO: CONFIGURE TO ONLY DO THIS IF IT IS AN IMAGE FILE
                        startImageViewerActivity(file1);
                    }
                }
            });

            // TODO: Setup Encrypt Button to run moveFileToVault()
            if (fileLayout.getImageButton().getVisibility() == View.VISIBLE)
            {
                fileLayout.getImageButton().setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        //moveFileToVaultDirectory(fileLayout, fileLayout.getFile());
                    }
                });
            }

            layoutFiles.addView(fileLayout);
        }
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

    private void clearLayoutFiles()
    {
        LinearLayout layoutFiles = findViewById(R.id.layout_files);

        if (layoutFiles.getChildCount() > 0)
        {
            layoutFiles.removeAllViews();
        }
    }

    private void scrollToTop()
    {
        ScrollView scrollView = findViewById(R.id.scrollview);
        scrollView.scrollTo(0, 0);
    }

    public void startImageViewerActivity(File file)
    {
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra("FILE_PATH", file.getAbsolutePath());
        startActivity(intent);
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
}
