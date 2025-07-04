package adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.henrique.pesquiifma.R;
import com.henrique.pesquiifma.ViewResponses;
import java.util.List;
import entities.Form;

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.FormViewHolder> {
    private List<Form> formList;
    private Context context;

    // Interface para o callback de deletar
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private OnDeleteClickListener onDeleteClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public FormAdapter(List<Form> formList, Context context) {
        this.formList = formList;
        this.context = context;
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

        // Navegação para ViewResponses
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewResponses.class);
            intent.putExtra("formId", form.getFormId());
            context.startActivity(intent);
        });

        // Copiar link
        holder.copyLinkButton.setOnClickListener(v -> {
            String formLink = "https://pesqui-ifma.com/form/" + form.getFormId();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Link do Formulário", formLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Link copiado!", Toast.LENGTH_SHORT).show();
        });

        // Deletar formulário - chama o callback
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return formList.size();
    }

    public static class FormViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public Button copyLinkButton;
        public Button deleteButton; // Botão deletar

        public FormViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            copyLinkButton = itemView.findViewById(R.id.btn_copy_link);
            deleteButton = itemView.findViewById(R.id.btn_delete); // Inicializa o botão deletar
        }
    }
}
