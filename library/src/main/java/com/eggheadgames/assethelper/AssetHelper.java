package com.eggheadgames.assethelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

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
     * @param assetFolder name of folder where file is located
     * @param fileName   a file name without version and file extension.
     *                       e.g. if you have an asset file data/testdatabase_15.sqlite
     *                       then you should specify testdatabase as a fileName
     * @return AssetHelperStatus
     * @throws RuntimeException in case if specified fileName is empty,
     *                          or assets with specified name not found,
     *                          or file was not written to the filesystem
     */
    public AssetHelperStatus copyIfNew(String assetFolder, String fileName) throws RuntimeException {
        return loadDatabaseToStorage(assetFolder, fileName).getStatus();
    }

    /**
     * Loads an asset to the file system.
     * Path to the file will be returned via a callback
     * <p>
     * P.S. The file will be stored by the following path:
     * context.getFilesDir() + File.separator + fileName + ".yyy"
     *
     * @param fileName   a file name without version and file extension.
     *                   e.g. if you have an asset file data/testdatabase_15.sqlite
     *                   then you should specify testdatabase as a fileName
     * @param fileFolder name of folder where file is located
     * @param listener   will notify about the status
     * @throws RuntimeException in case if specified fileName is empty,
     *                          or assets with specified name not found,
     *                          or file was not written to the filesystem
     */
    public void copyFileToStorageAsync(final String fileFolder, final String fileName,
                                        final IAssetHelperStorageListener listener) throws RuntimeException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final LoadFileToStorageResult result = loadDatabaseToStorage(fileFolder, fileName);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onLoadedToStorage(result.getName(), result.getStatus());
                    }
                });
            }
        }).start();
    }

    /**
     * Loads an asset to the file system.
     * Path to the file will be returned via a callback
     * <p>
     * P.S. The file will be stored by the following path:
     * context.getFilesDir() + File.separator + fileName + ".yyy"
     *
     * @param fileName   a file name without version and file extension.
     *                       e.g. if you have an asset file data/testdatabase_15.sqlite
     *                       then you should specify testdatabase as a fileName
     * @param assetFolder name of folder where file is located
     * @throws RuntimeException in case if specified fileName is empty,
     *                          or assets with specified name not found,
     *                          or file was not written to the filesystem
     */
    private LoadFileToStorageResult loadDatabaseToStorage(String assetFolder, String fileName) throws RuntimeException {
        mOsUtil.clearCache();

        if (mOsUtil.isEmpty(fileName)) {
            throw new RuntimeException("The file name is empty");
        }

        if (!mOsUtil.isDatabaseAssetExists(mContext, assetFolder, fileName)) {
            throw new RuntimeException("An asset for requested file doesn't exist");
        }

        String extension = mOsUtil.getAssetFileExtension(mContext, assetFolder, fileName);
        if (mOsUtil.isEmpty(extension)) {
            throw new RuntimeException("Extension for the " + fileName + " is empty");
        }

        String destinationFilePath = mOsUtil.generateFilePath(mContext, fileName, extension);
        if (mOsUtil.isEmpty(destinationFilePath)) {
            throw new RuntimeException("Can't generate destination file path");
        }

        Integer currentDbVersion = mOsUtil.getCurrentDbVersion(mContext, fileName);
        int assetsDbVersion = mOsUtil.getAssetsDbVersion(mContext, assetFolder, fileName);

        boolean isVersionAvailable = currentDbVersion == null || assetsDbVersion > currentDbVersion;
        if (isVersionAvailable) {
            String name = mOsUtil.loadDatabaseToLocalStorage(mContext, assetFolder, fileName, destinationFilePath);
            if (mOsUtil.isEmpty(name)) {
                throw new RuntimeException("Can't find copied file");
            }
            mOsUtil.storeDatabaseVersion(mContext, assetsDbVersion, name);
            return new LoadFileToStorageResult(name, currentDbVersion == null ? AssetHelperStatus.INSTALLED : AssetHelperStatus.UPDATED);
        } else {
            //do not update
            String name = new File(destinationFilePath).getName();
            return new LoadFileToStorageResult(name, AssetHelperStatus.IGNORED);
        }
    }
}
