package com.henrique.pesquiifma;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatForm extends AppCompatActivity {

    EditText editTextTitulo, editTextDescricao;
    Button buttonAdicionarPergunta, buttonSalvar;
    ListView listViewPerguntas;
    ArrayList<String> listaPerguntas;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        getSupportActionBar().hide();

        // Inicializando os componentes
        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        buttonAdicionarPergunta = findViewById(R.id.buttonAdicionarPergunta);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        listViewPerguntas = findViewById(R.id.listViewPerguntas);

        // Configuração da lista de perguntas
        listaPerguntas = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaPerguntas);
        listViewPerguntas.setAdapter(adapter);

        // Adicionar Pergunta
        buttonAdicionarPergunta.setOnClickListener(v -> mostrarDialogoAdicionarPergunta());

        // Salvar Formulário
        buttonSalvar.setOnClickListener(v -> salvarFormulario());
    }

    private void mostrarDialogoAdicionarPergunta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Pergunta");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        EditText editTextPergunta = view.findViewById(R.id.editTextPergunta);
        Spinner spinnerTipoResposta = view.findViewById(R.id.spinnerTipoResposta);

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.tipos_pergunta));
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoResposta.setAdapter(adapterSpinner);

        builder.setView(view);
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String pergunta = editTextPergunta.getText().toString().trim();
            String tipoResposta = spinnerTipoResposta.getSelectedItem().toString();

            if (pergunta.isEmpty()) {
                Toast.makeText(CreatForm.this, "Digite uma pergunta!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tipoResposta.equals("Múltipla Escolha") || tipoResposta.equals("SIM/Não")) {
                mostrarDialogoAdicionarOpcoes(pergunta, tipoResposta);
            } else {
                listaPerguntas.add(pergunta + " (" + tipoResposta + ")");
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoAdicionarOpcoes(String pergunta, String tipoResposta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Opções");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_options, null);
        EditText editTextOpcao = view.findViewById(R.id.editTextOpcao);
        Button buttonAdicionarOpcao = view.findViewById(R.id.buttonAdicionarOpcao);
        ListView listViewOpcoes = view.findViewById(R.id.listViewOpcoes);

        ArrayList<String> listaOpcoes = new ArrayList<>();
        ArrayAdapter<String> adapterOpcoes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaOpcoes);
        listViewOpcoes.setAdapter(adapterOpcoes);

        buttonAdicionarOpcao.setOnClickListener(v -> {
            String opcao = editTextOpcao.getText().toString().trim();
            if (!opcao.isEmpty()) {
                listaOpcoes.add(opcao);
                adapterOpcoes.notifyDataSetChanged();
                editTextOpcao.setText("");
            }
        });

        builder.setView(view);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            if (!listaOpcoes.isEmpty()) {
                String perguntaComOpcoes = pergunta + " (" + tipoResposta + "): " + listaOpcoes.toString();
                listaPerguntas.add(perguntaComOpcoes);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(CreatForm.this, "Adicione pelo menos uma opção!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void salvarFormulario() {
        String titulo = editTextTitulo.getText().toString();
        String descricao = editTextDescricao.getText().toString();

        if (titulo.isEmpty() || descricao.isEmpty() || listaPerguntas.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar a estrutura de perguntas para o formulário
        ArrayList<Map<String, Object>> perguntasList = new ArrayList<>();
        for (String pergunta : listaPerguntas) {
            Map<String, Object> perguntaData = new HashMap<>();
            String tipoResposta = "Texto"; // Default

            // Verifica e remove o tipo de resposta
            String[] tipos = {"Sim/Não", "Múltipla Escolha", "Número", "Texto", "Data"};
            for (String tipo : tipos) {
                if (pergunta.contains(" (" + tipo + ")")) {
                    tipoResposta = tipo;
                    pergunta = pergunta.replace(" (" + tipo + ")", "").trim();
                    break;
                }
            }

            perguntaData.put("pergunta", pergunta);
            perguntaData.put("tipoResposta", tipoResposta);
            perguntasList.add(perguntaData);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "";

        Map<String, Object> form = new HashMap<>();
        form.put("titulo", titulo);
        form.put("descricao", descricao);
        form.put("perguntas", perguntasList);
        form.put("uid", uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("formularios")
                .add(form)
                .addOnSuccessListener(documentReference -> {
                    String formId = documentReference.getId();
                    String link = "https://pesqui-ifma.com/form/" + formId;
                    documentReference.update("formId", formId, "link", link);
                });
    }
}
