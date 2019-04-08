package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FileCloudViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener
{
    private FileListener fileListener;
    private TextView textFileName;
    private TextView textFileDate;
    private TextView textFileSize;
    private ImageView imageFileIcon;
    private ImageButton buttonFileBackup;

    public FileCloudViewHolder(View view, final FileListener fileListener)
    {
        super(view);

        this.fileListener = fileListener;
        textFileName = view.findViewById(R.id.text_file_name);
        textFileDate = view.findViewById(R.id.text_file_date);
        textFileSize = view.findViewById(R.id.text_file_size);
        imageFileIcon = view.findViewById(R.id.image_file_icon);
        buttonFileBackup = view.findViewById(R.id.button_file_menu);

        buttonFileBackup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fileListener.onMenuClick(getAdapterPosition());
            }
        });

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public TextView getTextFileName()
    {
        return textFileName;
    }

    public TextView getTextFileDate()
    {
        return textFileDate;
    }

    public TextView getTextFileSize()
    {
        return textFileSize;
    }

    public ImageView getImageFileIcon()
    {
        return imageFileIcon;
    }

    public ImageButton getButtonFileBackup()
    {
        return buttonFileBackup;
    }

    @Override
    public void onClick(View view)
    {
        fileListener.onFileClick(getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View view)
    {
        fileListener.onFileLongClick(getAdapterPosition());
        return true;
    }
}
