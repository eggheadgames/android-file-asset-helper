package com.eggheadgames.assethelper;

public interface IAssetHelperStorageListener {

    void onLoadedToStorage(String filePath, AssetHelperStatus status);
}
