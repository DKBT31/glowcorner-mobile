package com.example.mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Models.Routine;
import com.example.mobile.R;

import java.util.ArrayList;
import java.util.List;

public class ManagerRoutineAdapter extends RecyclerView.Adapter<ManagerRoutineAdapter.RoutineViewHolder> {
    private Context context;
    private List<Routine> routineList = new ArrayList<>();
    private OnRoutineActionListener actionListener;

    public interface OnRoutineActionListener {
        void onUpdate(Routine routine);
        void onDelete(Routine routine);
        void onRoutineClick(Routine routine);
    }

    public ManagerRoutineAdapter(Context context, OnRoutineActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
    }

    public void setRoutines(List<Routine> routines) {
        this.routineList = routines;
        notifyDataSetChanged();
    }

    @Override
    public RoutineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manager_item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoutineViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.routineTitleTextView.setText(routine.getRoutineName());
        holder.routineDescriptionTextView.setText(routine.getRoutineDescription());
        holder.itemView.setOnClickListener(v -> actionListener.onRoutineClick(routine));
        holder.updateButton.setOnClickListener(v -> actionListener.onUpdate(routine));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDelete(routine));
    }

    @Override
    public int getItemCount() {
        return routineList != null ? routineList.size() : 0;
    }

    public static class RoutineViewHolder extends RecyclerView.ViewHolder {
        public TextView routineTitleTextView;
        public TextView routineDescriptionTextView;
        public Button updateButton;
        public Button deleteButton;

        public RoutineViewHolder(View itemView) {
            super(itemView);
            routineTitleTextView = itemView.findViewById(R.id.routine_title);
            routineDescriptionTextView = itemView.findViewById(R.id.routine_description);
            updateButton = itemView.findViewById(R.id.update_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

