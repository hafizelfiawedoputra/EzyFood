package com.hms.utsmobileprogramming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hms.utsmobileprogramming.adapter.MySnackAdapter;
import com.hms.utsmobileprogramming.eventbus.MyUpdateCartEvent;
import com.hms.utsmobileprogramming.listener.ICartLoadListener;
import com.hms.utsmobileprogramming.listener.ISnackLoadListener;
import com.hms.utsmobileprogramming.listener.ISnackLoadListener;
import com.hms.utsmobileprogramming.model.CartModel;
import com.hms.utsmobileprogramming.model.SnackModel;
import com.hms.utsmobileprogramming.utils.SpaceItemDecoration;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SnackActivity extends AppCompatActivity implements ISnackLoadListener, ICartLoadListener {
    @BindView(R.id.recycler_snack)
    RecyclerView recyclerSnack;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.badge)
    NotificationBadge badge;
    @BindView(R.id.btnCart)
    FrameLayout btnCart;

    ISnackLoadListener snackLoadListener;
    ICartLoadListener cartLoadListener;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(MyUpdateCartEvent.class))
            EventBus.getDefault().removeStickyEvent(MyUpdateCartEvent.class);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onUpdateCart(MyUpdateCartEvent event){
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack);

        ImageView buttonBack = findViewById(R.id.btnBack);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SnackActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        init();
        loadSnackFromFirebase();
        countCartItem();
    }

    private void loadSnackFromFirebase() {
        List<SnackModel> snackModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Snack")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot snackSnapshot:snapshot.getChildren()){
                                SnackModel snackModel = snackSnapshot.getValue(SnackModel.class);
                                snackModel.setKey(snackSnapshot.getKey());
                                snackModels.add(snackModel);
                            }
                            snackLoadListener.onSnackLoadSuccess(snackModels);
                        }else
                            snackLoadListener.onSnackLoadFailed("Can't find Snack");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        snackLoadListener.onSnackLoadFailed(error.getMessage());
                    }
                });
    }

    private void init() {
        ButterKnife.bind(this);

        snackLoadListener = this;
        cartLoadListener = this;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerSnack.setLayoutManager(gridLayoutManager);
        recyclerSnack.addItemDecoration(new SpaceItemDecoration());

        btnCart.setOnClickListener(c -> startActivity(new Intent(this,CartActivity.class)));

    }

    @Override
    public void onSnackLoadSuccess(List<SnackModel> snackModelList) {
        MySnackAdapter adapter = new MySnackAdapter(this,snackModelList, cartLoadListener);
        recyclerSnack.setAdapter(adapter);
    }

    @Override
    public void onSnackLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        int cartSum = 0;
        for (CartModel cartModel: cartModelList)
            cartSum += cartModel.getQuantity();
        badge.setNumber(cartSum);
    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        countCartItem();
    }

    private void countCartItem() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase
                .getInstance().getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot cartSnapshot:snapshot.getChildren()){
                            CartModel cartModel = cartSnapshot.getValue(CartModel.class);
                            cartModel.setKey(cartSnapshot.getKey());
                            cartModels.add(cartModel);
                        }
                        cartLoadListener.onCartLoadSuccess(cartModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }
}