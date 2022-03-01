package com.khushal.onlinemusicapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebStorage;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.khushal.onlinemusicapp.Model.UploadSong;
import com.khushal.onlinemusicapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    AppCompatEditText editTextTitle;
    TextView textViewImage;
    ProgressBar progressBar;
    Uri audioUri;
    StorageReference mStorageRef;
    DatabaseReference referenceSongs;
    StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle=(AppCompatEditText)findViewById(R.id.songtitle);
        textViewImage=(TextView)findViewById(R.id.txtViewSongFileSelected);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        referenceSongs= FirebaseDatabase.getInstance().getReference().child("Songs");
        mStorageRef= FirebaseStorage.getInstance().getReference().child("Songs");




    }

    public void openAudioFile (View v){
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101 && resultCode==RESULT_OK && data.getData()!=null){

            audioUri=data.getData();
            String fileName= getFileName(audioUri);
            textViewImage.setText(fileName);


        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if(uri.getScheme().equals("contents")){
            Cursor cursor=getContentResolver().query(uri,null,null,null,null);

            try {
                if (cursor!=null && cursor.moveToFirst())
                {
                    result=cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }

        }

        if (result==null){
            result=uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut!=-1){
                result=result.substring(cut+1);

            }
        }
        return result;
    }

    public void uploadAudioToFirebase(View v){
        if (textViewImage.getText().toString().equals("No Files Selected")){
            Toast.makeText(getApplicationContext(),"Please Select a File",Toast.LENGTH_LONG).show();

        }
        else{
            if (mUploadTask!=null && mUploadTask.isInProgress())
            {
                Toast.makeText(getApplicationContext(), "Song Upload is already in Progress", Toast.LENGTH_LONG).show();
            }else
            {
                uploadFile();
            }
        }
    }

    private void uploadFile() {
        if (audioUri!=null)
        {
            String durationTxt;
            Toast.makeText(getApplicationContext(),"Uploading File Please wait...",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.VISIBLE);
            StorageReference storageReference=mStorageRef.child(System.currentTimeMillis()+"."+getFileExtention(audioUri));

            int durationInMillis=findSongDuration(audioUri);
            if (durationInMillis==0){
                durationTxt="NA";

            }
            durationTxt= getDurationFromMilli(durationInMillis);

            String finalDurationTxt = durationTxt;
            mUploadTask=storageReference.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UploadSong uploadSong= new UploadSong(editTextTitle.getText().toString(),
                                            finalDurationTxt,uri.toString());

                                    String uploadId= referenceSongs.push().getKey();
                                    referenceSongs.child(uploadId).setValue(uploadSong);
                                }
                            });

                        }
                    })
            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0 * taskSnapshot.getBytesTransferred()
                            /taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                }
            });
            
        }
        else {
            Toast.makeText(getApplicationContext(),"No Files Selected to Upload",Toast.LENGTH_LONG).show();
        }
    }

    private String getDurationFromMilli(int durationInMillis) {
        Date date = new Date(durationInMillis);
        SimpleDateFormat simple = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime= simple.format(date);
        return myTime;
    }

    private int findSongDuration(Uri audioUri) {
        int timeInMillsec=0;
        try {
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(this,audioUri);
            String time=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            timeInMillsec=Integer.parseInt(time);
            retriever.release();
            return timeInMillsec;
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtention(Uri audioUri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));

    }

    public void openSongsActivity(View v)
    {
        Intent i = new Intent(MainActivity.this,ShowSongsActivity.class);
        startActivity(i);
    }
}