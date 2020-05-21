package com.eggheadgames.assethelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class AssetHelper {
    @SuppressLint("StaticFieldLeak")
    protected static final AssetHelper instance = new AssetHelper();
    protected Context mContext;
    protected OsUtil mOsUtil;

    /**
     * Please consider using Application Context as a @param context
     */
    public static AssetHelper getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        instance.mContext = applicationContext == null ? context : applicationContext;
        instance.mOsUtil = new OsUtil();
        return instance;
    }

    protected AssetHelper() {
    }

    /**
     * Loads an asset to the file system.
     * This method does process in the UI thread. Try to call it from the background thread or use {@link #copyFileToStorageAsync}
     *
     * @param assetFolder       name of folder where file is located
     * @param fileName          a file name with version and file extension.
     *                          e.g. if you have an asset file data/testdatabase_15.sqlite
     *                          then you should specify testdatabase.sqlite as a fileName
     * @param destinationFolder relative path to the folder where file will be stored
     * @return LoadFileToStorageResult
     * @throws RuntimeException in case if specified fileName is empty,
     *                          or assets with specified name not found,
     *                          or file was not written to the filesystem
     */
    public CopyFileToStorageResult copyIfNew(String assetFolder, String fileName, final String destinationFolder) throws RuntimeException {
        return loadFileToStorage(assetFolder, fileName, destinationFolder);
    }

    public CopyFileToStorageResult copyIfNew(String assetFolder, String fileName) throws RuntimeException {
        return copyIfNew(assetFolder, fileName, mContext.getFilesDir().getAbsolutePath());
    }


    /**
     * Loads an asset to the file system.
     * Path to the file will be returned via a callback
     * <p>
     * P.S. The file will be stored by the following path:
     * context.getFilesDir() + File.separator + fileName + ".yyy"
     *
     * @param fileFolder        name of folder where file is located
     * @param fileName          a file name with version and file extension.
     *                          e.g. if you have an asset file data/testdatabase_15.sqlite
     *                          then you should specify testdatabase.sqlite as a fileName
     * @param destinationFolder relative path to the folder where file will be stored
     * @param listener          will notify about the status
     * @throws RuntimeException in case if specified fileName is empty,
     *                          or assets with specified name not found,
     *                          or file was not written to the filesystem
     */
    public void copyFileToStorageAsync(final String fileFolder, final String fileName, final String destinationFolder,
                                       final IAssetHelperStorageListener listener) throws RuntimeException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final CopyFileToStorageResult result = loadFileToStorage(fileFolder, fileName, destinationFolder);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onLoadedToStorage(result.getPathToFile(), result.getStatus());
                    }
                });
            }
        }).start();
    }

    public void copyFileToStorageAsync(final String fileFolder, final String fileName, final IAssetHelperStorageListener listener) throws RuntimeException {
        copyFileToStorageAsync(fileFolder, fileName, mContext.getFilesDir().getAbsolutePath(), listener);
    }

    private CopyFileToStorageResult loadFileToStorage(String assetFolder, String fileName, String destinationFolder) throws RuntimeException {
        mOsUtil.clearCache();

        if (mOsUtil.isEmpty(fileName)) {
            throw new RuntimeException("The file name is empty");
        }

        String name;
        String extension = null;
        int indexOfDot = fileName.lastIndexOf(".");
        if (indexOfDot == -1) {
            name = fileName;
        } else {
            name = fileName.substring(0, indexOfDot);
            extension = fileName.substring(indexOfDot + 1);
        }

        if (!mOsUtil.isFileAssetExists(mContext, assetFolder, name, extension)) {
            throw new RuntimeException("An asset for requested file doesn't exist");
        }

        String destinationFilePath = mOsUtil.generateFilePath(destinationFolder, fileName, extension);
        if (mOsUtil.isEmpty(destinationFilePath)) {
            throw new RuntimeException("Can't generate destination file path");
        }

        Integer currentFileVersion = mOsUtil.getCurrentFileVersion(mContext, fileName);
        int assetsFileVersion = mOsUtil.getAssetsFileVersion(mContext, assetFolder, fileName, extension);

        boolean isVersionAvailable = currentFileVersion == null || assetsFileVersion > currentFileVersion;
        if (isVersionAvailable) {
            String pathToFile = mOsUtil.loadFileToLocalStorage(mContext, assetFolder, fileName, extension, destinationFilePath);
            if (mOsUtil.isEmpty(pathToFile)) {
                throw new RuntimeException("Can't find copied file");
            }
            mOsUtil.storeFileVersion(mContext, assetsFileVersion, fileName);
            return new CopyFileToStorageResult(pathToFile, currentFileVersion == null ? AssetHelperStatus.INSTALLED : AssetHelperStatus.UPDATED);
        } else {
            //do not update
            return new CopyFileToStorageResult(destinationFilePath, AssetHelperStatus.IGNORED);
        }
    }
}
