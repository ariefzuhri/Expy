package com.xdev.expy.data;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.google.firebase.firestore.CollectionReference;
import com.xdev.expy.data.source.local.LocalDataSource;
import com.xdev.expy.data.source.local.entity.ProductWithReminders;
import com.xdev.expy.data.source.local.entity.ReminderEntity;
import com.xdev.expy.data.source.remote.ApiResponse;
import com.xdev.expy.data.source.remote.RemoteDataSource;
import com.xdev.expy.data.source.local.entity.ProductEntity;
import com.xdev.expy.data.source.remote.response.ProductResponse;
import com.xdev.expy.data.source.remote.response.ReminderResponse;
import com.xdev.expy.utils.AppExecutors;
import com.xdev.expy.vo.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.xdev.expy.utils.AppUtils.isNetworkAvailable;

public class MainRepository implements MainDataSource {

    private volatile static MainRepository INSTANCE = null;
    private final RemoteDataSource remoteDataSource;
    private final LocalDataSource localDataSource;
    private final AppExecutors appExecutors;

    private MainRepository(@NonNull RemoteDataSource remoteDataSource, @NonNull LocalDataSource localDataSource, AppExecutors appExecutors) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
        this.appExecutors = appExecutors;
    }

    public static MainRepository getInstance(RemoteDataSource remoteData, LocalDataSource localDataSource, AppExecutors appExecutors) {
        if (INSTANCE == null){
            synchronized (MainRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MainRepository(remoteData, localDataSource, appExecutors);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public LiveData<Resource<PagedList<ProductWithReminders>>> getProducts(boolean isExpired, boolean reFetch) {
        return new NetworkBoundResource<PagedList<ProductWithReminders>, List<ProductResponse>>(appExecutors){
            @Override
            protected LiveData<PagedList<ProductWithReminders>> loadFromDB() {
                PagedList.Config config = new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setInitialLoadSizeHint(10)
                        .setPageSize(10)
                        .build();
                return new LivePagedListBuilder<>(localDataSource.getProducts(isExpired), config).build();
            }

            @Override
            protected Boolean shouldFetch(PagedList<ProductWithReminders> data) {
                return reFetch || isNetworkAvailable();
            }

            @Override
            protected LiveData<ApiResponse<List<ProductResponse>>> createCall() {
                return remoteDataSource.getProducts();
            }

            @Override
            protected void saveCallResult(List<ProductResponse> data) {
                ArrayList<ProductEntity> productList = new ArrayList<>();
                ArrayList<ReminderEntity> reminderList = new ArrayList<>();
                for (ProductResponse productResponse : data) {
                    ProductEntity product = new ProductEntity(productResponse.getId(),
                            productResponse.getName(),
                            productResponse.getExpiryDate(),
                            productResponse.isOpened(),
                            productResponse.getOpenedDate(),
                            productResponse.getPao(),
                            new ArrayList<>());
                    productList.add(product);

                    for (ReminderResponse reminderResponse : productResponse.getReminders()) {
                        ReminderEntity reminder = new ReminderEntity();
                        reminder.setProductId(productResponse.getId());
                        reminder.setTimestamp(reminderResponse.getTimestamp());
                        reminderList.add(reminder);
                    }
                }
                localDataSource.insertProductsAndReminders(productList, reminderList);
            }
        }.asLiveData();
    }

    @Override
    public LiveData<ApiResponse<Boolean>> insertProduct(ProductEntity product) {
        return remoteDataSource.insertProduct(product);
    }

    @Override
    public LiveData<ApiResponse<Boolean>> updateProduct(ProductEntity product) {
        return remoteDataSource.updateProduct(product);
    }

    @Override
    public LiveData<ApiResponse<Boolean>> deleteProduct(ProductEntity product) {
        return remoteDataSource.deleteProduct(product);
    }

    @Override
    public LiveData<ApiResponse<String>> uploadImage(Context context, Uri uriPath, String storagePath, String fileName) {
        return remoteDataSource.uploadImage(context, uriPath, storagePath, fileName);
    }

    @Override
    public CollectionReference getProductsReference() {
        return remoteDataSource.getProductsReference();
    }

    @Override
    public void setProductsReference(String userId) {
        remoteDataSource.setProductsReference(userId);
    }
}
