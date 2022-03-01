package com.khushal.onlinemusicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khushal.onlinemusicapp.Model.UploadSong;
import com.khushal.onlinemusicapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShowSongsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<UploadSong> mUpload;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    MediaPlayer mediaPlayer;
    SongsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);

        recyclerView=(RecyclerView) findViewById(R.id.recyclerView);
        progressBar=(ProgressBar)findViewById(R.id.progressBarShowSongs);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUpload=new ArrayList<>();

        adapter=new SongsAdapter(ShowSongsActivity.this,mUpload);
        recyclerView.setAdapter(adapter);

        databaseReference= FirebaseDatabase.getInstance().getReference("Songs");
        valueEventListener=databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUpload.clear();
                for (DataSnapshot dss:dataSnapshot.getChildren())
                {
                    UploadSong uploadSong=dss.getValue(UploadSong.class);
                    uploadSong.setMkey(dss.getKey());
                    mUpload.add(uploadSong);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),""+databaseError.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
    }

    public void playSong(List<UploadSong> arrayListSongs, final int adapterPosition) throws IOException
    {
        UploadSong uploadSong=arrayListSongs.get(adapterPosition);
        if (mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }

        mediaPlayer=new MediaPlayer();

        mediaPlayer.setDataSource(uploadSong.getSongLink());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mp.start();
            }
        });
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                mediaPlayer.stop();

                mediaPlayer.release();
               /* try {
                    Random rand = new Random();
                    final int adapterPosition =rand.nextInt((mUpload.size() - 1) - 0 + 1) + 0;
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });
    }
}