package com.henrique.pesquiifma;

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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Form extends AppCompatActivity {

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
                Toast.makeText(Form.this, "Digite uma pergunta!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tipoResposta.equals("Múltipla Escolha")) {
                mostrarDialogoAdicionarOpcoes(pergunta);
            } else {
                listaPerguntas.add(pergunta + " (" + tipoResposta + ")");
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoAdicionarOpcoes(String pergunta) {
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
                String perguntaComOpcoes = pergunta + " (Múltipla Escolha): " + listaOpcoes.toString();
                listaPerguntas.add(perguntaComOpcoes);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(Form.this, "Adicione pelo menos uma opção!", Toast.LENGTH_SHORT).show();
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
            perguntaData.put("pergunta", pergunta);
            perguntaData.put("tipoResposta", "Texto"); // Ajuste conforme necessário
            perguntasList.add(perguntaData);
        }

        // Criar o objeto do formulário sem o link por enquanto
        Map<String, Object> form = new HashMap<>();
        form.put("titulo", titulo);
        form.put("descricao", descricao);
        form.put("perguntas", perguntasList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("formularios")
                .add(form)
                .addOnSuccessListener(documentReference -> {
                    // Gerar o link usando o ID do documento
                    String formId = documentReference.getId();
                    String link = "https://pesqui-ifma.com/form/" + formId;

                    // Atualizar o documento com o link
                    documentReference.update("link", link)
                            .addOnSuccessListener(aVoid -> {
                                // Exibir o link na tela
                                TextView textViewLink = findViewById(R.id.textViewLink);
                                textViewLink.setText("Link do formulário: " + link);
                                textViewLink.setVisibility(View.VISIBLE);

                                Toast.makeText(this, "Formulário salvo com sucesso!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao salvar o link do formulário!", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar o formulário!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}