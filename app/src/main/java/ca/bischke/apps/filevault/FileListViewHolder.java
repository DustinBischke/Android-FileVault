package ca.bischke.apps.filevault;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileListViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener
{
    private FileListListener fileListListener;
    private TextView textFileName;
    private TextView textFileDate;
    private TextView textFileSize;
    private ImageView imageFileIcon;
    private ImageButton buttonFileEncrypt;

    public FileListViewHolder(View view, final FileListListener fileListListener)
    {
        super(view);

        this.fileListListener = fileListListener;
        textFileName = view.findViewById(R.id.text_file_name);
        textFileDate = view.findViewById(R.id.text_file_date);
        textFileSize = view.findViewById(R.id.text_file_size);
        imageFileIcon = view.findViewById(R.id.image_file_icon);
        buttonFileEncrypt = view.findViewById(R.id.button_file_encrypt);

        view.setOnClickListener(this);
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

    public ImageButton getButtonFileEncrypt()
    {
        return buttonFileEncrypt;
    }

    @Override
    public void onClick(View view)
    {
        fileListListener.onFileClick(getAdapterPosition());
    }
}
