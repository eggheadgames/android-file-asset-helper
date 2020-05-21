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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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

        when(osUtil.isEmpty(Mockito.<String>any())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                String string = (String) invocation.getArguments()[0];
                return string == null || string.isEmpty();
            }
        });
        when(osUtil.isFileAssetExists(any(Context.class), anyString(), anyString(), anyString())).thenReturn(true);

        when(osUtil.loadFileToLocalStorage(any(Context.class), anyString(),
                anyString(), anyString(), anyString())).thenReturn(TestConstants.FILE_PATH);

        when(osUtil.generateFilePath(anyString(), anyString())).thenReturn(TestConstants.FILE_PATH);

        assetHelper.mOsUtil = osUtil;
    }

    @Test(expected = RuntimeException.class)
    public void onEmptyFileName_shouldThrowRuntimeException() throws RuntimeException {
        assetHelper.copyIfNew(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void onMissingFileAsset_shouldThrowRuntimeException() throws RuntimeException {
        when(osUtil.isFileAssetExists(context, TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME, TestConstants.FILE_EXTENSION)).thenReturn(false);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
    }

    @Test
    public void onFreshInstall_fileVersionShouldBeStored() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(null);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);

        verify(osUtil, Mockito.times(1)).storeFileVersion(context, 2, TestConstants.FILE_PATH);
    }

    @Test
    public void onFileUpdate_fileVersionShouldBeStored() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(1);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);

        verify(osUtil, Mockito.times(1)).storeFileVersion(context, 2, TestConstants.FILE_PATH);
    }

    @Test
    public void onFreshAppInstall_fileShouldBeLoadedToInternalStorage() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(null);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);

        verify(osUtil, Mockito.times(1))
                .loadFileToLocalStorage(any(Context.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void onFileUpdate_fileShouldBeLoadedToInternalStorage() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(1);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);

        verify(osUtil, Mockito.times(1))
                .loadFileToLocalStorage(any(Context.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void onSameFileVersionArrived_fileShouldNotBeLoadedToInternalStorage() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(2);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
        verify(osUtil, Mockito.never())
                .loadFileToLocalStorage(any(Context.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void onFreshAppInstall_relevantCallbackShouldBeTriggered() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(null);

        CopyFileToStorageResult result = assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
        Assert.assertEquals(AssetHelperStatus.INSTALLED, result.getStatus());
    }

    @Test
    public void onFileUpdate_relevantCallbackShouldBeTriggered() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(1);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        CopyFileToStorageResult result = assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
        Assert.assertEquals(AssetHelperStatus.UPDATED, result.getStatus());
    }

    @Test
    public void onSameFileVersionArrived_relevantCallbackShouldBeTriggered() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(2);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        CopyFileToStorageResult result = assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
        Assert.assertEquals(AssetHelperStatus.IGNORED, result.getStatus());
    }

    @Test
    public void onFileUpdate_versionShouldBeSetForCorrectFile() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(1);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
        verify(osUtil, Mockito.times(1)).storeFileVersion(context, 2, TestConstants.FILE_PATH);
    }

    @Test(expected = RuntimeException.class)
    public void onLoadFileToStorageFileNotFound_exceptionShouldBeThrown() {
        when(osUtil.getCurrentFileVersion(any(Context.class), anyString())).thenReturn(1);
        when(osUtil.getAssetsFileVersion(any(Context.class), anyString(), anyString(), anyString())).thenReturn(2);

        when(osUtil.loadFileToLocalStorage(any(Context.class), anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        assetHelper.copyIfNew(TestConstants.ASSET_FOLDER, TestConstants.FILE_NAME_WITH_EXTENSION, TestConstants.DESTINATION_FOLDER_PATH);
    }
}