package ca.bischke.apps.filevault;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VaultActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FileListener
{
    private final String TAG = "FileVault";
    private Permissions permissions;
    private FileManager fileManager;
    private Encryption encryption;
    private RecyclerView recyclerView;
    private ArrayList<File> fileList;
    private FileGridAdapter fileAdapter;
    private boolean sortByName = true;
    private final int CAMERA_PERMISSION_CODE = 22;
    private final int REQUEST_IMAGE_CAPTURE = 23;
    private Uri cameraImageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference userReference;
    private String encryptionKey;

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

        // Create Vault directory
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
        fileList = new ArrayList<>();
        fileAdapter = new FileGridAdapter(this, fileList, this);
        fileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fileAdapter);

        // Create Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            Log.d(TAG, "Logged in as: " + firebaseUser.getEmail());

            userReference = storageReference.child("user/" + firebaseUser.getUid());
        }

        decryptThumbnails();
        listFiles();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        encryptThumbnails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Adds Menu to the Toolbar
        getMenuInflater().inflate(R.menu.vault, menu);
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
                break;
            case R.id.action_backup:
                buttonBackup();
                break;
            case R.id.action_by_name:
                buttonSortByName();
                break;
            case R.id.action_by_date:
                buttonSortByDate();
                break;
            case R.id.action_settings:
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

    // TODO Update to generate Thumbnail file
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            // Get path of captured image
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(cameraImageUri, projection, null, null, null);
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String cameraImagePath = cursor.getString(dataIndex);

            // Set new file name
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmss", Locale.getDefault());
            String date = simpleDateFormat.format(new Date());
            String fileName = "IMG_" + date + ".jpg";

            // Move image into Vault
            File file = new File(cameraImagePath);
            fileManager.moveFileToVault(file, fileName);

            // Display image
            File vault = fileManager.getVaultDirectory();
            file = new File(vault + File.separator + fileName);
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

    private void buttonAccount()
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

    private void buttonBackup()
    {
        if (isVerifiedUser())
        {
            backupFiles();
        }
        else
        {
            buttonAccount();
        }
    }

    private void buttonSortByName()
    {
        if (!sortByName)
        {
            sortByName = true;
            listFiles();
        }
    }

    private void buttonSortByDate()
    {
        if (sortByName)
        {
            sortByName = false;
            listFiles();
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
        fileList.clear();

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

    // TODO Upload Encrypted files
    private void backupFiles()
    {
        File vault = fileManager.getVaultDirectory();
        ArrayList<File> files = fileManager.getFilesInDirectory(vault);

        for (final File file : files)
        {
            final Uri uri = Uri.fromFile(file);
            StorageReference fileReference = userReference.child(uri.getLastPathSegment());

            // Checks if File already exists
            fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
            {
                @Override
                public void onSuccess(StorageMetadata storageMetadata)
                {
                    if (storageMetadata.getSizeBytes() != file.getTotalSpace())
                    {
                        Log.d(TAG, file.getName() + " already uploaded");
                    }
                    else
                    {
                        Log.d(TAG, file.getName() + " updated since last upload");
                        uploadFile(uri);
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, file.getName() + " not uploaded");
                    uploadFile(uri);
                }
            });
        }
    }

    private void uploadFile(final Uri uri)
    {
        StorageReference fileReference = userReference.child(uri.getLastPathSegment());
        UploadTask uploadTask = fileReference.putFile(uri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Log.d(TAG, uri.getLastPathSegment() + " uploaded successfully");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.d(TAG,  uri.getLastPathSegment() + " failed to upload: " + e.getMessage());
            }
        });
    }

    private void encryptThumbnails()
    {
        File vault = fileManager.getVaultDirectory();
        ArrayList<File> directories = fileManager.getFilesInDirectory(vault);

        ArrayList<File> files = new ArrayList<>();

        for (File directory : directories)
        {
            File file = fileManager.getThumbnailFromDirectory(directory);

            if (file != null)
            {
                files.add(file);
            }
        }

        try
        {
            encryption.encryptFileList(encryptionKey, files);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void decryptThumbnails()
    {
        File vault = fileManager.getVaultDirectory();
        ArrayList<File> directories = fileManager.getFilesInDirectory(vault);

        ArrayList<File> files = new ArrayList<>();

        for (File directory : directories)
        {
            File file = fileManager.getThumbnailFromDirectory(directory);

            if (file != null)
            {
                files.add(file);
            }
        }

        try
        {
            encryption.decryptFileList(encryptionKey, files);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void encryptVault()
    {
        try
        {
            File vault = fileManager.getVaultDirectory();
            encryption.encryptDirectory(encryptionKey, vault);
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
            encryption.decryptDirectory(encryptionKey, vault);
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void startFileExplorer()
    {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        intent.putExtra("ENCRYPTION_KEY", encryptionKey);
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

    private void openFile(File file)
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
    }

    @Override
    public void onFileClick(int position)
    {
        File directory = fileAdapter.getDataFromPosition(position);
        File file = fileManager.getMainFileFromDirectory(directory);

        openFile(file);
    }

    @Override
    public void onMenuClick(int position)
    {
        FileGridViewHolder fileViewHolder = (FileGridViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
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

        popupMenu.getMenuInflater().inflate(R.menu.file_grid, popupMenu.getMenu());

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
                        openFile(file);
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
