package com.tejma.sched.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tejma.sched.POJO.NotificationSaved;
import com.tejma.sched.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    List<NotificationSaved> objects;
    Context context;
    SharedPreferences sharedPreferences;
    private final OnClickListener onClickListener;

    public NotificationAdapter(@NonNull Context context, List<NotificationSaved> objects, OnClickListener onClickListener) {
        this.context = context;
        this.objects = objects;
        this.onClickListener = onClickListener;
        sharedPreferences = context.getSharedPreferences("Classes", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.title.setText(objects.get(position).getTitle());
        holder.desc.setText(objects.get(position).getDescription());
    }

    @Override
    public int getItemCount() { return objects == null ? 0 : objects.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView title, desc;
        OnClickListener clickListener;

         public ViewHolder(View itemView, OnClickListener OnClickListener){
             super(itemView);
             this.title = itemView.findViewById(R.id.title);
             this.desc = itemView.findViewById(R.id.desc);
             itemView.setOnClickListener(this);
             itemView.setOnLongClickListener(this);
         }




        @Override
        public void onClick(View v) {
            clickListener.onClick(getLayoutPosition(), v);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onLongClick(getLayoutPosition());
            return true;
        }
    }

    public interface OnClickListener{
        void onClick(int position, View v);
        void onLongClick(int position);
    }

}
