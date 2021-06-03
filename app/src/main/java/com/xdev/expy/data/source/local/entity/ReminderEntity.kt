package com.xdev.expy.data.source.local.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Entity(tableName = "reminderEntities",
    foreignKeys = [ForeignKey(entity = ProductEntity::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["productId"])]
)
@Parcelize
data class ReminderEntity (
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "productId")
    @Exclude var productId: String = "",

    @ColumnInfo(name = "timestamp")
    var timestamp: Timestamp = Timestamp(0, 0)
) : Parcelable