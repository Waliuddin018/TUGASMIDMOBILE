package com.example.todolist.Model.ui.completed;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.MainActivity;
import com.example.todolist.Model.AppDatabase;
import com.example.todolist.Model.Task;
import com.example.todolist.Model.TaskDao;
import com.example.todolist.Model.home.RecyclerAdapter;
import com.example.todolist.R;
import com.example.todolist.databinding.FragmentCompletedBinding;
import com.example.todolist.ui.home.SelectListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;


public class CompletedFragment extends Fragment implements SelectListener {

    AppDatabase db;
    TaskDao taskDao;

    private FragmentCompletedBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCompletedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onStart() {
        super.onStart();
        db = MainActivity.db;
        taskDao = db.taskDao();
        refreshRecycler();
        setRecyclerVisibility();
    }

    public void refreshRecycler(){
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Task> taskList = taskDao.getComplete();
        Collections.sort(taskList);
        recyclerView.setAdapter(new RecyclerAdapter(getContext(),taskList, this));
    }

    public void setRecyclerVisibility(){
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        TextView textView = getView().findViewById(R.id.noTaskTextView);
        if (!taskDao.getComplete().isEmpty()){
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(Task task) {
        showBottomDialog(task);
    }

    public void showBottomDialog(Task task) {

        final Dialog bottomDialog = new Dialog(getContext());
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(R.layout.completed_bottom_sheet_layout);


        LinearLayout markIncompleteLayout = bottomDialog.findViewById(R.id.completed_bottom_sheet_markIncomplete);
        LinearLayout deleteLayout = bottomDialog.findViewById(R.id.completed_bottom_sheet_delete);

        markIncompleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskDao.setIncomplete(task.getTaskId());
                Snackbar.make(getView(), task.getTaskName() + " Marked As Incomplete", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            taskDao.setComplete(task.getTaskId());
                            refreshRecycler();
                            setRecyclerVisibility();
                        })
                        .show();
                refreshRecycler();
                setRecyclerVisibility();
                bottomDialog.dismiss();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDelete(task);
                bottomDialog.dismiss();}
        });

        bottomDialog.show();
        bottomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showDelete(Task task){
        final Dialog deleteDialog = new Dialog(getContext());

        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteDialog.setContentView(R.layout.delete_dialog);

        Button cancelButton = deleteDialog.findViewById(R.id.cancel_delete_button);
        Button deleteButton = deleteDialog.findViewById(R.id.delete_button);

        deleteDialog.show();
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.getWindow().setGravity(Gravity.CENTER);
        deleteDialog.setCancelable(false);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskDao.delete(task);
                refreshRecycler();
                setRecyclerVisibility();
                deleteDialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}