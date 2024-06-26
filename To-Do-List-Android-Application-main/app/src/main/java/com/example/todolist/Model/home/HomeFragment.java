package com.example.todolist.Model.home;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.MainActivity;
import com.example.todolist.Model.AppDatabase;
import com.example.todolist.Model.Notifications;
import com.example.todolist.Model.Task;
import com.example.todolist.Model.TaskDao;
import com.example.todolist.R;
import com.example.todolist.databinding.FragmentHomeBinding;
import com.example.todolist.ui.home.SelectListener;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
public class HomeFragment extends Fragment implements SelectListener {

    AppDatabase db;
    TaskDao taskDao;

    private FragmentHomeBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        return binding.getRoot();
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
        recycler();
        setRecyclerVisibility();
    }

    public void addToRecycler(Task task) {
        taskDao.insert(task);
        recycler();
        setRecyclerVisibility();
    }

    public void removeFromRecycler(Task task){
        taskDao.delete(task);
        recycler();
        setRecyclerVisibility();
    }

    public void recycler() {
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Task> taskList = taskDao.getIncomplete();
        Collections.sort(taskList);
        recyclerView.setAdapter(new RecyclerAdapter(getContext(),taskList, this));
    }

    public void setRecyclerVisibility(){
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        TextView textView = getView().findViewById(R.id.noTaskTextView);
        if (!taskDao.getIncomplete().isEmpty()){
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClicked(Task task) {
        showBottomDialog(task);
    }
    private void showBottomDialog(Task task) {
        final Dialog bottomDialog = new Dialog(getContext());
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout markCompleteLayout = bottomDialog.findViewById(R.id.bottom_sheet_markComplete);
        LinearLayout editLayout = bottomDialog.findViewById(R.id.bottom_sheet_edit);
        LinearLayout deleteLayout = bottomDialog.findViewById(R.id.bottom_sheet_delete);
        LinearLayout notifLayout = bottomDialog.findViewById(R.id.bottom_sheet_notification);

        markCompleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskDao.setComplete(task.getTaskId());
                Snackbar.make(getView(), task.getTaskName() + " Marked As Complete", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            taskDao.setIncomplete(task.getTaskId());
                            recycler();
                            setRecyclerVisibility();
                        })
                        .show();
                recycler();
                setRecyclerVisibility();
                bottomDialog.dismiss();
            }
        });

        editLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(task);
                bottomDialog.dismiss();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog(task);
                bottomDialog.dismiss();}
        });

        notifLayout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED) {
                }
                else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }

                Notifications notifications = new Notifications();
                notifications.createNotificationChannel(getContext());
                Notification notif = notifications.createNotification(
                        task.getTaskName(),
                        task.getTaskDescription(),
                        R.drawable.baseline_notifications_24,
                        getContext());

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy", Locale.ENGLISH);
                try {
                    calendar.setTime(sdf.parse(task.getTaskDate()));
                    calendar.set(Calendar.HOUR, Calendar.HOUR);
                    calendar.set(Calendar.MINUTE, Calendar.MINUTE);
                    calendar.set(Calendar.SECOND, Calendar.SECOND + 5);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                notifications.scheduleNotification(notif, calendar, getContext());
                bottomDialog.dismiss();
            }
        });

        bottomDialog.show();
        bottomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    private void showDeleteDialog(Task task){
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
                removeFromRecycler(task);
                deleteDialog.dismiss();
            }
        });

    }

     private void showEditDialog(Task task){
         final Dialog editDialog = new Dialog(getContext());
         editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         editDialog.setContentView(R.layout.edit_dialog);
         EditText taskName = editDialog.findViewById(R.id.task_name);
         EditText cDate = editDialog.findViewById(R.id.current_date);
         EditText dueDate = editDialog.findViewById(R.id.due_date);
         EditText reminder = editDialog.findViewById(R.id.reminder_time);
         EditText taskDescription = editDialog.findViewById(R.id.description_task);
         Button cancelButton = editDialog.findViewById(R.id.cancelButton);
         Button updateButton = editDialog.findViewById(R.id.update_button);

         cDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus) {
                     cDate.setEnabled(false);
                 }
             }
         });
         cancelButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 editDialog.dismiss();
             }
         });

         dueDate.addTextChangedListener(new TextWatcher() {
             private String current = "";
             private String mmddyyyy = "MMDDYYYY";
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 if (!s.toString().equals(current)) {
                     String clean = s.toString().replaceAll("[^\\d.]", "");
                     String cleanC = current.replaceAll("[^\\d.]", "");

                     int cl = clean.length();
                     int sel = cl;
                     for (int i = 2; i <= cl && i < 6; i += 2) {
                         sel++;
                     }
                     if (clean.equals(cleanC)) sel--;

                     if (clean.length() < 8) {
                         clean = clean + mmddyyyy.substring(clean.length());
                     } else {
                         int month = Integer.parseInt(clean.substring(0, 2));
                         int day = Integer.parseInt(clean.substring(2, 4));
                         int year = Integer.parseInt(clean.substring(4, 8));

                         month = month < 1 ? 1 : month > 12 ? 12 : month;
                         Calendar cal = Calendar.getInstance();
                         cal.set(Calendar.MONTH, month - 1);
                         year = (year < 1900) ? 1900 : (year > cal.get(Calendar.YEAR)) ? cal.get(Calendar.YEAR) : year;
                         cal.set(Calendar.YEAR, year);
                         day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
                         clean = String.format("%02d%02d%02d", month, day, year);
                     }

                     clean = String.format("%s/%s/%s", clean.substring(0, 2),
                             clean.substring(2, 4),
                             clean.substring(4, 8));

                     sel = sel < 0 ? 0 : sel;
                     current = clean;
                     dueDate.setText(current);
                     dueDate.setSelection(sel < current.length() ? sel : current.length());
                 }
             }

             @Override
             public void afterTextChanged(Editable s) {}
         });

         updateButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 addToRecycler(new Task(task.getTaskId(), task.getTaskImage(), task.getIsComplete(), taskName.getText().toString(), taskDescription.getText().toString(), dueDate.getText().toString()));
                 editDialog.dismiss();
             }
         });

         taskName.setText(task.getTaskName());
         SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
         cDate.setText(dateFormat.format(calendar.getTime()));
         dueDate.setText(task.getTaskDate());
         taskDescription.setText(task.getTaskDescription());

         editDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
         editDialog.show();

     }
}