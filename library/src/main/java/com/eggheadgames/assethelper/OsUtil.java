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

    private static final String VERSION_PATTERN = "_\\d+\\.";
    private String cachedAssetPath;

    public String loadFileToLocalStorage(Context context, String assetFolder, String fileName, String extension, String destinationFilePath) {
        String assetFilePath = findAsset(context, assetFolder, fileName, extension);

        File file = new File(destinationFilePath);
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                throw new RuntimeException("Can not remove old file");
            }
        }
        try {
            InputStream is = context.getAssets().open(assetFilePath);
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

        return destinationFilePath;
    }

    public String generateFilePath(String destinationFolder, String fileName, String extension) {
        String suffix = isEmpty(extension) ? "" : ("." + extension);
        return destinationFolder + File.separator + fileName + suffix;
    }

    public Integer getCurrentFileVersion(Context context, String fileName) {
        int currentVersion = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.PREFERENCES_FILE_VERSION + fileName, -1);
        return currentVersion == -1 ? null : currentVersion;
    }

    public int getAssetsFileVersion(Context context, String assetFolder, String fileName, String extension) {
        String fileAsset = findAsset(context, assetFolder, fileName, extension);
        Pattern pattern = Pattern.compile(VERSION_PATTERN);
        Matcher matcher = pattern.matcher(fileAsset);

        if (matcher.find()) {
            String version = matcher.group().substring(1, matcher.group().indexOf('.'));
            return Integer.parseInt(version);
        }
        return 0;
    }

    public void storeFileVersion(Context context, int version, String fileName) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PREFERENCES_FILE_VERSION + fileName, version).apply();
    }

    public boolean isEmpty(String string) {
        return TextUtils.isEmpty(string);
    }

    /**
     * expected asset name <fileName>_xx.yyy or <fileName>.yyy
     */
    public boolean isFileAssetExists(Context context, String assetFolder, String fileName, String extension) {
        return !TextUtils.isEmpty(findAsset(context, assetFolder, fileName, extension));
    }

    public void clearCache() {
        cachedAssetPath = null;
    }

    private String findAsset(Context context, String path, String fileName, String extension) {
        if (!TextUtils.isEmpty(cachedAssetPath)) {
            return cachedAssetPath;
        } else {
            try {
                String[] list;
                list = context.getAssets().list(path);
                if (list.length > 0) {
                    for (String file : list) {
                        if (!TextUtils.isEmpty(file) &&
                                (file.matches(fileName + VERSION_PATTERN + extension) || file.matches(fileName + "." + extension))) {
                            cachedAssetPath = path + File.separator + file;
                            return cachedAssetPath;
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
