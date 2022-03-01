package com.khushal.onlinemusicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khushal.onlinemusicapp.Model.UploadSong;
import com.khushal.onlinemusicapp.R;

import java.io.IOException;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterViewHolder>
{

    Context context;
    List<UploadSong> arrayListSongs;

    public SongsAdapter(Context context,List<UploadSong> arrayListSongs)
    {
        this.context=context;
        this.arrayListSongs=arrayListSongs;

    }


    @Override
    public SongsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.song_item,viewGroup,false);
        return new SongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterViewHolder holder, int i) {
        UploadSong uploadSong=arrayListSongs.get(i);
        holder.titleTxt.setText(uploadSong.getSongTitle());
        holder.durationTxt.setText(uploadSong.getSongDuration());

    }

    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }

    public class SongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView titleTxt, durationTxt;
        public SongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt= (TextView)itemView.findViewById(R.id.song_title);
            durationTxt=(TextView)itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            try {
                ((ShowSongsActivity)context).playSong(arrayListSongs,getAbsoluteAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
