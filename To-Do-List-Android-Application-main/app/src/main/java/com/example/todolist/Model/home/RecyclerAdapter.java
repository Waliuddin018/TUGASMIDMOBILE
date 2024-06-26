package com.example.todolist.Model.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.todolist.Model.Task;
import com.example.todolist.R;
import com.example.todolist.ui.home.SelectListener;

import java.util.List;
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    Context context;
    List<Task> taskList;
    SelectListener listener;

    public RecyclerAdapter(Context context, List<Task> taskList, SelectListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.task_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        holder.taskImage.setImageResource(taskList.get(position).getTaskImage());
        holder.taskName.setText(taskList.get(position).getTaskName());
        holder.taskDesc.setText(taskList.get(position).getTaskDescription());
        holder.taskDate.setText(taskList.get(position).getTaskDate());

        if (taskList.get(position).getIsComplete()) {
            holder.isComplete.setText("Completed");
            holder.taskImage.setImageResource(R.drawable.baseline_check_24);
        }
        else  {
            holder.isComplete.setText("Incomplete");
            holder.taskImage.setImageResource(R.drawable.baseline_priority_high_24);
        }

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClicked(taskList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
