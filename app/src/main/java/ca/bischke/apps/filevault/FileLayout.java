package ca.bischke.apps.filevault;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLayout extends LinearLayout
{
    private File file;
    private String[] imageFormats = new String[] {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    public FileLayout(Context context, File file)
    {
        // Instantiates File Layout XML
        super(context);
        this.file = file;
        inflate(context, R.layout.file, this);

        // Displays File name
        TextView fileName = findViewById(R.id.text_file_name);
        fileName.setText(file.getName());

        // Displays File last modified date
        TextView fileDate = findViewById(R.id.text_file_date);
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy");
        String formattedDate = simpleDateFormat.format(date);
        fileDate.setText(formattedDate);

        ImageView fileIcon = findViewById(R.id.image_file_icon);
        TextView fileSize = findViewById(R.id.text_file_size);

        if (file.isDirectory())
        {
            // Displays Directory Icon
            fileIcon.setImageResource(R.drawable.ic_folder_24dp);

            // Removes File Size TextView
            fileSize.setVisibility(View.GONE);

            // Removes Encrypt Button
            ImageButton encryptIcon = findViewById(R.id.button_file_encrypt);
            encryptIcon.setVisibility(View.GONE);
        }
        else
        {
            // Formats and displays File size
            fileSize.setText(Formatter.formatShortFileSize(context, file.length()));

            if (isImageFile(file))
            {
                // Displays Image Icon
                fileIcon.setImageResource(R.drawable.ic_image_24dp);

                // TODO: Setup File Icon to display Image Preview
                //fileIcon.setImageURI(Uri.fromFile(file));
            }
        }
    }

    public boolean isImageFile(File file)
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

    public File getFile()
    {
        return file;
    }

    public String getFileName()
    {
        return file.getName();
    }

    public ImageButton getImageButton()
    {
        return findViewById(R.id.button_file_encrypt);
    }
}
