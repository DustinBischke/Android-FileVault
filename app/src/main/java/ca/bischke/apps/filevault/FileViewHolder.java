package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FileViewHolder extends RecyclerView.ViewHolder
{
    TextView fileName;
    ImageView fileIcon;

    public FileViewHolder(View view)
    {
        super(view);

        fileName = view.findViewById(R.id.text_file_name);
        fileIcon = view.findViewById(R.id.image_file_icon);
    }
}
