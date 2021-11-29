package com.hms.utsmobileprogramming.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hms.utsmobileprogramming.R;
import com.hms.utsmobileprogramming.eventbus.MyUpdateCartEvent;
import com.hms.utsmobileprogramming.listener.ICartLoadListener;
import com.hms.utsmobileprogramming.listener.IRecyclerViewClickListener;
import com.hms.utsmobileprogramming.model.CartModel;
import com.hms.utsmobileprogramming.model.SnackModel;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MySnackAdapter extends RecyclerView.Adapter<MySnackAdapter.MySnackViewHolder> {

    private Context context;
    private List<SnackModel> snackModelList;
    private ICartLoadListener iCartLoadListener;

    public MySnackAdapter(Context context, List<SnackModel> snackModelList, ICartLoadListener iCartLoadListener) {
        this.context = context;
        this.snackModelList = snackModelList;
        this.iCartLoadListener = iCartLoadListener;
    }

    @NonNull
    @Override
    public MySnackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MySnackViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_order_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MySnackAdapter.MySnackViewHolder holder, int position) {
        Glide.with(context)
                .load(snackModelList.get(position).getImage())
                .into(holder.imageView);
        holder.txtPrice.setText(new StringBuffer("Rp. ").append(snackModelList.get(position).getPrice()));
        holder.txtName.setText(new StringBuffer().append(snackModelList.get(position).getName()));

        holder.setListener((view, adapterPosition) -> {
            addToCart(snackModelList.get(position));
        });
    }

    private void addToCart(SnackModel drinkModel) {
        DatabaseReference userCart = FirebaseDatabase
                .getInstance()
                .getReference("Cast")
                .child("UNIQUE_USER_ID");

        userCart.child(drinkModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            //Just update quantity and totalPrice
                            CartModel cartModel = snapshot.getValue(CartModel.class);
                            cartModel.setQuantity(cartModel.getQuantity()+1);
                            Map<String,Object> updateData = new HashMap<>();
                            updateData.put("quantity",cartModel.getQuantity());
                            updateData.put("totalPrice",cartModel.getQuantity()*Float.parseFloat(cartModel.getPrice()));

                            userCart.child(drinkModel.getKey())
                                    .updateChildren(updateData)
                                    .addOnSuccessListener(unused -> {
                                        iCartLoadListener.onCartLoadFailed("Add To Cart Success");
                                    })
                                    .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));
                        }
                        else
                        {
                            CartModel cartModel = new CartModel();
                            cartModel.setName(drinkModel.getName());
                            cartModel.setImage(drinkModel.getImage());
                            cartModel.setKey(drinkModel.getKey());
                            cartModel.setPrice(drinkModel.getPrice());
                            cartModel.setQuantity(1);
                            cartModel.setTotalPrice(Float.parseFloat(drinkModel.getPrice()));

                            userCart.child(drinkModel.getKey())
                                    .setValue(cartModel)
                                    .addOnSuccessListener(unused -> {
                                        iCartLoadListener.onCartLoadFailed("Add To Cart Success");
                                    })
                                    .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));

                        }
                        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iCartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return snackModelList.size();
    }

    public class MySnackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPrice)
        TextView txtPrice;

        IRecyclerViewClickListener listener;

        public void setListener(IRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        private Unbinder unbinder;
        public MySnackViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onRecyclerClick(v, getAdapterPosition());
        }
    }
}
