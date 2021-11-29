package com.hms.utsmobileprogramming.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.hms.utsmobileprogramming.R;
import com.hms.utsmobileprogramming.eventbus.MyUpdateCartEvent;
import com.hms.utsmobileprogramming.eventbus.MyUpdateMoneyEvent;
import com.hms.utsmobileprogramming.model.CartModel;
import com.hms.utsmobileprogramming.model.MoneyModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyMoneyAdapter extends RecyclerView.Adapter<MyMoneyAdapter.MyMoneyViewHolder> {
    private Context context;
    private List<MoneyModel> moneyModelList;

    public MyMoneyAdapter(Context context, List<MoneyModel> moneyModelList) {
        this.context = context;
        this.moneyModelList = moneyModelList;
    }

    @NonNull
    @Override
    public MyMoneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyMoneyAdapter.MyMoneyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_money_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyMoneyAdapter.MyMoneyViewHolder holder, int position) {
        Glide.with(context)
                .load(moneyModelList.get(position).getImage())
                .into(holder.imageView);
        holder.txtPrice.setText(new StringBuffer("Rp. ").append(moneyModelList.get(position).getPrice()));
        holder.txtName.setText(new StringBuffer().append(moneyModelList.get(position).getName()));
        holder.txtQuantity.setText(new StringBuffer().append(moneyModelList.get(position).getQuantity()));

    }

    @Override
    public int getItemCount() {
        return moneyModelList.size();
    }


    public class MyMoneyViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPrice)
        TextView txtPrice;
        @BindView(R.id.txtQuantity)
        TextView txtQuantity;

        Unbinder unbinder;
        public MyMoneyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
