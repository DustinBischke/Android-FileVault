package ca.bischke.apps.filevault;

import android.net.Uri;

import java.io.File;

public class FileData
{
    private String fileName;
    private Uri fileIcon;

    private String[] imageFormats = new String[] {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    public FileData(File file)
    {
        fileName = file.getName();

        for (String format : imageFormats)
        {
            if (file.getName().endsWith(format))
            {
                fileIcon = Uri.fromFile(file);
            }
        }
    }

    public String getFileName()
    {
        return fileName;
    }

    public Uri getFileIcon()
    {
        return fileIcon;
    }
}
