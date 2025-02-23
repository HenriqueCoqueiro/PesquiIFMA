package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.henrique.pesquiifma.R;

import java.util.List;

import entities.Form;

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.FormViewHolder> {
    private List<Form> formList;

    public FormAdapter(List<Form> formList) {
        this.formList = formList;
    }

    @Override
    public FormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_form, parent, false);
        return new FormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FormViewHolder holder, int position) {
        Form form = formList.get(position);
        holder.titleTextView.setText(form.getTitle());
        holder.descriptionTextView.setText(form.getDescription());
    }

    @Override
    public int getItemCount() {
        return formList.size();
    }

    public static class FormViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;

        public FormViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
