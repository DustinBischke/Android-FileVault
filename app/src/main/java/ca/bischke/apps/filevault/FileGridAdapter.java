package ca.bischke.apps.filevault;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FileGridAdapter extends RecyclerView.Adapter<FileGridViewHolder>
{
    private Context context;
    private FileManager fileManager;
    private List<File> fileList;
    private FileListener fileListener;

    public FileGridAdapter(Context context, List<File> fileList, FileListener fileListener)
    {
        this.context = context;
        this.fileList = fileList;
        this.fileListener = fileListener;
        fileManager = new FileManager();
    }

    @NonNull
    @Override
    public FileGridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_grid, viewGroup, false);
        return new FileGridViewHolder(view, fileListener);
    }

    // TODO Match List Adapter
    @Override
    public void onBindViewHolder(@NonNull final FileGridViewHolder fileViewHolder, int i)
    {
        File directory = fileList.get(i);
        File file = fileManager.getMainFileFromVaultSubdirectory(directory);

        TextView textFileName = fileViewHolder.getTextFileName();
        textFileName.setText(file.getName());

        ImageView imageFileIcon = fileViewHolder.getImageFileIcon();

        if (fileManager.getThumbnailFromVaultSubdirectory(directory) != null)
        {
            new ThumbnailAsyncTask().execute(imageFileIcon, directory);
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
        return fileList.size();
    }

    public File getDataFromPosition(int position)
    {
        return fileList.get(position);
    }

    private class ThumbnailAsyncTask extends AsyncTask<Object, Void, Bitmap>
    {
        private ImageView imageFileIcon;

        @Override
        protected Bitmap doInBackground(Object... objects)
        {
            imageFileIcon = (ImageView) objects[0];
            File file = (File) objects[1];

            File thumbnail = fileManager.getThumbnailFromVaultSubdirectory(file);
            String thumbnailPath = thumbnail.getAbsolutePath();

            return BitmapFactory.decodeFile(thumbnailPath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageFileIcon.setImageBitmap(bitmap);
            imageFileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
