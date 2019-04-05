package ca.bischke.apps.filevault;

import java.io.File;
import java.util.Arrays;

public class FileTypes
{
    private static String[] audioFormats = new String[] {"mp3", "ogg", "wav", "m4a", "mid", "xmf", "aac", "flac"};
    private static String[] imageFormats = new String[] {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    private static String[] videoFormats = new String[] {"mp4", "3gp", "webm", "mkv"};

    private static String getFileExtension(File file)
    {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public static boolean isAudio(File file)
    {
        String extension = getFileExtension(file);
        return Arrays.asList(audioFormats).contains(extension);
    }

    public static boolean isImage(File file)
    {
        String extension = getFileExtension(file);
        return Arrays.asList(imageFormats).contains(extension);
    }

    public static boolean isVideo(File file)
    {
        String extension = getFileExtension(file);
        return Arrays.asList(videoFormats).contains(extension);
    }
}
