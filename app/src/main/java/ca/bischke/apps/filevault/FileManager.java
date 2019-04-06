package ca.bischke.apps.filevault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private final File vaultFilesDirectory = new File(vaultDirectory + File.separator + "Files");
    private final File vaultTempDirectory = new File(vaultDirectory + File.separator + "Temp");
    private final File vaultFileList = new File(vaultDirectory + File.separator + "FileVault-Files.txt");
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

    public File getVaultFilesDirectory()
    {
        return vaultFilesDirectory;
    }

    public File getVaultTempDirectory()
    {
        return vaultTempDirectory;
    }

    public File getVaultFileList()
    {
        return vaultFileList;
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

    private boolean vaultTempDirectoryExists()
    {
        return vaultTempDirectory.exists();
    }

    private void createDirectory(File directory)
    {
        String name = directory.getName();

        if (!directory.exists())
        {
            if (directory.mkdirs())
            {
                Log.d(TAG, name + " Directory created");
            }
            else
            {
                Log.d(TAG, name + " Directory could not be created");
            }
        }
        else
        {
            Log.d(TAG, name + " Directory already exists");
        }
    }

    public void createVaultFilesDirectory()
    {
        createDirectory(vaultFilesDirectory);
    }

    public void createVaultTempDirectory()
    {
        createDirectory(vaultTempDirectory);
    }

    private File createVaultFileSubdirectory(String fileName)
    {
        createVaultFilesDirectory();

        fileName = getFileNameWithoutExtension(fileName);
        File fileDirectory = new File(vaultFilesDirectory + File.separator + fileName);

        if (fileDirectory.mkdirs())
        {
            Log.d(TAG, fileName + " Directory created");
            return fileDirectory;
        }
        else
        {
            Log.d(TAG, fileName + " Directory could not be created");
            return null;
        }
    }

    private String getFileNameWithoutExtension(String fileName)
    {
        int position = fileName.lastIndexOf('.');

        if (position == -1)
        {
            return fileName;
        }
        else
        {
            return fileName.substring(0, position);
        }
    }

    public String getFileNameWithoutExtension(File file)
    {
        String fileName = file.getName();

        return getFileNameWithoutExtension(fileName);
    }

    public void moveFileToVaultFiles(File file, String fileName)
    {
        File directory = createVaultFileSubdirectory(fileName);

        if (directory != null)
        {
            String directoryPath = directory.getAbsolutePath();
            File vaultFile = new File(directoryPath + File.separator + fileName);

            if (file.renameTo(vaultFile))
            {
                Log.d(TAG, fileName + " moved to Vault Directory");

                try
                {
                    if (FileTypes.isImage(vaultFile))
                    {
                        exportImageThumbnail(vaultFile, directory);
                    }
                    else if (FileTypes.isVideo(vaultFile))
                    {
                        exportVideoThumbnail(vaultFile, directory);
                    }
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Failed to create thumbnail");
                }
            }
            else
            {
                Log.d(TAG, fileName + " could not be moved");
            }
        }
        else
        {
            Log.d(TAG, fileName + " could not be moved - No directory");
        }
    }

    public void moveFileToVaultFiles(File file)
    {
        String fileName = file.getName();
        moveFileToVaultFiles(file, fileName);
    }

    private void exportImageThumbnail(File file, File directory)
            throws IOException
    {
        File thumbnail = new File(directory + File.separator + "thumbnail.jpg");

        int size = 512;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        if (bitmap.getWidth() < size || bitmap.getHeight() < size)
        {
            if (bitmap.getWidth() <= bitmap.getHeight())
            {
                size = bitmap.getWidth();
            }
            else
            {
                size = bitmap.getHeight();
            }
        }

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size);

        FileOutputStream fileOutputStream = new FileOutputStream(thumbnail);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    private void exportVideoThumbnail(File file, File directory)
            throws IOException
    {
        File thumbnail = new File(directory + File.separator + "thumbnail.jpg");
        String videoPath = file.getAbsolutePath();

        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);

        FileOutputStream fileOutputStream = new FileOutputStream(thumbnail);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    public File getMainFileFromVaultSubdirectory(File directory)
    {
        ArrayList<File> files = getFilesInDirectory(directory);

        if (files.size() > 1)
        {
            for (File file : files)
            {
                if (file.getName().startsWith(directory.getName()))
                {
                    return file;
                }
            }
        }

        return files.get(0);
    }

    public File getThumbnailFromVaultSubdirectory(File directory)
    {
        ArrayList<File> files = getFilesInDirectory(directory);

        for (File file : files)
        {
            if (file.getName().equals("thumbnail.jpg"))
            {
                return file;
            }
        }

        return null;
    }

    public boolean isFileInVaultDirectory(File file)
    {
        String filePath = file.getAbsolutePath();
        String vaultFilesPath = vaultFilesDirectory.getAbsolutePath();

        return filePath.contains(vaultFilesPath);
    }

    public void clearVaultTempDirectory()
    {
        if (vaultTempDirectoryExists())
        {
            if (vaultTempDirectory.listFiles().length > 0)
            {
                File[] fileArray = vaultTempDirectory.listFiles();

                for (File file : fileArray)
                {
                    file.delete();
                }
            }
        }
    }
}
