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
import com.hms.utsmobileprogramming.adapter.MyTopUpAdapter;
import com.hms.utsmobileprogramming.eventbus.MyUpdateCartEvent;
import com.hms.utsmobileprogramming.listener.ICartLoadListener;
import com.hms.utsmobileprogramming.listener.ITopUpLoadListener;
import com.hms.utsmobileprogramming.model.CartModel;
import com.hms.utsmobileprogramming.model.TopUpModel;
import com.hms.utsmobileprogramming.utils.SpaceItemDecoration;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TopUpActivity extends AppCompatActivity implements ITopUpLoadListener, ICartLoadListener {
    @BindView(R.id.recycler_topUp)
    RecyclerView recyclerTopUp;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.badge)
    NotificationBadge badge;
    @BindView(R.id.btnCart)
    FrameLayout btnCart;

    ITopUpLoadListener topUpLoadListener;
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
        setContentView(R.layout.activity_top_up);

        ImageView buttonBack = findViewById(R.id.btnBack);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TopUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        init();
        loadTopUpFromFirebase();
        countCartItem();
    }

    private void loadTopUpFromFirebase() {
        List<TopUpModel> topUpModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("TopUp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot topUpSnapshot:snapshot.getChildren()){
                                TopUpModel topUpModel = topUpSnapshot.getValue(TopUpModel.class);
                                topUpModel.setKey(topUpSnapshot.getKey());
                                topUpModels.add(topUpModel);
                            }
                            topUpLoadListener.onTopUpLoadSuccess(topUpModels);
                        }else
                            topUpLoadListener.onTopUpLoadFailed("Can't find Top Up");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        topUpLoadListener.onTopUpLoadFailed(error.getMessage());
                    }
                });
    }

    private void init() {
        ButterKnife.bind(this);

        topUpLoadListener = this;
        cartLoadListener = this;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerTopUp.setLayoutManager(gridLayoutManager);
        recyclerTopUp.addItemDecoration(new SpaceItemDecoration());

        btnCart.setOnClickListener(c -> startActivity(new Intent(this,CartActivity.class)));

    }

    @Override
    public void onTopUpLoadSuccess(List<TopUpModel> topUpModelList) {
        MyTopUpAdapter adapter = new MyTopUpAdapter(this,topUpModelList, cartLoadListener);
        recyclerTopUp.setAdapter(adapter);
    }

    @Override
    public void onTopUpLoadFailed(String message) {
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