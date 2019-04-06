package ca.bischke.apps.filevault;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class FileExplorerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FileListener
{
    private final String TAG = "FileVault";
    private FileManager fileManager;
    private Encryption encryption;
    private RecyclerView recyclerView;
    private ArrayList<File> fileList;
    private FileListAdapter fileAdapter;
    private boolean sortByName = true;
    private String encryptionKey;
    private boolean useExternalIntents;

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

        // Get Encryption Key from Intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null)
        {
            if (extras.containsKey("ENCRYPTION_KEY"))
            {
                encryptionKey = intent.getExtras().getString("ENCRYPTION_KEY");
            }
        }

        fileManager = new FileManager();
        fileManager.createVaultFilesDirectory();

        // Setup Recycler View
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Add Divider between all RecyclerView Items
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Recycler View Caching
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(12);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheEnabled(false);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Setup File Adapter
        fileList = new ArrayList<>();
        fileAdapter = new FileListAdapter(this, fileList, this);
        fileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fileAdapter);

        listFiles(fileManager.getCurrentDirectory());

        ImageButton buttonDropdown = findViewById(R.id.button_dropdown);
        buttonDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                PopupMenu popupMenu = new PopupMenu(FileExplorerActivity.this, view);
                final Menu menu = popupMenu.getMenu();

                try
                {
                    Field[] fields = popupMenu.getClass().getDeclaredFields();

                    for (Field field : fields)
                    {
                        if (field.getName().equals("mPopup"))
                        {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popupMenu);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Log.d(TAG, ex.getMessage());
                }

                String currentPath = fileManager.getCurrentDirectory().getAbsolutePath();
                String rootPath = fileManager.getRootDirectory().getAbsolutePath();
                currentPath = currentPath.substring(rootPath.length() + 1, currentPath.length());

                menu.add(Menu.NONE, 0, 0, R.string.internal_storage);
                menu.getItem(0).setIcon(R.drawable.ic_storage_24dp);

                String[] pathParts = currentPath.split("/");

                for (int i = 0; i < pathParts.length; i++)
                {
                    menu.add(Menu.NONE, i + 1, i + 1, pathParts[i]);
                    menu.getItem(i + 1).setIcon(R.drawable.ic_subdirectory_24dp);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        int id = menuItem.getItemId();

                        String path = fileManager.getRootDirectory().getAbsolutePath();

                        for (int i = 1; i <= id; i++)
                        {
                            path += "/" + menu.getItem(i).getTitle().toString();
                        }

                        listFiles(new File(path));

                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        encryption = new Encryption(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Setup Preferences
        Preferences preferences = new Preferences(this);
        useExternalIntents = preferences.getBoolean(getString(R.string.preference_external_intents));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Adds Menu to the Toolbar
        getMenuInflater().inflate(R.menu.file_explorer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles Menu item clicks
        switch(id)
        {
            case R.id.action_by_name:
                buttonSortByName();
                break;
            case R.id.action_by_date:
                buttonSortByDate();
                break;
            case R.id.action_settings:
                buttonSettings();
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
                startVault();
                break;
            case R.id.nav_file_explorer:
                break;
            case R.id.nav_backup:
                startBackupIntent();
            case R.id.nav_settings:
                buttonSettings();
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

    private void buttonSortByName()
    {
        if (!sortByName)
        {
            sortByName = true;
            listFiles(fileManager.getCurrentDirectory());
        }
    }

    private void buttonSortByDate()
    {
        if (sortByName)
        {
            sortByName = false;
            listFiles(fileManager.getCurrentDirectory());
        }
    }

    private void buttonSettings()
    {
        startSettingsIntent();
    }

    private void listFiles(File directory)
    {
        Log.d(TAG, "Listing Files in " + directory.getAbsolutePath());
        fileManager.setCurrentDirectory(directory);
        displayCurrentDirectory();
        scrollToTop();

        fileList.clear();

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
        new FileAsyncTask().execute(file);
    }

    private void displayCurrentDirectory()
    {
        ImageButton buttonDropdown = findViewById(R.id.button_dropdown);

        if (fileManager.currentDirectoryIsRoot())
        {
            setTitle(getString(R.string.internal_storage));
            buttonDropdown.setVisibility(View.GONE);

            return;
        }

        setTitle(fileManager.getCurrentDirectory().getName());
        buttonDropdown.setVisibility(View.VISIBLE);
    }

    private void scrollToTop()
    {
        recyclerView.scrollToPosition(0);
    }

    private void moveFileToVault(File file)
    {
        String fileName = fileManager.getFileNameWithoutExtension(file);
        fileManager.moveFileToVaultFiles(file);

        File vault = fileManager.getVaultFilesDirectory();
        String vaultPath = vault.getAbsolutePath();
        File directory = new File(vaultPath + File.separator + fileName);

        try
        {
            encryption.encryptDirectory(encryptionKey, directory);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void startVault()
    {
        Intent intent = new Intent(this, VaultActivity.class);
        intent.putExtra("ENCRYPTION_KEY", encryptionKey);
        startActivity(intent);
        finish();
    }

    private void startBackupIntent()
    {
        Intent intent = new Intent(this, BackupActivity.class);
        intent.putExtra("ENCRYPTION_KEY", encryptionKey);
        startActivity(intent);
        finish();
    }

    private void startSettingsIntent()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openFile(File file)
    {
        if (useExternalIntents)
        {
            openFileExternal(file);
        }
        else
        {
            openFileInternal(file);
        }
    }

    private void openFileInternal(File file)
    {
        String filePath = file.getAbsolutePath();

        if (FileTypes.isImage(file))
        {
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("ENCRYPTION_KEY", encryptionKey);
            intent.putExtra("FILE_PATH", filePath);
            startActivity(intent);
        }
        else if (FileTypes.isVideo(file))
        {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("ENCRYPTION_KEY", encryptionKey);
            intent.putExtra("FILE_PATH", filePath);
            startActivity(intent);
        }
        else
        {
            openFileExternal(file);
        }
    }

    private void openFileExternal(File file)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".")+1);
        String type = mime.getMimeTypeFromExtension(ext);

        intent.setDataAndType(Uri.fromFile(file), type);
        startActivity(intent);
    }

    @Override
    public void onFileClick(int position)
    {
        File file = fileAdapter.getDataFromPosition(position);

        if (file.isDirectory())
        {
            listFiles(file);
        }
        else
        {
            openFile(file);
        }
    }

    // TODO Multi file selection
    @Override
    public void onFileLongClick(int position)
    {
        onMenuClick(position);
    }

    @Override
    public void onMenuClick(int position)
    {
        FileListViewHolder fileViewHolder = (FileListViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        ImageButton menuButton = fileViewHolder.getButtonFileMenu();

        final File file = fileAdapter.getDataFromPosition(position);

        PopupMenu popupMenu = new PopupMenu(this, menuButton);

        try
        {
            Field[] fields = popupMenu.getClass().getDeclaredFields();

            for (Field field : fields)
            {
                if (field.getName().equals("mPopup"))
                {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }

        popupMenu.getMenuInflater().inflate(R.menu.file_list, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                int id = menuItem.getItemId();

                switch(id)
                {
                    case R.id.file_open:
                        openFile(file);
                        break;
                    case R.id.file_encrypt:
                        moveFileToVault(file);
                    default:
                        break;
                }

                return true;
            }
        });

        popupMenu.show();
    }

    private class FileAsyncTask extends AsyncTask<File, Void, Void>
    {
        @Override
        protected Void doInBackground(File... files)
        {
            fileList.add(files[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            fileAdapter.notifyDataSetChanged();
        }
    }
}
