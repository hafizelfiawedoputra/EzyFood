package com.hms.utsmobileprogramming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hms.utsmobileprogramming.adapter.MyMoneyAdapter;
import com.hms.utsmobileprogramming.eventbus.MyUpdateMoneyEvent;
import com.hms.utsmobileprogramming.listener.IMoneyLoadListener;
import com.hms.utsmobileprogramming.model.MoneyModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoneyActivity extends AppCompatActivity implements IMoneyLoadListener {

    @BindView(R.id.recycler_money)
    RecyclerView recyclerMoney;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTotal)
    TextView txtTotal;

    IMoneyLoadListener moneyLoadListener;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(MyUpdateMoneyEvent.class))
            EventBus.getDefault().removeStickyEvent(MyUpdateMoneyEvent.class);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onUpdateMoney(MyUpdateMoneyEvent event){
        loadMoneyFromFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);

        init();
        loadMoneyFromFirebase();
        ImageView homeButton = findViewById(R.id.btnHome);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoneyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadMoneyFromFirebase() {
        List<MoneyModel> moneyModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Cast")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot moneySnapshot:snapshot.getChildren()){
                                MoneyModel moneyModel = moneySnapshot.getValue(MoneyModel.class);
                                moneyModel.setKey(moneySnapshot.getKey());
                                moneyModels.add(moneyModel);
                            }
                            moneyLoadListener.onMoneyLoadSuccess(moneyModels);
                        }else
                            moneyLoadListener.onMoneyLoadFailed("Money empty");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        moneyLoadListener.onMoneyLoadFailed(error.getMessage());
                    }
                });
    }

    private void init() {
        ButterKnife.bind(this);

        moneyLoadListener = this;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerMoney.setLayoutManager(layoutManager);
        recyclerMoney.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMoneyLoadSuccess(List<MoneyModel> moneyModelList) {
        double sum = 0;
        for (MoneyModel moneyModel : moneyModelList){
            sum+=moneyModel.getTotalPrice();
        }
        txtTotal.setText(new StringBuffer("Rp. ").append(sum));
        MyMoneyAdapter adapter = new MyMoneyAdapter(this, moneyModelList);
        recyclerMoney.setAdapter(adapter);

    }

    @Override
    public void onMoneyLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();
    }
}