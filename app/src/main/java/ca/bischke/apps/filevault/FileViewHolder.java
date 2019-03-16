package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FileViewHolder extends RecyclerView.ViewHolder
{
    FileGridListener fileGridListener;
    TextView textFileName;
    ImageView imageFileIcon;

    public FileViewHolder(View view, FileGridListener onNoteListener)
    {
        super(view);

        this.fileGridListener = onNoteListener;

        textFileName = view.findViewById(R.id.text_file_name);
        imageFileIcon = view.findViewById(R.id.image_file_icon);

        imageFileIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fileGridListener.onFileClick(getAdapterPosition());
            }
        });
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
