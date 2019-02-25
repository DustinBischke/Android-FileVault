package ca.bischke.apps.filevault;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder>
{
    private Context context;
    private List<FileData> fileDataList;

    public FileAdapter(Context context, List<FileData> fileDataList)
    {
        this.context = context;
        this.fileDataList = fileDataList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_grid, viewGroup, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder fileViewHolder, int i)
    {
        fileViewHolder.fileName.setText(fileDataList.get(i).getFileName());

        if (fileDataList.get(i).getFileIcon() != null)
        {
            fileViewHolder.fileIcon.setImageURI(fileDataList.get(i).getFileIcon());
        }
    }

    @Override
    public int getItemCount()
    {
        return fileDataList.size();
    }
}
