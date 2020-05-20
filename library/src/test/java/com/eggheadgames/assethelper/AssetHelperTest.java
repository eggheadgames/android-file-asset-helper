package com.eggheadgames.assethelper;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AssetHelperTest {

    @Mock
    private Context context;

    @Mock
    private OsUtil osUtil;

    private AssetHelper assetHelper;

    @Before
    public void prepareForTest() {
        assetHelper = spy(new AssetHelper());
        assetHelper.mContext = context;

        Mockito.when(osUtil.isEmpty(Mockito.<String>any())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                String string = (String) invocation.getArguments()[0];
                return string == null || string.isEmpty();
            }
        });
        Mockito.when(osUtil.isDatabaseAssetExists(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Mockito.when(osUtil.loadDatabaseToLocalStorage(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(TestConstants.FILE_NAME);

        Mockito.when(osUtil.generateFilePath(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(TestConstants.FILE_PATH);

        Mockito.when(osUtil.getAssetFileExtension(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(TestConstants.FILE_EXTENSION);

        assetHelper.mOsUtil = osUtil;
    }

    @Test(expected = RuntimeException.class)
    public void onEmptyDatabaseName_shouldThrowRuntimeException() throws RuntimeException {
        assetHelper.copyIfNew(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void onMissingDbAsset_shouldThrowRuntimeException() throws RuntimeException {
        Mockito.when(osUtil.isDatabaseAssetExists(context, TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME)).thenReturn(false);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
    }

    @Test
    public void onFreshInstall_fileVersionShouldBeStored() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(null);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);

        Mockito.verify(osUtil, Mockito.times(1)).storeDatabaseVersion(context, 2, TestConstants.FILE_NAME);
    }

    @Test
    public void onDatabaseUpdate_fileVersionShouldBeStored() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(1);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);

        Mockito.verify(osUtil, Mockito.times(1)).storeDatabaseVersion(context, 2, TestConstants.FILE_NAME);
    }

    @Test
    public void onFreshAppInstall_fileShouldBeLoadedToInternalStorage() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(null);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);

        Mockito.verify(osUtil, Mockito.times(1)).loadDatabaseToLocalStorage(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void onDatabaseUpdate_fileShouldBeLoadedToInternalStorage() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(1);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);

        Mockito.verify(osUtil, Mockito.times(1)).loadDatabaseToLocalStorage(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void onSameDatabaseVersionArrived_fileShouldNotBeLoadedToInternalStorage() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(2);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
        Mockito.verify(osUtil, Mockito.never()).loadDatabaseToLocalStorage(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void onFreshAppInstall_relevantCallbackShouldBeTriggered() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(null);

        CopyFileToStorageResult result = assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
        Assert.assertEquals(AssetHelperStatus.INSTALLED, result.getStatus());
    }

    @Test
    public void onDatabaseUpdate_relevantCallbackShouldBeTriggered() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(1);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        CopyFileToStorageResult result = assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
        Assert.assertEquals(AssetHelperStatus.UPDATED, result.getStatus());
    }

    @Test
    public void onSameDatabaseVersionArrived_relevantCallbackShouldBeTriggered() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(2);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        CopyFileToStorageResult result = assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
        Assert.assertEquals(AssetHelperStatus.IGNORED, result.getStatus());
    }

    @Test
    public void onDatabaseUpdate_versionShouldBeSetForCorrectDatabase() {

        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(1);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
        Mockito.verify(osUtil, Mockito.times(1)).storeDatabaseVersion(context, 2, TestConstants.FILE_NAME);
    }

    @Test(expected = RuntimeException.class)
    public void onLoadDatabaseToStorageFileNotFound_exceptionShouldBeThrown() {
        Mockito.when(osUtil.getCurrentDbVersion(Mockito.any(Context.class), Mockito.anyString())).thenReturn(1);
        Mockito.when(osUtil.getAssetsDbVersion(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString())).thenReturn(2);

        when(osUtil.loadDatabaseToLocalStorage(Mockito.any(Context.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME);
    }
}