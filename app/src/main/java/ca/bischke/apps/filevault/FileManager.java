package ca.bischke.apps.filevault;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class FileManager
{
    private final String TAG = "FileVault";
    private final String rootPath = Environment.getExternalStorageDirectory().toString();
    private final String vaultPath = rootPath + File.separator + "FileVault";
    private final File rootDirectory = new File(rootPath);
    private final File vaultDirectory = new File(vaultPath);
    private File currentDirectory;

    public FileManager()
    {
        currentDirectory = rootDirectory;
    }

    public File getRootDirectory()
    {
        return rootDirectory;
    }

    public File getVaultDirectory()
    {
        return vaultDirectory;
    }

    public File getCurrentDirectory()
    {
        return currentDirectory;
    }

    public void setCurrentDirectory(File directory)
    {
        currentDirectory = directory;
    }

    public boolean currentDirectoryIsRoot()
    {
        return currentDirectory.equals(rootDirectory);
    }

    public File getParentDirectory()
    {
        String path = currentDirectory.getAbsolutePath();

        if (path.equals(rootPath))
        {
            return rootDirectory;
        }

        if (path.length() > 0)
        {
            int endIndex = path.lastIndexOf('/');

            if (endIndex != -1)
            {
                return new File(path.substring(0, endIndex));
            }
        }

        return rootDirectory;
    }

    public ArrayList<File> getFilesInDirectory(File directory)
    {
        if (directory.isDirectory())
        {
            File[] fileArray = directory.listFiles();
            return new ArrayList<>(Arrays.asList(fileArray));
        }

        return null;
    }

    public ArrayList<File> getSortedFiles(ArrayList<File> files, boolean sortByName)
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

    private boolean vaultExists()
    {
        return vaultDirectory.exists();
    }

    public void createVault()
    {
        if (!vaultExists())
        {
            if (vaultDirectory.mkdirs())
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

    public void moveFileToVault(File file)
    {
        String fileName = file.getName();
        moveFileToVault(file, fileName);
    }

    public void moveFileToVault(File file, String fileName)
    {
        if (!vaultExists())
        {
            createVault();
        }

        File to = new File(vaultPath + File.separator + fileName);

        if (file.renameTo(to))
        {
            Log.d(TAG, fileName + " moved to Vault Directory");
        }
        else
        {
            Log.d(TAG, fileName + " could not be moved");
        }
    }
}
