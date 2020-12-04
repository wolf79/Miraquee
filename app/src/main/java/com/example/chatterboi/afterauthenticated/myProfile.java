package com.example.chatterboi.afterauthenticated;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaDrm;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatterboi.Preferences;
import com.example.chatterboi.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import id.zelory.compressor.Compressor;

public class myProfile extends AppCompatActivity {
    TextView name;
    CircularImageView profilepic;
    Preferences pref;
    Uri downloadUri;
    StorageReference ref;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String uid;
    TextView groupCount, postCount;
    Compressor compressedFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        name = findViewById(R.id.userName);
        profilepic = findViewById(R.id.profilepic);
        pref = new Preferences(getApplicationContext());
        name.setText(pref.getData("usernameAdded"));
        groupCount = findViewById(R.id.groupCount);
        postCount = findViewById(R.id.postCount);

        mAuth =  FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery,1000);
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        getGroupCount();
        getPostsCount();




        ref = FirebaseStorage.getInstance().getReference().child("Profile Photos").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        List<File> files = new ArrayList<>(Arrays.asList(getCacheDir().listFiles()));
        for(File file : files){
            if(file.getName().equals(uid + ".jpg")){
                Toast.makeText(getApplicationContext(), "Uri loaded through file", Toast.LENGTH_SHORT).show();
                Picasso
                        .get() // if file found then load it
                        .load(file)
                        .into(profilepic);
                return;
            }
        }

        File file = new File(getCacheDir() + File.separator + uid + ".jpg");
        if(!file.exists()){
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Toast.makeText(getApplicationContext(), "Uri Downloaded through OnCreate Activity !", Toast.LENGTH_SHORT).show();
                    Picasso picasso = Picasso.get();
                    picasso.setIndicatorsEnabled(true);
                    picasso.load(uri).into(profilepic);
                    loadImage(uri);
                }
            });
            return;
        }
    }

    private void getPostsCount() {
        db.collection("All Users")
                .document(uid)
                .collection("No Of Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int postCoun = 0 ;
                for(QueryDocumentSnapshot ignored : value){
                    postCoun = postCoun +1;
                    postCount.setText(Integer.toString(postCoun));
                }
            }
        });
    }

    private void getGroupCount() {
        db.collection("All Users").document(uid)
                .collection("No of Groups created").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int groupCoun = 0 ;
                for(QueryDocumentSnapshot snap: value){
                    groupCoun++;
                    groupCount.setText(Integer.toString(groupCoun));
                }
            }
        });
    }


    public void loadImage(final Uri uri){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Log.d("Checkere", "stuck at file");
                File file = new File(getCacheDir() + File.separator + uid + ".jpg");
                Log.d("Checkere", "file" + file);
                try {
                    Picasso picasso = Picasso.get();
                    Bitmap bitmap = picasso.load(uri).get();
                    Log.d("Checkere", "bitmap working");
                    FileOutputStream fOut = new FileOutputStream(file);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 20, out);
                    out.writeTo(fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    Log.d("Checkere", e.getMessage());
                    e.printStackTrace();
                }
            }};
        thread.start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            if(imageUri!= null) {
                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), System.currentTimeMillis() + ".jpg" )))
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(200, 200)
                        .start(myProfile.this);
            }
        }
        else if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            Uri imageUri = UCrop.getOutput(data);
            final ProgressDialog dialog = new ProgressDialog(myProfile.this);
            dialog.setMessage("Uploading Image...");
            dialog.show();
            profilepic.setImageURI(imageUri);
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Picasso picasso = Picasso.get();
                            picasso.setIndicatorsEnabled(true);
                            picasso.load(uri).into(profilepic);
                            downloadUri = uri;
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Image Uploaded!",
                                    Toast.LENGTH_SHORT).show();
                            loadImage(uri);
                        }
                    }); }
            });
        }
    }
}