package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class VaultActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private Encryption encryption;
    private final String TAG = "FileVault";
    private final String STORAGE_ROOT = Environment.getExternalStorageDirectory().toString();
    private final String STORAGE_VAULT = STORAGE_ROOT + File.separator + "FileVault";
    private boolean sortByName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Permissions permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasPermissions())
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

        encryption = new Encryption(this);

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
    }

    private ArrayList<File> getSortedFiles(ArrayList<File> files)
    {
        if (sortByName)
        {
            return getFilesSortedByName(files);
        }
        else
        {
            return getFilesSortedByDate(files);
        }
    }

    private ArrayList<File> getFilesSortedByName(ArrayList<File> files)
    {
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });

        return files;
    }

    private ArrayList<File> getFilesSortedByDate(ArrayList<File> files)
    {
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                long f1 = file1.lastModified();
                long f2 = file2.lastModified();

                if (f1 > f2)
                {
                    return -1;
                }
                else if (f1 < f2)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        });

        return files;
    }

    private void listFiles()
    {
        File directory = new File(STORAGE_VAULT);
        File[] fileArray = directory.listFiles();
        ArrayList<File> files = new ArrayList<>(Arrays.asList(fileArray));

        // Sort Files Alphabetically
        getSortedFiles(files);

        // Setting up Adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Add margin between all RecyclerView items
        GridMarginDecoration itemDecoration = new GridMarginDecoration(this, R.dimen.grid_margin);
        recyclerView.addItemDecoration(itemDecoration);

        ArrayList<FileData> fileDataList = new ArrayList<>();

        for (File file : files)
        {
            fileDataList.add(new FileData(file));
        }

        FileAdapter fileAdapter = new FileAdapter(this, fileDataList);
        recyclerView.setAdapter(fileAdapter);
    }

    private void encryptVault()
    {
        try
        {
            File vault = new File(STORAGE_VAULT);
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
            File vault = new File(STORAGE_VAULT);
            encryption.decryptDirectory("SHIBA", vault);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }
}
