package com.eggheadgames.assethelper;

public class LoadFileToStorageResult {

    LoadFileToStorageResult(String name, AssetHelperStatus status) {
        this.name = name;
        this.status = status;
    }

    private String name;
    private AssetHelperStatus status;

    public String getName() {
        return name;
    }

    public AssetHelperStatus getStatus() {
        return status;
    }
}
