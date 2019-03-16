package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FileViewHolder extends RecyclerView.ViewHolder
{
    TextView textFileName;
    ImageView imageFileIcon;

    public FileViewHolder(View view)
    {
        super(view);

        textFileName = view.findViewById(R.id.text_file_name);
        imageFileIcon = view.findViewById(R.id.image_file_icon);
    }

    public TextView getTextFileName()
    {
        return textFileName;
    }

    public ImageView getImageFileIcon()
    {
        return imageFileIcon;
    }
}
