package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FileGridViewHolder extends RecyclerView.ViewHolder
{
    private TextView textFileName;
    private ImageView imageFileIcon;
    private ImageButton buttonFileMenu;

    public FileGridViewHolder(View view, final FileGridListener fileGridListener)
    {
        super(view);

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

        buttonFileMenu = view.findViewById(R.id.button_file_menu);

        buttonFileMenu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fileGridListener.onMenuClick(getAdapterPosition());
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

    public ImageButton getButtonFileMenu()
    {
        return buttonFileMenu;
    }
}
