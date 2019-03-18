package ca.bischke.apps.filevault;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class FileGridAdapter extends RecyclerView.Adapter<FileGridViewHolder>
{
    private Context context;
    private List<FileGridData> fileDataList;
    private FileGridListener fileGridListener;

    public FileGridAdapter(Context context, List<FileGridData> fileDataList, FileGridListener fileGridListener)
    {
        this.context = context;
        this.fileDataList = fileDataList;
        this.fileGridListener = fileGridListener;
    }

    @NonNull
    @Override
    public FileGridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_grid, viewGroup, false);
        return new FileGridViewHolder(view, fileGridListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileGridViewHolder fileViewHolder, int i)
    {
        final FileGridData fileData = fileDataList.get(i);

        fileViewHolder.getTextFileName().setText(fileData.getFileName());

        if (fileDataList.get(i).getFileIcon() != null)
        {
            fileViewHolder.getImageFileIcon().setScaleType(ImageView.ScaleType.CENTER_CROP);
            fileViewHolder.getImageFileIcon().setImageBitmap(fileDataList.get(i).getFileIcon());
        }
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getItemCount()
    {
        return fileDataList.size();
    }

    public FileGridData getDataFromPosition(int position)
    {
        return fileDataList.get(position);
    }
}
