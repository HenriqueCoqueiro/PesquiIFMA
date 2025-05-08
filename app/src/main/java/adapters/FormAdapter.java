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
import com.henrique.pesquiifma.ViewResponses; // Importa a RespostasActivity
import java.util.List;
import entities.Form;

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.FormViewHolder> {
    private List<Form> formList;
    private Context context;

    public FormAdapter(List<Form> formList, Context context) {
        this.formList = formList;
        this.context = context; // Contexto necessário para navegar para outra Activity
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

        // Adiciona o OnClickListener para navegar até a RespostasActivity
        holder.itemView.setOnClickListener(v -> {
            // Passa o formId para a RespostasActivity
            Intent intent = new Intent(context, ViewResponses.class);
            intent.putExtra("formId", form.getFormId()); // Passa o formId
            context.startActivity(intent);
        });

        // Adiciona o comportamento de copiar link ao botão
        holder.copyLinkButton.setOnClickListener(v -> {
            // Ajuste do link para seguir o padrão PesquiIFMA
            String formLink = "https://pesqui-ifma.com/form/" + form.getFormId(); // Link do formulário
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Link do Formulário", formLink);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Link copiado!", Toast.LENGTH_SHORT).show(); // Notifica que o link foi copiado
        });
    }

    @Override
    public int getItemCount() {
        return formList.size();
    }

    public static class FormViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public Button copyLinkButton; // Botão para copiar o link

        public FormViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            copyLinkButton = itemView.findViewById(R.id.btn_copy_link); // Inicializa o botão de copiar
        }
    }
}
