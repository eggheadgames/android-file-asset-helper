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
    public void checkFile_NameWithExtension_ShouldReturnCorrectVersions() {
        int version = osUtil.getAssetsFileVersion("name_2.ext");
        Assert.assertEquals(2, version);
    }

    @Test
    public void checkFile_NameWithoutExtension_ShouldReturnCorrectVersions() {
        int version = osUtil.getAssetsFileVersion("name_24");
        Assert.assertEquals(24, version);
    }

    @Test
    public void getVersion_OnlyName_ShouldReturn0() {
        int version = osUtil.getAssetsFileVersion("name");
        Assert.assertEquals(0, version);
    }

    @Test
    public void checkVersion_NameWithExtension_ShouldReturn0() {
        int version = osUtil.getAssetsFileVersion("name.ex");
        Assert.assertEquals(0, version);
    }

    @Test
    public void checkFiltration_ShouldReturnCorrectFile() {
        String[] list = new String[3]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "f.extension";
        list[2] = "file1.ext1";

        String neededAsset = osUtil.findNeededAssetFile(list, "folder1", "file1", "ext1");
        Assert.assertEquals("folder1" + File.separator + "file1.ext1", neededAsset);
    }

    @Test
    public void checkFiltration_ShouldReturnFileWithVersionNumber() {
        String[] list = new String[3]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "data_28.db";
        list[2] = "fileNew";

        String neededAsset = osUtil.findNeededAssetFile(list, "databases", "data", "db");
        Assert.assertEquals("databases" + File.separator + "data_28.db", neededAsset);
    }

    @Test
    public void checkFiltration_ShouldReturnFileWithVersionNumberAndLongExtension() {
        String[] list = new String[2]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "volume_2.realmext";

        String neededAsset = osUtil.findNeededAssetFile(list, "data", "volume", "realmext");
        Assert.assertEquals("data" + File.separator + "volume_2.realmext", neededAsset);
    }

    @Test
    public void checkFiltrationWithoutExtension_ShouldReturnCorrectFile() {
        String[] list = new String[3]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "file1.ext1";
        list[2] = "fileNew";

        String neededAsset = osUtil.findNeededAssetFile(list, "folderFolder", "fileNew", null);
        Assert.assertEquals("folderFolder" + File.separator + "fileNew", neededAsset);
    }

    @Test
    public void checkFiltrationWithVersionAndWithoutExtension_ShouldReturnCorrectFile() {
        String[] list = new String[3]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "file1.ext1";
        list[2] = "fileNew_44";

        String neededAsset = osUtil.findNeededAssetFile(list, "folderFolder", "fileNew", null);
        Assert.assertEquals("folderFolder" + File.separator + "fileNew_44", neededAsset);
    }

    @Test
    public void checkFiltration_ShouldReturnNull() {
        String[] list = new String[2]; // available file on asset
        list[0] = "f.qwer";
        list[1] = "file_3";

        String neededAsset = osUtil.findNeededAssetFile(list, "folder1", "file2", null);
        Assert.assertNull(neededAsset);
    }
}
