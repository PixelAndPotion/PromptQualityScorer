package com.eduvos.promptqualityscorer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduvos.promptqualityscorer.model.CompanyRuleEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class CompanyRuleAdapter extends RecyclerView.Adapter<CompanyRuleAdapter.ViewHolder> {

    private final List<CompanyRuleEntity> rulesList;
    private final OnRuleChangeListener listener;

    public interface OnRuleChangeListener {
        void onToggle(CompanyRuleEntity rule, boolean isEnabled);
        void onDelete(CompanyRuleEntity rule);
    }

    public CompanyRuleAdapter(List<CompanyRuleEntity> rulesList, OnRuleChangeListener listener) {
        this.rulesList = rulesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company_rule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompanyRuleEntity rule = rulesList.get(position);
        holder.textRuleTitle.setText(rule.getTitle());
        holder.textRuleDesc.setText(rule.getDescription());

        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(rule.isEnabled());
        holder.switchEnabled.setContentDescription("Enable " + rule.getTitle());
        holder.btnDeleteRule.setContentDescription("Delete " + rule.getTitle());

        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onToggle(rule, isChecked);
        });

        holder.btnDeleteRule.setOnClickListener(v -> listener.onDelete(rule));
    }

    @Override
    public int getItemCount() {
        return rulesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textRuleTitle, textRuleDesc;
        SwitchMaterial switchEnabled;
        ImageButton btnDeleteRule;

        ViewHolder(View itemView) {
            super(itemView);
            textRuleTitle = itemView.findViewById(R.id.textRuleTitle);
            textRuleDesc = itemView.findViewById(R.id.textRuleDesc);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            btnDeleteRule = itemView.findViewById(R.id.btnDeleteRule);
        }
    }
}
