package com.tunjid.projects.avantphotouploader.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.Setter;

/**
 * BaseModel for all MyFab5 Models
 */
@Getter
@Setter
public class BaseModel implements Parcelable {
    public String id;
    public String _class;


    public BaseModel() {

    }

    protected BaseModel(Parcel in) {
        id = in.readString();
        _class = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(_class);
    }

    public static final Creator<BaseModel> CREATOR = new Creator<BaseModel>() {
        @Override
        public BaseModel createFromParcel(Parcel in) {
            return new BaseModel(in);
        }

        @Override
        public BaseModel[] newArray(int size) {
            return new BaseModel[size];
        }
    };
}