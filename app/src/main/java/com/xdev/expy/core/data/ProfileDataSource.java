package com.xdev.expy.core.data;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.xdev.expy.core.data.source.remote.ApiResponse;

public interface ProfileDataSource {

    LiveData<ApiResponse<String>> uploadImage(Context context, Uri uriPath, String storagePath, String fileName);
}