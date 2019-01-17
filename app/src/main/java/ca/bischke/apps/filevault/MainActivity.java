package ca.bischke.apps.filevault;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private final String STORAGE_ROOT = Environment.getExternalStorageDirectory().toString();
    private final String STORAGE_VAULT = STORAGE_ROOT + File.separator + "FileVault";
    private String currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createVaultDirectory();
        encryptVaultDirectory();
        listFiles(STORAGE_ROOT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Add Menu to the Action Bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_settings:
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
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });

        final LinearLayout layoutFiles = findViewById(R.id.layout_files);

        for (File file : files)
        {
            Button fileButton = new Button(this);
            fileButton.setText(file.getName());

            final File file1 = file;
            final boolean isDirectory = file.isDirectory();
            final String newPath = path + File.separator + file.getName();

            fileButton.setOnClickListener(new View.OnClickListener()
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
                        moveFileToVaultDirectory(file1);
                    }
                }
            });

            layoutFiles.addView(fileButton);
        }
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
