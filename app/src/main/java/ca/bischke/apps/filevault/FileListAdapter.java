package ca.bischke.apps.filevault;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileListAdapter extends RecyclerView.Adapter<FileListViewHolder>
{
    private Context context;
    private List<File> fileList;
    private FileListener fileListener;

    public FileListAdapter(Context context, List<File> fileList, FileListener fileListener)
    {
        this.context = context;
        this.fileList = fileList;
        this.fileListener = fileListener;
    }

    @NonNull
    @Override
    public FileListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_list, viewGroup, false);
        return new FileListViewHolder(view, fileListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileListViewHolder fileViewHolder, int i)
    {
        File file = fileList.get(i);

        TextView textFileName = fileViewHolder.getTextFileName();
        textFileName.setText(file.getName());

        TextView textFileDate = fileViewHolder.getTextFileDate();
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        textFileDate.setText(simpleDateFormat.format(date));

        ImageView imageFileIcon = fileViewHolder.getImageFileIcon();
        imageFileIcon.setScaleType(ImageView.ScaleType.CENTER);

        if (file.isDirectory())
        {
            fileViewHolder.getTextFileSize().setVisibility(View.GONE);
            fileViewHolder.getButtonFileMenu().setVisibility(View.GONE);

            imageFileIcon.setImageResource(R.drawable.ic_folder_24dp);
        }
        else
        {
            TextView textFileSize = fileViewHolder.getTextFileSize();
            textFileSize.setVisibility(View.VISIBLE);
            String fileSize = Formatter.formatShortFileSize(context, file.length());
            textFileSize.setText(fileSize);

            ImageButton buttonFileMenu = fileViewHolder.getButtonFileMenu();
            buttonFileMenu.setVisibility(View.VISIBLE);

            if (FileTypes.isImage(file))
            {
                imageFileIcon.setImageResource(R.drawable.ic_image_24dp);
                new ImageAsyncTask().execute(imageFileIcon, file);
            }
            else if (FileTypes.isVideo(file))
            {
                imageFileIcon.setImageResource(R.drawable.ic_video_24dp);
                new VideoAsyncTask().execute(imageFileIcon, file);
            }
            else if (FileTypes.isAudio(file))
            {
                imageFileIcon.setImageResource(R.drawable.ic_audio_24dp);
            }
            else
            {
                imageFileIcon.setImageResource(R.drawable.ic_file_24dp);
            }
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

    private class ImageAsyncTask extends AsyncTask<Object, Void, Bitmap>
    {
        private ImageView imageFileIcon;

        @Override
        protected Bitmap doInBackground(Object... objects)
        {
            imageFileIcon = (ImageView) objects[0];
            File file = (File) objects[1];

            int size = 128;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

            return ThumbnailUtils.extractThumbnail(bitmap, size, size);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageFileIcon.setImageBitmap(bitmap);
            imageFileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private class VideoAsyncTask extends AsyncTask<Object, Void, Bitmap>
    {
        private ImageView imageFileIcon;

        @Override
        protected Bitmap doInBackground(Object... objects)
        {
            imageFileIcon = (ImageView) objects[0];
            File file = (File) objects[1];
            String path = file.getAbsolutePath();

            return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageFileIcon.setImageBitmap(bitmap);
            imageFileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
