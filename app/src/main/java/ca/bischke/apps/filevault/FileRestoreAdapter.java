package ca.bischke.apps.filevault;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileRestoreAdapter extends RecyclerView.Adapter<FileCloudViewHolder>
{
    private Context context;
    private List<DataSnapshot> fileList;
    private FileListener fileListener;
    private StorageReference userReference;
    private FileManager fileManager;

    public FileRestoreAdapter(Context context, List<DataSnapshot> fileList, FileListener fileListener, StorageReference userReference)
    {
        this.context = context;
        this.fileList = fileList;
        this.fileListener = fileListener;
        this.userReference = userReference;
        fileManager = new FileManager();
    }

    @NonNull
    @Override
    public FileCloudViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_list_cloud, viewGroup, false);
        return new FileCloudViewHolder(view, fileListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileCloudViewHolder fileViewHolder, int i)
    {
        DataSnapshot dataSnapshot = fileList.get(i);

        String reference = dataSnapshot.getValue().toString();
        String[] parts = reference.split("/");

        File directory = new File(fileManager.getVaultFilesDirectory() + File.separator + parts[0]);
        final File file = new File(directory + File.separator + parts[1]);

        TextView textFileName = fileViewHolder.getTextFileName();
        textFileName.setText(file.getName());

        ImageView imageFileIcon = fileViewHolder.getImageFileIcon();
        imageFileIcon.setScaleType(ImageView.ScaleType.CENTER);

        if (FileTypes.isImage(file))
        {
            imageFileIcon.setImageResource(R.drawable.ic_image_24dp);
        }
        else if (FileTypes.isVideo(file))
        {
            imageFileIcon.setImageResource(R.drawable.ic_video_24dp);
        }
        else if (FileTypes.isAudio(file))
        {
            imageFileIcon.setImageResource(R.drawable.ic_audio_24dp);
        }
        else
        {
            imageFileIcon.setImageResource(R.drawable.ic_file_24dp);
        }

        final TextView textFileDate = fileViewHolder.getTextFileDate();
        final TextView textFileSize = fileViewHolder.getTextFileSize();

        final ImageButton buttonFileBackup = fileViewHolder.getButtonFileBackup();
        buttonFileBackup.setEnabled(true);

        final StorageReference fileReference = userReference.child(reference);

        // Checks if File already exists
        fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
        {
            @Override
            public void onSuccess(StorageMetadata storageMetadata)
            {
                if (file.exists())
                {
                    if (storageMetadata.getSizeBytes() == file.length())
                    {
                        buttonFileBackup.setImageResource(R.drawable.ic_cloud_done_24dp);
                        buttonFileBackup.setEnabled(false);
                    }
                    else
                    {
                        if (storageMetadata.getCreationTimeMillis() > file.lastModified())
                        {
                            buttonFileBackup.setImageResource(R.drawable.ic_cloud_download_24dp);
                        }
                        else if (storageMetadata.getCreationTimeMillis() < file.lastModified())
                        {
                            buttonFileBackup.setImageResource(R.drawable.ic_cloud_download_red_24dp);
                        }
                        else
                        {
                            buttonFileBackup.setImageResource(R.drawable.ic_cloud_done_24dp);
                            buttonFileBackup.setEnabled(false);
                        }
                    }
                }
                else
                {
                    buttonFileBackup.setImageResource(R.drawable.ic_cloud_download_24dp);
                }

                Date date = new Date(storageMetadata.getCreationTimeMillis());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                textFileDate.setText(simpleDateFormat.format(date));

                String fileSize = Formatter.formatShortFileSize(context, storageMetadata.getSizeBytes());
                textFileSize.setText(fileSize);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                buttonFileBackup.setImageResource(R.drawable.ic_cloud_upload_24dp);
            }
        });
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

    public DataSnapshot getDataFromPosition(int position)
    {
        return fileList.get(position);
    }
}
