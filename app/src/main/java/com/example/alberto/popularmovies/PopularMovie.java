package com.example.alberto.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alberto on 04/03/2018.
 */

public class PopularMovie implements Parcelable{
    private int id;
    private String title;
    private String releaseDate;
    private String poster;
    private double voteAverage;
    private String plot;
    private boolean favorite = false;

    PopularMovie (int id, String title, String releaseDate, String poster,
                         double voteAverage, String plot) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.poster = poster;
        this.voteAverage = voteAverage;
        this.plot = plot;
    }

    public int getId() {
        return id;
    }

    public String getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPlot() {
        return plot;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(poster);
        parcel.writeString(title);
        parcel.writeDouble(voteAverage);
        parcel.writeString(releaseDate);
        parcel.writeString(plot);
        parcel.writeByte((byte) (favorite ? 1:0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<PopularMovie>(){

        @Override
        public PopularMovie createFromParcel(Parcel parcel) {
            return new PopularMovie(parcel);
        }

        @Override
        public PopularMovie[] newArray(int i) {
            return new PopularMovie[i];
        }
    };

    private PopularMovie (Parcel parcel) {
        id = parcel.readInt();
        poster = parcel.readString();
        title = parcel.readString();
        voteAverage = parcel.readDouble();
        releaseDate = parcel.readString();
        plot = parcel.readString();
        favorite = parcel.readByte() != 0;
    }
}
