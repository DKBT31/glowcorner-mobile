package com.example.mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Models.AnswerOption;
import com.example.mobile.R;
import com.example.mobile.Models.Quiz;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private List<Quiz> quizList;
    private Context context;

    public QuizAdapter(Context context, List<Quiz> quizList) {
        this.context = context;
        this.quizList = quizList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.quizText.setText(quiz.getQuizText());

        holder.answerOptions.removeAllViews();
        for (AnswerOption option : quiz.getAnswerOptionDTOS()) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(option.getOptionText());
            radioButton.setId(View.generateViewId());
            holder.answerOptions.addView(radioButton);
        }
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView quizText;
        RadioGroup answerOptions;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            quizText = itemView.findViewById(R.id.quizText);
            answerOptions = itemView.findViewById(R.id.answerOptions);
        }
    }
}