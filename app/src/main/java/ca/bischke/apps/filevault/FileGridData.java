package ca.bischke.apps.filevault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;

public class FileGridData
{
    private File file;
    private String fileName;
    private String filePath;
    private Bitmap fileIcon;

    private String[] imageFormats = new String[] {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    private String[] videoFormats = new String[] {".mp4", ".3gp", ".webm", ".mkv"};

    public FileGridData(File file)
    {
        this.file = file;
        fileName = file.getName();
        filePath = file.getAbsolutePath();

        if (isImage())
        {
            int size = 512;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            fileIcon = ThumbnailUtils.extractThumbnail(bitmap, size, size);
        }

        if (isVideo())
        {
            fileIcon = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
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

    public boolean isImage()
    {
        for (String format : imageFormats)
        {
            if (file.getName().endsWith(format))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isVideo()
    {
        for (String format : videoFormats)
        {
            if (file.getName().endsWith(format))
            {
                return true;
            }
        }

        return false;
    }
}
