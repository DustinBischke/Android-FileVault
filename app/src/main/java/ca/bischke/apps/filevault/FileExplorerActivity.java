package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.Environment;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class FileExplorerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private Permissions permissions;
    private Encryption encryption;
    private final String TAG = "FileVault";
    private final String STORAGE_ROOT = Environment.getExternalStorageDirectory().toString();
    private final String STORAGE_VAULT = STORAGE_ROOT + File.separator + "FileVault";
    private String currentDirectory;
    private boolean sortByName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasPermissions())
        {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_fileexplorer);

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

        createVaultDirectory();
        listFiles(STORAGE_ROOT);
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
                listFiles(currentDirectory);
                break;
            case R.id.action_by_date:
                sortByName = false;
                listFiles(currentDirectory);
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
        else
        {
            listFiles(getParentDirectory(currentDirectory));
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

    private void listFiles(String path)
    {
        Log.d(TAG, "Listing Files in " + path);
        currentDirectory = path;
        clearLayoutFiles();
        scrollToTop();
        displayCurrentDirectory();

        File directory = new File(path);
        File[] fileArray = directory.listFiles();
        ArrayList<File> files = new ArrayList<>(Arrays.asList(fileArray));

        // Sort Files Alphabetically
        getSortedFiles(files);

        final LinearLayout layoutFiles = findViewById(R.id.layout_files);

        for (File file : files)
        {
            final FileLayout fileLayout = new FileLayout(this, file);

            final File file1 = file;
            final boolean isDirectory = file.isDirectory();
            final String newPath = path + File.separator + file.getName();

            fileLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (isDirectory)
                    {
                        listFiles(newPath);
                    }
                    else
                    {
                        // TODO: CONFIGURE TO ONLY DO THIS IF IT IS AN IMAGE FILE
                        startImageViewActivity(file1);

                        // TODO: Setup Encrypt Button to execute moveFileToVaultDirectory(file1)
                    }
                }
            });

            layoutFiles.addView(fileLayout);
        }
    }

    public void startImageViewActivity(File file)
    {
        Intent intent = new Intent(this, ViewImageActivity.class);
        intent.putExtra("FILE_PATH", file.getAbsolutePath());
        startActivity(intent);
    }

    private String getParentDirectory(String path)
    {
        if (path.equals(STORAGE_ROOT))
        {
            return STORAGE_ROOT;
        }

        if (path.length() > 0)
        {
            int endIndex = path.lastIndexOf('/');

            if (endIndex != -1)
            {
                return path.substring(0, endIndex);
            }
        }

        return STORAGE_ROOT;
    }

    private String getCurrentDirectory()
    {
        if (currentDirectory.equals(STORAGE_ROOT))
        {
            return getString(R.string.internal_storage);
        }
        else
        {
            int startIndex = currentDirectory.lastIndexOf('/') + 1;
            return currentDirectory.substring(startIndex, currentDirectory.length());
        }
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

    private void displayCurrentDirectory()
    {
        setTitle(getCurrentDirectory());
    }

    private void createVaultDirectory()
    {
        File directory = new File(STORAGE_VAULT);

        if (!directory.exists())
        {
            if (directory.mkdirs())
            {
                Log.d(TAG, "Vault Directory created");
            }
            else
            {
                Log.d(TAG, "Vault Directory could not be created");
            }
        }
        else
        {
            Log.d(TAG, "Vault Directory already exists");
        }
    }

    private void moveFileToVaultDirectory(File file)
    {
        File directory = new File(STORAGE_VAULT);

        if (!directory.exists())
        {
            createVaultDirectory();
        }

        String fileName = file.getName();
        File to = new File(directory.toString() + File.separator + fileName);

        if (file.renameTo(to))
        {
            Log.d(TAG, fileName + " moved to Vault Directory");
        }
        else
        {
            Log.d(TAG, fileName + " could not be moved");
        }
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
}
