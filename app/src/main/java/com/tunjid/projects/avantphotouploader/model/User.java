package com.tunjid.projects.avantphotouploader.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A MF5 User
 */

@NoArgsConstructor
@Getter
@Setter

public class User extends BaseModel {

    String firstName;
    String lastName;
    String userName;
    @Getter(AccessLevel.NONE)
    String profilePicture;
    String link;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        return id.equals(user.id);

    }

    public String getPlainUserName() {
        return userName;
    }

    public String getUserName() {
        return "@" + userName;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();

        if (firstName != null) {
            sb.append(firstName);
        }
        if (lastName != null) {
            sb.append(" ");
            sb.append(lastName);
        }

        return sb.toString();
    }

    public String getProfilePicture() {
        return profilePicture != null
                ? profilePicture
                : "https://d247mdk325wmjc.cloudfront.net/assets/v3/press/kit/al-goldstein@2x-409c79bb8d2d82f2c00c390910a629a8.jpg";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    protected User(Parcel in) {
        super(in);
        firstName = in.readString();
        lastName = in.readString();
        userName = in.readString();
        profilePicture = in.readString();
        link = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(userName);
        dest.writeString(profilePicture);
        dest.writeString(link);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}