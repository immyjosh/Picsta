package com.ijp.app.picsta;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ijp.app.picsta.Common.Common;
import com.ijp.app.picsta.Database.DataSource.RecentRepository;
import com.ijp.app.picsta.Database.LocalDatabase.LocalDatabase;
import com.ijp.app.picsta.Database.LocalDatabase.RecentsDataSource;
import com.ijp.app.picsta.Database.Recents;
import com.ijp.app.picsta.Helper.SaveImageHelper;
import com.ijp.app.picsta.Model.WallpaperItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewWallpaper extends AppCompatActivity {

    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton floatingActionButton,fabDownload;
    CoordinatorLayout rootLayout;
    ImageView imageView;

    FloatingActionMenu mainFloating;
    com.github.clans.fab.FloatingActionButton fbShare;

    //Room Database
    CompositeDisposable compositeDisposable;
    RecentRepository recentRepository;

    //facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1000:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    AlertDialog dialog=new SpotsDialog(ViewWallpaper.this);
                    dialog.show();
                    dialog.setMessage("Please Wait...");

                    String fileName= UUID.randomUUID().toString()+".png";
                    Picasso.with(getBaseContext())
                            .load(Common.selectBackground.getImagelink())
                            .into(new SaveImageHelper(getBaseContext(),dialog,getApplicationContext().getContentResolver(),fileName,"Picsta LiveWalpaper Image"));
                }
                else
                    Toast.makeText(this, "You Need To Accept Permission To Download Image", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private Target target= new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            WallpaperManager wallpaperManager=WallpaperManager.getInstance(getApplicationContext());
            try{
                wallpaperManager.setBitmap(bitmap);
                Snackbar.make(rootLayout,"Wallpaper was set",Snackbar.LENGTH_SHORT).show();
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private Target facebookConvertBitmap=new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto sharePhoto=new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if(shareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content=new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallpaper);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Init Facebook
        callbackManager= CallbackManager.Factory.create();
        shareDialog=new ShareDialog(this);

        //Init RoomDatabase
        compositeDisposable=new CompositeDisposable();
        LocalDatabase database=LocalDatabase.getInstance(this);
        recentRepository=RecentRepository.getInstance(RecentsDataSource.getInstance(database.recentsDAO()));

        //Init
        rootLayout=findViewById(R.id.rootlayout);
        collapsingToolbarLayout=findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.ExpandedAppBar);

        collapsingToolbarLayout.setTitle(Common.CATEGORY_SELECTED);

        imageView=findViewById(R.id.imagethumb);

        Picasso.with(this)
                .load(Common.selectBackground.getImagelink())
                .into(imageView);

        mainFloating=findViewById(R.id.menu);
        fbShare=findViewById(R.id.fb_share);
        fbShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create callback
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(ViewWallpaper.this, "Share Successful", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(ViewWallpaper.this, "Share Cancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(ViewWallpaper.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                //Fetch photo from link and convert to bitmap
                Picasso.with(getBaseContext())
                        .load(Common.selectBackground.getImagelink())
                        .into(facebookConvertBitmap);
            }
        });

        //add to recents
        addToRecents();

        floatingActionButton=findViewById(R.id.fab_wallpaper);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.with(getBaseContext())
                        .load(Common.selectBackground.getImagelink())
                        .into(target);
            }
        });

        fabDownload=findViewById(R.id.fab_download);
        fabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(ViewWallpaper.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1000);
                }
                else
                {
                    AlertDialog dialog=new SpotsDialog(ViewWallpaper.this);
                    dialog.show();
                    dialog.setMessage("Please Wait...");

                    String fileName= UUID.randomUUID().toString()+".png";
                    Picasso.with(getBaseContext())
                            .load(Common.selectBackground.getImagelink())
                            .into(new SaveImageHelper(getBaseContext(),dialog,getApplicationContext().getContentResolver(),fileName,"Picsta LiveWalpaper Image"));
                }
            }
        });

        //View Count
        increaseViewCount();

    }

    private void increaseViewCount() {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER)
                .child(Common.selectBackgroundKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("viewCount"))
                        {
                            WallpaperItem wallpaperItem=dataSnapshot.getValue(WallpaperItem.class);
                            long count=wallpaperItem.getViewCount()+1;
                            //Update
                            Map<String,Object> updateView=new HashMap<>();
                            updateView.put("viewCount",count);

                            FirebaseDatabase.getInstance()
                                    .getReference(Common.STR_WALLPAPER)
                                    .child(Common.selectBackgroundKey)
                                    .updateChildren(updateView)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewWallpaper.this, "Cannot Update View Count", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else // If view Count is not set to default
                        {
                            Map<String,Object> updateView=new HashMap<>();
                            updateView.put("viewCount",Long.valueOf(1));

                            FirebaseDatabase.getInstance()
                                    .getReference(Common.STR_WALLPAPER)
                                    .child(Common.selectBackgroundKey)
                                    .updateChildren(updateView)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewWallpaper.this, "Cannot Set Default View Count", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addToRecents() {
        Disposable disposable= io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Recents recents=new Recents(Common.selectBackground.getImagelink(),
                        Common.selectBackground.getCategoryId(),
                        String.valueOf(System.currentTimeMillis()),Common.selectBackgroundKey);
                recentRepository.insertRecents(recents);
                e.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                },new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("ERROR VIEW WALLPAPER",throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        Picasso.with(this).cancelRequest(target);
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

}
