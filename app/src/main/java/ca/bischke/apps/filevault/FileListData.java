package ca.bischke.apps.filevault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileListData
{
    private File file;
    private String fileName;
    private String filePath;
    private String fileDate;
    private Long fileSize;
    private Bitmap fileIcon;

    private String[] imageFormats = new String[] {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    private String[] videoFormats = new String[] {".mp4", ".3gp", ".webm", ".mkv"};

    public FileListData(File file)
    {
        this.file = file;
        fileName = file.getName();
        filePath = file.getAbsolutePath();

        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        fileDate = simpleDateFormat.format(date);

        if (!file.isDirectory())
        {
            fileSize = file.length();
        }

        /*if (isImage())
        {
            int size = 64;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            fileIcon = ThumbnailUtils.extractThumbnail(bitmap, size, size);
        }

        if (isVideo())
        {
            fileIcon = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        }*/
    }

    public File getFile()
    {
        return file;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getFileDate()
    {
        return fileDate;
    }

    public Long getFileSize()
    {
        return fileSize;
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
