package com.example.alberto.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * Created by Alberto on 04/03/2018.
 */

public class MoviePosterAdapter extends RecyclerView.Adapter<MoviePosterAdapter.ViewHolder> {

    private final static String POSTER_LINK = "http://image.tmdb.org/t/p/w780/";

    private ArrayList<PopularMovie> movies;

    private final MoviePosterAdapterOnClickHandler mClickHandler;

    public interface MoviePosterAdapterOnClickHandler {
        void onClick(PopularMovie movieSelected);
    }

    MoviePosterAdapter(MoviePosterAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView posterView;

        ViewHolder(View view) {
            super(view);
            posterView = view.findViewById(R.id.grid_item_poster);
            posterView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            PopularMovie selectedMovie = movies.get(adapterPosition);
            mClickHandler.onClick(selectedMovie);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.grid_item_movie, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PopularMovie popularMovie = movies.get(position);
        String moviePoster = popularMovie.getPoster();
        String posterLink = POSTER_LINK + moviePoster;
        Context context = holder.posterView.getContext();
        int width = Utils.getColumnsWidth(context);
        int height = width/2 + width;
        Picasso.with(context)
                .load(posterLink)
                .resize(width, height)
                .onlyScaleDown()
                .centerCrop()
                .into(holder.posterView);
    }

    @Override
    public int getItemCount() {
        return (movies != null) ? movies.size(): 0;
    }

    public void setMoviesList(ArrayList<PopularMovie> movies){
        if (this.movies == null) {
            this.movies = new ArrayList<>();
        }
        this.movies.clear();
        this.movies.addAll(movies);
        super.notifyDataSetChanged();
    }
}
