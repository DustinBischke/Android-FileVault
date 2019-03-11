package ca.bischke.apps.filevault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import java.io.File;

public class FileData
{
    private String fileName;
    private String filePath;
    private Bitmap fileIcon;

    private String[] imageFormats = new String[] {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    public FileData(File file)
    {
        fileName = file.getName();
        filePath = file.getAbsolutePath();

        for (String format : imageFormats)
        {
            if (file.getName().endsWith(format))
            {
                int size = 512;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                fileIcon = ThumbnailUtils.extractThumbnail(bitmap, size, size);
            }
        }
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public Bitmap getFileIcon()
    {
        return fileIcon;
    }
}
