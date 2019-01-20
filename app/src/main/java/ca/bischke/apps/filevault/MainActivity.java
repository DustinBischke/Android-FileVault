package ca.bischke.apps.filevault;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = "FileVault";
    private boolean sortByName = true;
    private final String STORAGE_ROOT = Environment.getExternalStorageDirectory().toString();
    private final String STORAGE_VAULT = STORAGE_ROOT + File.separator + "FileVault";
    private String currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Sets Activity Layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createVaultDirectory();
        encryptVaultDirectory();
        listFiles(STORAGE_ROOT);
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

        // Handles Menu button click events
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
    public void onBackPressed()
    {
        listFiles(getPreviousDirectory(currentDirectory));
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

    private String getPreviousDirectory(String path)
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
        if (currentDirectory.equals(STORAGE_ROOT))
        {
            setTitle("/");
            return;
        }

        int startIndex = STORAGE_ROOT.length();
        String directory = currentDirectory.substring(startIndex, currentDirectory.length());

        setTitle(directory);
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

    private void encryptVaultDirectory()
    {
        File directory = new File(STORAGE_VAULT);

        if (!directory.exists())
        {
            createVaultDirectory();
        }

        if (directory.listFiles().length > 0)
        {
            for (File file : directory.listFiles())
            {
                encryptFile(file);
            }
        }
        else
        {
            Log.d(TAG, "No files in Vault Directory to encrypt");
        }
    }

    private byte[] getSecureKey()
    {
        byte[] key = null;

        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

            keyGenerator.init(128, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            key = secretKey.getEncoded();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception when generating key" + e.getMessage());
        }

        return key;
    }

    private void encryptFile(File file)
    {
        try
        {
            byte[] key = getSecureKey();
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] fileContent = getFileContent(file);
            byte[] encrypted = cipher.doFinal(fileContent);

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(encrypted);
            bos.flush();
            bos.close();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception when encrypting file" + e.getMessage());
        }
    }

    private byte[] getFileContent(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] fileContent = null;

        try
        {
            fileInputStream = new FileInputStream(file);

            fileContent = new byte[(int)file.length()];
            fileInputStream.read(fileContent);
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "File not found");
        }
        catch (IOException e)
        {
            Log.d(TAG, "Exception when reading file");
        }
        finally
        {
            if (fileInputStream != null)
            {
                try
                {
                    fileInputStream.close();
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Exception when closing FileInputStream");
                }
            }
        }

        return fileContent;
    }
}
