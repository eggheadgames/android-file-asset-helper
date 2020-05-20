package com.eggheadgames.assethelper;

public class CopyFileToStorageResult {

    CopyFileToStorageResult(String pathToFile, AssetHelperStatus status) {
        this.pathToFile = pathToFile;
        this.status = status;
    }

    private String pathToFile;
    private AssetHelperStatus status;


    public AssetHelperStatus getStatus() {
        return status;
    }

    public String getPathToFile() {
        return pathToFile;
    }
}
