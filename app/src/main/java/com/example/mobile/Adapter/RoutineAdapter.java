package com.example.mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Models.Routine;
import com.example.mobile.R;

import java.util.ArrayList;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {
    private Context context;
    private List<Routine> routineList = new ArrayList<>();
    private OnRoutineClickListener clickListener;

    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    public RoutineAdapter(Context context, OnRoutineClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    public void setRoutines(List<Routine> routines) {
        this.routineList = routines;
        notifyDataSetChanged();
    }

    @Override
    public RoutineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoutineViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.routineNameTextView.setText(routine.getRoutineName());
        holder.routineDescriptionTextView.setText(routine.getRoutineDescription());
        holder.itemView.setOnClickListener(v -> clickListener.onRoutineClick(routine));
    }

    @Override
    public int getItemCount() {
        return routineList != null ? routineList.size() : 0;
    }

    public static class RoutineViewHolder extends RecyclerView.ViewHolder {
        public TextView routineNameTextView;
        public TextView routineDescriptionTextView;

        public RoutineViewHolder(View itemView) {
            super(itemView);
            routineNameTextView = itemView.findViewById(R.id.routine_name);
            routineDescriptionTextView = itemView.findViewById(R.id.routine_description);
        }
    }
}