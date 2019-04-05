package ca.bischke.apps.filevault;

public interface FileListener
{
    void onFileClick(int position);

    void onFileLongClick(int position);

    void onMenuClick(int position);
}
