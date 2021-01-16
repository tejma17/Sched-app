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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tejma.sched.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tejma.sched.POJO.Lecture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    String TAG = "ADAPTER";
    List<Lecture> objects, query_list;
    Context context;
    int res;
    SharedPreferences sharedPreferences;
    String link;
    BottomSheetDialog builder;
    String day;
    Dialog alert;
    private final onNoteListener mOnNoteListener;
    LayoutInflater inflater;

    public MyAdapter(@NonNull Context context, String day, int resource, List<Lecture> objects, onNoteListener mOnNoteListener) {
        this.context = context;
        this.objects = new ArrayList<>();
        this.objects = objects;
        this.day = day;
        res = resource;
        inflater = LayoutInflater.from(context);
        this.mOnNoteListener = mOnNoteListener;
        query_list = new ArrayList<>();
        alert = new Dialog(context);
        alert.setContentView(R.layout.alertdialog);
        sharedPreferences = context.getSharedPreferences("Classes", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
        query_list.clear();
        query_list.addAll(objects);
        return new ViewHolder(view, mOnNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String type = objects.get(position).getType().substring(0,3);
        type = "("+type+")";
        String titleText = objects.get(position).getSubject();
        if (titleText.length() > 14) {
            titleText = titleText.substring(0, 14) + "...";
        }
        holder.title.setText(titleText);
        holder.type.setText(type);
        holder.code.setText(objects.get(position).getCode());
        holder.teacher.setText(objects.get(position).getFaculty());
        holder.time.setText(objects.get(position).getTime());

        builder = new BottomSheetDialog(context);
        builder.setContentView(R.layout.search_layout);
        ImageButton phone = builder.findViewById(R.id.phoneButton);
        ImageButton pc = builder.findViewById(R.id.pcButton);
        ImageButton help = builder.findViewById(R.id.help);


        CheckBox checkBox = alert.findViewById(R.id.checkbox);
        checkBox.setVisibility(View.VISIBLE);
        TextView button = alert.findViewById(R.id.understood);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                builder.show();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit().putString("UNDERSTOOD", String.valueOf(checkBox.isChecked())).apply();
            }
        });

        Objects.requireNonNull(pc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnection()) {
                    String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(UID);
                    databaseReference.child("Link").setValue(link);
                    Toast.makeText(context, "Success. Check your computer", Toast.LENGTH_SHORT).show();
                    builder.dismiss();
                }else{
                    Toast.makeText(context, "Please turn on the network access", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Objects.requireNonNull(phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);
                Toast.makeText(context, "Opening...", Toast.LENGTH_LONG).show();
                builder.dismiss();
            }
        });

        Objects.requireNonNull(help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
                builder.dismiss();
                checkBox.setVisibility(View.GONE);
            }
        });


        holder.meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                link = objects.get(position).getMeet_link();
                if(link.contains("meet.google.com/")) {
                    if(link.startsWith("meet.google.com/"))
                        link = "https://"+link;
                    if(sharedPreferences.getString("UNDERSTOOD", "false").equals("false")) {
                        alert.show();
                    }else
                        builder.show();
                }else
                    Toast.makeText(context, "Invalid Meet Link", Toast.LENGTH_SHORT).show();
            }
        });

        holder.classroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                link = objects.get(position).getClassroom_link();
                if(link.contains("classroom.google.com/")) {
                    if(link.startsWith("classroom.google.com/"))
                        link = "https://"+link;
                    if(sharedPreferences.getString("UNDERSTOOD", "false").equals("false")) {
                        alert.show();
                    }else
                        builder.show();
                }else
                    Toast.makeText(context, "Invalid Classroom Link", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView title, time, teacher, code, type;
        MaterialButton meet, classroom;
        RelativeLayout parent;
        onNoteListener noteListener;

         public ViewHolder(View itemView, onNoteListener onNoteListener){
             super(itemView);
             this.title = itemView.findViewById(R.id.name);
             this.type = itemView.findViewById(R.id.type);
             this.time = itemView.findViewById(R.id.time);
             this.code = itemView.findViewById(R.id.code);
             this.teacher = itemView.findViewById(R.id.faculty);
             this.meet = itemView.findViewById(R.id.meet);
             this.classroom = itemView.findViewById(R.id.classroom);
             this.noteListener = onNoteListener;
             itemView.setOnClickListener(this);
             itemView.setOnLongClickListener(this);
             this.parent = (RelativeLayout) itemView.findViewById(R.id.parent_layout);
         }




        @Override
        public void onClick(View v) {
            noteListener.onNoteClick(getLayoutPosition(), v);
        }

        @Override
        public boolean onLongClick(View view) {
            noteListener.onNoteLongClick(getLayoutPosition());
            return true;
        }
    }

    public interface onNoteListener{
        void onNoteClick(int position, View v);
        void onNoteLongClick(int position);
    }

    public boolean isConnection(){
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
