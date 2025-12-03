package com.farashian.test.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.farashian.test.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TestCaseAdapter extends RecyclerView.Adapter<TestCaseAdapter.ViewHolder> {

    private List<PersianCalendarActivity.TestCase> testCases;
    private OnTestCaseClickListener listener;

    public interface OnTestCaseClickListener {
        void onTestCaseClick(PersianCalendarActivity.TestCase testCase);
    }

    public TestCaseAdapter(List<PersianCalendarActivity.TestCase> testCases, 
                          OnTestCaseClickListener listener) {
        this.testCases = testCases;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersianCalendarActivity.TestCase testCase = testCases.get(position);
        
        holder.textTitle.setText(testCase.getTitle());
        holder.textDescription.setText(testCase.getDescription());
        holder.textSample.setText("Sample: " + testCase.getSample());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTestCaseClick(testCase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return testCases.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textDescription;
        TextView textSample;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.tvTitle);
            textDescription = itemView.findViewById(R.id.tvDescription);
            textSample = itemView.findViewById(R.id.tvSample);
        }
    }
}