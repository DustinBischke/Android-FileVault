package ca.bischke.apps.filevault;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class BackupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FileListener
{
    private final String TAG = "FileVault";
    private FileManager fileManager;
    private RecyclerView recyclerView;
    private ArrayList<File> fileList;
    private FileBackupAdapter fileAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference storageReference;
    private StorageReference userReference;
    private DatabaseReference databaseReference;
    private String encryptionKey;
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
        setContentView(R.layout.activity_backup);

        // Sets Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Backup");

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

        // Create Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            Log.d(TAG, "Logged in as: " + firebaseUser.getEmail());

            String reference = "user/" + firebaseUser.getUid();
            userReference = storageReference.child(reference);
            databaseReference = firebaseDatabase.getReference(reference);
        }

        // Setup File Adapter
        fileList = new ArrayList<>();
        fileAdapter = new FileBackupAdapter(this, fileList, this, userReference);
        fileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fileAdapter);

        listFiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Adds Menu to the Toolbar
        getMenuInflater().inflate(R.menu.backup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles Menu item clicks
        switch(id)
        {
            case R.id.action_account:
                buttonAccount();
            case R.id.action_refresh:
                buttonRefresh();
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
                startFileExplorer();
                break;
            case R.id.nav_backup:
                break;
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
    }

    private void buttonAccount()
    {
        startAccountIntent();
    }

    private void buttonRefresh()
    {
        listFiles();
    }

    private void buttonSettings()
    {
        startSettingsIntent();
    }

    public void buttonBackup(View view)
    {
        backupFiles();
    }

    public void buttonRestore(View view)
    {
        restoreFiles();
    }

    private void listFiles()
    {
        scrollToTop();
        fileList.clear();

        File vault = fileManager.getVaultFilesDirectory();
        ArrayList<File> files = fileManager.getFilesInDirectory(vault);
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

    private void scrollToTop()
    {
        recyclerView.scrollToPosition(0);
    }

    private boolean isVerifiedUser()
    {
        return (firebaseUser != null && firebaseUser.isEmailVerified());
    }

    private void backupFiles()
    {
        File vault = fileManager.getVaultFilesDirectory();
        ArrayList<File> directories = fileManager.getFilesInDirectory(vault);

        for (File directory : directories)
        {
            backupUpdatedFiles(directory);
        }
    }

    private void backupUpdatedFiles(File directory)
    {
        ArrayList<File> files = fileManager.getFilesInDirectory(directory);
        String directoryName = directory.getName();

        for (final File file : files)
        {
            final Uri uri = Uri.fromFile(file);
            final String reference = directoryName + File.separator + uri.getLastPathSegment();

            final String fileName = file.getName();
            final StorageReference fileReference = userReference.child(reference);

            // Checks if File already exists
            fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
            {
                @Override
                public void onSuccess(StorageMetadata storageMetadata)
                {
                    if (storageMetadata.getSizeBytes() == file.length())
                    {
                        Log.d(TAG, fileName + " is up to date");
                    }
                    else
                    {
                        if (storageMetadata.getCreationTimeMillis() > file.lastModified())
                        {
                            Log.d(TAG, fileName + " is older than uploaded version");
                        }
                        else if (storageMetadata.getCreationTimeMillis() < file.lastModified())
                        {
                            Log.d(TAG, fileName + " is newer than uploaded version");
                            uploadFile(uri, fileReference, reference);
                        }
                        else
                        {
                            Log.d(TAG, fileName + " is up to date");
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, fileName + " is not backed up");
                    uploadFile(uri, fileReference, reference);
                }
            });
        }
    }

    private void uploadFile(final Uri uri, final StorageReference fileReference, final String reference)
    {
        UploadTask uploadTask = fileReference.putFile(uri);
        final String fileName = uri.getLastPathSegment();

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Toast toast = Toast.makeText(getApplicationContext(), fileName + " uploaded successfully", Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG, fileName + " uploaded successfully");

                writeToDatabase(reference);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast toast = Toast.makeText(getApplicationContext(), fileName + " failed to upload", Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG,  fileName + " failed to upload: " + e.getMessage());
            }
        });
    }

    private void writeToDatabase(String reference)
    {
        DatabaseReference newFileReference = databaseReference.push();
        newFileReference.setValue(reference);
    }

    private void restoreFiles()
    {
        databaseReference.orderByValue().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.getChildrenCount() > 0)
                {
                    restoreMissingFiles(dataSnapshot.getChildren());
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "No files found", Toast.LENGTH_SHORT);
                    toast.show();

                    Log.d(TAG, "No files found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d(TAG, databaseError.toString());
            }
        });
    }

    private void restoreMissingFiles(Iterable<DataSnapshot> files)
    {
        for (DataSnapshot dataSnapshot : files)
        {
            String reference = dataSnapshot.getValue().toString();
            String[] parts = reference.split("/");

            File directory = new File(fileManager.getVaultFilesDirectory() + File.separator + parts[0]);

            if (!directory.exists())
            {
                directory.mkdirs();
            }

            final File file = new File(directory + File.separator + parts[1]);

            final String fileName = file.getName();
            final StorageReference fileReference = userReference.child(reference);

            // Checks if File already exists
            fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
            {
                @Override
                public void onSuccess(StorageMetadata storageMetadata)
                {
                    if (file.exists())
                    {
                        if (storageMetadata.getSizeBytes() == file.length())
                        {
                            Log.d(TAG, fileName + " is up to date");
                        }
                        else
                        {
                            if (storageMetadata.getCreationTimeMillis() > file.lastModified())
                            {
                                Log.d(TAG, fileName + " is older than uploaded version");
                                downloadFile(file, fileReference);
                            }
                            else if (storageMetadata.getCreationTimeMillis() < file.lastModified())
                            {
                                Log.d(TAG, fileName + " is newer than uploaded version");
                            }
                            else
                            {
                                Log.d(TAG, fileName + " is up to date");
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, fileName + " is not on local device");
                        downloadFile(file, fileReference);
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, fileName + " is not uploaded");
                }
            });
        }
    }

    private void downloadFile(final File file, StorageReference fileReference)
    {
        final String fileName = file.getName();

        fileReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
            {
                Toast toast = Toast.makeText(getApplicationContext(), fileName + " downloaded successfully", Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG, fileName + " downloaded successfully");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast toast = Toast.makeText(getApplicationContext(), fileName + " failed to download", Toast.LENGTH_SHORT);
                toast.show();

                Log.d(TAG,  fileName + " failed to download: " + e.getMessage());
            }
        });
    }

    private void startAccountIntent()
    {
        if (isVerifiedUser())
        {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void startVault()
    {
        Intent intent = new Intent(this, VaultActivity.class);
        intent.putExtra("ENCRYPTION_KEY", encryptionKey);
        startActivity(intent);
        finish();
    }

    private void startFileExplorer()
    {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        intent.putExtra("ENCRYPTION_KEY", encryptionKey);
        startActivity(intent);
        finish();
    }

    private void startSettingsIntent()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFileClick(int position)
    {
        // TODO
    }

    @Override
    public void onFileLongClick(int position)
    {
        // TODO
    }

    @Override
    public void onMenuClick(int position)
    {
        File directory = fileAdapter.getDataFromPosition(position);
        backupUpdatedFiles(directory);
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
