package com.tunjid.projects.avantphotouploader.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stats for page loaded data
 */
@Getter
@Setter
@NoArgsConstructor

public class PageStats implements Parcelable {
    @SerializedName("offset")
    int apiOffset;
    int limit;
    int dataReceived;
    long numResults;

    public PageStats(int apiOffset, int limit, int dataReceived, long numResults) {
        this.apiOffset = apiOffset;
        this.limit = limit;
        this.dataReceived = dataReceived;
        this.numResults = numResults;
    }

    protected PageStats(Parcel in) {
        apiOffset = in.readInt();
        limit = in.readInt();
        dataReceived = in.readInt();
        numResults = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(apiOffset);
        dest.writeInt(limit);
        dest.writeInt(dataReceived);
        dest.writeLong(numResults);
    }

    public static final Creator<PageStats> CREATOR = new Creator<PageStats>() {
        @Override
        public PageStats createFromParcel(Parcel in) {
            return new PageStats(in);
        }

        @Override
        public PageStats[] newArray(int size) {
            return new PageStats[size];
        }
    };
}
