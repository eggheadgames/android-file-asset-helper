package com.eggheadgames.assethelper;

import android.annotation.SuppressLint;
import android.content.Context;

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
     * Path to the file will be returned via a callback
     * <p>
     * P.S. The file will be stored by the following path:
     * context.getFilesDir() + File.separator + databaseName + ".yyy"
     *
     * @param databaseName   a database name without version and file extension.
     *                       e.g. if you have an asset file data/testdatabase_15.sqlite
     *                       then you should specify testdatabase as a databaseName
     * @param databaseFolder name of folder where database is located
     * @param listener       will notify about the status and return an instance of Realm database if there is no error
     * @throws RuntimeException in case if specified databaseName is empty,
     *                          or assets with specified name not found,
     *                          or file was not written to the filesystem
     */
    public void loadDatabaseToStorage(String databaseFolder, String databaseName, IAssetHelperStorageListener listener) throws RuntimeException {
        mOsUtil.clearCache();

        if (mOsUtil.isEmpty(databaseName)) {
            throw new RuntimeException("The database name is empty");
        }

        if (!mOsUtil.isDatabaseAssetExists(mContext, databaseFolder, databaseName)) {
            throw new RuntimeException("An asset for requested database doesn't exist");
        }

        String extension = mOsUtil.getAssetFileExtension(mContext, databaseFolder, databaseName);
        if (mOsUtil.isEmpty(extension)) {
            throw new RuntimeException("Extension for the " + databaseName + " is empty");
        }

        String destinationFilePath = mOsUtil.generateFilePath(mContext, databaseName, extension);
        if (mOsUtil.isEmpty(destinationFilePath)) {
            throw new RuntimeException("Can't generate destination file path");
        }

        Integer currentDbVersion = mOsUtil.getCurrentDbVersion(mContext, databaseName);
        int assetsDbVersion = mOsUtil.getAssetsDbVersion(mContext, databaseFolder, databaseName);

        boolean isVersionAvailable = currentDbVersion == null || assetsDbVersion > currentDbVersion;
        if (isVersionAvailable) {
            String path = mOsUtil.loadDatabaseToLocalStorage(mContext, databaseFolder, databaseName, destinationFilePath);
            if (mOsUtil.isEmpty(path)) {
                throw new RuntimeException("Can't find copied file");
            }
            mOsUtil.storeDatabaseVersion(mContext, assetsDbVersion, databaseName);
            if (listener != null) {

                AssetHelperStatus status = currentDbVersion == null ? AssetHelperStatus.INSTALLED : AssetHelperStatus.UPDATED;
                listener.onLoadedToStorage(path, status);
            }
        } else {
            //do not update
            if (listener != null) {
                listener.onLoadedToStorage(destinationFilePath, AssetHelperStatus.IGNORED);
            }
        }
    }
}
