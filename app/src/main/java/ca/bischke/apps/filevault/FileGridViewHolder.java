package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FileGridViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener
{
    private FileListener fileListener;
    private TextView textFileName;
    private ImageView imageFileIcon;
    private ImageButton buttonFileMenu;

    public FileGridViewHolder(View view, final FileListener fileListener)
    {
        super(view);

        this.fileListener = fileListener;
        textFileName = view.findViewById(R.id.text_file_name);
        imageFileIcon = view.findViewById(R.id.image_file_icon);
        buttonFileMenu = view.findViewById(R.id.button_file_menu);

        buttonFileMenu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fileListener.onMenuClick(getAdapterPosition());
            }
        });

        view.setOnClickListener(this);
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

    @Override
    public void onClick(View view)
    {
        fileListener.onFileClick(getAdapterPosition());
    }
}
