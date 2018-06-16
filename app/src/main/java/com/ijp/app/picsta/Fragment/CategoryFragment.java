package com.ijp.app.picsta.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ijp.app.picsta.Common.Common;
import com.ijp.app.picsta.Interface.ItemClickListener;
import com.ijp.app.picsta.ListWallpaper;
import com.ijp.app.picsta.Model.CategoryItem;
import com.ijp.app.picsta.R;
import com.ijp.app.picsta.ViewHolder.CategoryViewHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class CategoryFragment extends Fragment {
    // Firebase
    FirebaseDatabase database;
    DatabaseReference categoryBackground;

    //Firebase UI Adapeter
    FirebaseRecyclerOptions<CategoryItem> options;
    FirebaseRecyclerAdapter<CategoryItem,CategoryViewHolder> adapter;

    RecyclerView recyclerView;

    private static CategoryFragment INSTANCE=null;


    public CategoryFragment() {
        database=FirebaseDatabase.getInstance();
        categoryBackground=database.getReference(Common.STR_CATEGORY_BACKGROUND);

        options=new FirebaseRecyclerOptions.Builder<CategoryItem>()
                .setQuery(categoryBackground,CategoryItem.class)// Select all
                .build();

        adapter=new FirebaseRecyclerAdapter<CategoryItem, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position, @NonNull final CategoryItem model) {
                Picasso.with(getActivity())
                        .load(model.getImageLink())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.backgroundImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                // Try again online if cache fail
                                Picasso.with(getActivity())
                                        .load(model.getImageLink())
                                        .error(R.drawable.ic_terrain_black_24dp)
                                        .into(holder.backgroundImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Log.e("ERROR","COULDNT FETCH IMAGE");
                                            }
                                        });
                            }
                        });
                holder.categoryName.setText(model.getName());

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Common.CATEGORY_ID_SELECTED=adapter.getRef(position).getKey();// Get Key of Item
                        Common.CATEGORY_SELECTED=model.getName();
                        Intent intent=new Intent(getActivity(), ListWallpaper.class);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_category_item,parent,false);
                return new CategoryViewHolder(itemView);
            }
        };
    }

    public static CategoryFragment getInstance()
    {
        if(INSTANCE==null)
            INSTANCE=new CategoryFragment();
        return INSTANCE;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_category, container, false);
        recyclerView=view.findViewById(R.id.recycler_category);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(gridLayoutManager);

        setCategory();

        return view;
    }

    private void setCategory() {
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        super.onStop();

    }
}
