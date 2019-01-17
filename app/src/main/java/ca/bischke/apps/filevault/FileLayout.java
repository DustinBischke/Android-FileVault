package ca.bischke.apps.filevault;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLayout extends LinearLayout
{
    public FileLayout(Context context, File file)
    {
        super(context);
        inflate(context, R.layout.file, this);

        TextView fileName = findViewById(R.id.text_file_name);
        fileName.setText(file.getName());

        TextView fileDate = findViewById(R.id.text_file_date);
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy");
        String formattedDate = simpleDateFormat.format(date);
        fileDate.setText(formattedDate);

        if (file.isDirectory())
        {
            ImageView icon = findViewById(R.id.image_file_icon);
            icon.setImageResource(R.drawable.ic_folder_24dp);
        }
    }
}
