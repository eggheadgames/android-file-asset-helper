package com.eggheadgames.assethelper;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OsUtil {

    private static final String VERSION_PATTERN = "_\\d+\\.+";
    private String cachedAssetPath;

    public String loadDatabaseToLocalStorage(Context context, String databaseFolder, String databaseName, String destinationFilePath) {
        String asset = findAsset(context, databaseFolder, databaseName);

        File file = new File(destinationFilePath);
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                throw new RuntimeException("Can not remove old database");
            }
        }
        try {
            InputStream is = context.getAssets().open(asset);
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(file);

            int length = 0;
            while ((length = is.read(buffer)) >= 0) {
                fos.write(buffer, 0, length);
            }

            is.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return file.getName();
    }

    public String generateFilePath(Context context, String databaseName, String extension) {
        return context.getFilesDir() + File.separator + databaseName + "." + extension;
    }

    public Integer getCurrentDbVersion(Context context, String databaseName) {
        int currentVersion = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.PREFERENCES_DB_VERSION + databaseName, -1);
        return currentVersion == -1 ? null : currentVersion;
    }

    public int getAssetsDbVersion(Context context, String databaseFolder, String databaseName) {
        String dbAsset = findAsset(context, databaseFolder, databaseName);
        Pattern pattern = Pattern.compile(VERSION_PATTERN);
        Matcher matcher = pattern.matcher(dbAsset);

        if (matcher.find()) {
            String version = matcher.group().substring(1, matcher.group().indexOf('.'));
            return Integer.parseInt(version);
        }
        return 0;
    }

    public void storeDatabaseVersion(Context context, int version, String databaseName) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PREFERENCES_DB_VERSION + databaseName, version).apply();
    }

    public boolean isEmpty(String string) {
        return TextUtils.isEmpty(string);
    }

    /**
     * expected asset name <databaseName>_xx.yyy or <databaseName>.yyy
     */
    public boolean isDatabaseAssetExists(Context context, String databaseFolder, String databaseName) {
        return !TextUtils.isEmpty(findAsset(context, databaseFolder, databaseName));
    }

    public String getAssetFileExtension(Context context, String databaseFolder, String databaseName) {
        String assetFilePath = findAsset(context, databaseFolder, databaseName);
        if (assetFilePath != null) {
            int beginIndex = assetFilePath.lastIndexOf(".");
            if (beginIndex != -1) {
                return assetFilePath.substring(beginIndex + 1);
            }
        }
        return null;
    }

    public void clearCache() {
        cachedAssetPath = null;
    }

    private String findAsset(Context context, String path, String databaseName) {
        if (!TextUtils.isEmpty(cachedAssetPath)) {
            return cachedAssetPath;
        } else {
            try {
                String[] list;
                list = context.getAssets().list(path);
                if (list.length > 0) {
                    for (String file : list) {
                        if (!TextUtils.isEmpty(file) &&
                                (file.matches(databaseName + VERSION_PATTERN) || file.matches(databaseName + "."))) {
                            cachedAssetPath = path + File.separator + file;
                            return path;
                        }
                    }
                }
            } catch (IOException e) {
                return null;
            }
            return null;
        }
    }
}
