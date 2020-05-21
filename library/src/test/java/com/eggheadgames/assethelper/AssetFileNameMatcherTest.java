package com.eggheadgames.assethelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class AssetFileNameMatcherTest {
    private OsUtil osUtil;

    @Before
    public void init() {
        osUtil = new OsUtil();
    }

    @Test
    public void checkFileNameWithExtension_ShouldReturnCorrectVersions() {
        int version = osUtil.getAssetsFileVersion("name_2.ext");
        Assert.assertEquals(2, version);
    }

    @Test
    public void checkFileNameWithoutExtension_ShouldReturnCorrectVersions() {
        int version = osUtil.getAssetsFileVersion("name_24");
        Assert.assertEquals(24, version);
    }

    @Test
    public void checkAssetsFilesFiltration_ShouldReturnCorrectFile() {
        String[] list = new String[4]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "f.extension";
        list[2] = "file1.ext1";
        list[3] = "fileNew";

        String neededAsset = osUtil.findNeededAssetFile(list, "folder1", "file1", "ext1");
        Assert.assertEquals("folder1" + File.separator + "file1.ext1", neededAsset);
    }

    @Test
    public void checkAssetsFilesFiltrationWithoutExtension_ShouldReturnCorrectFile() {
        String[] list = new String[4]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "f.extension";
        list[2] = "file1.ext1";
        list[3] = "fileNew";

        String neededAsset = osUtil.findNeededAssetFile(list, "folderFolder", "fileNew", null);
        Assert.assertEquals("folderFolder" + File.separator + "fileNew", neededAsset);
    }

    @Test
    public void checkAssetsFilesFiltration_ShouldReturnNull() {
        String[] list = new String[4]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "f.extension";
        list[2] = "file";
        list[3] = "fileNew.qwer";

        String neededAsset = osUtil.findNeededAssetFile(list, "folder1", "file2", null);
        Assert.assertNull(neededAsset);
    }
}
