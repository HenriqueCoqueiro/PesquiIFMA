package com.henrique.pesquiifma;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Form extends AppCompatActivity {

    EditText editTextTitulo, editTextDescricao, editTextPergunta;
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
        editTextPergunta = findViewById(R.id.editTextPergunta);
        buttonAdicionarPergunta = findViewById(R.id.buttonAdicionarPergunta);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        listViewPerguntas = findViewById(R.id.listViewPerguntas);

        // Inicializando a lista de perguntas e o adaptador
        listaPerguntas = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaPerguntas);
        listViewPerguntas.setAdapter(adapter);

        // Botão de adicionar pergunta
        buttonAdicionarPergunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pergunta = editTextPergunta.getText().toString().trim();
                if (!pergunta.isEmpty()) {
                    listaPerguntas.add(pergunta);  // Adiciona a pergunta à lista
                    adapter.notifyDataSetChanged();  // Atualiza a lista exibida
                    editTextPergunta.setText(""); // Limpa o campo de entrada
                } else {
                    Toast.makeText(Form.this, "Digite uma pergunta!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Botão de salvar
        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = editTextTitulo.getText().toString();
                String descricao = editTextDescricao.getText().toString();

                // Verifica se todos os campos foram preenchidos
                if (titulo.isEmpty() || descricao.isEmpty() || listaPerguntas.isEmpty()) {
                    Toast.makeText(Form.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Exibe os dados do formulário
                StringBuilder perguntasSalvas = new StringBuilder();
                for (String pergunta : listaPerguntas) {
                    perguntasSalvas.append(pergunta).append("\n");
                }

                // Exibe um resumo do formulário salvo
                Toast.makeText(Form.this, "Formulário salvo!\n" +
                        "Título: " + titulo + "\n" +
                        "Descrição: " + descricao + "\n" +
                        "Perguntas: " + perguntasSalvas.toString(), Toast.LENGTH_LONG).show();

                finish();  // Fecha a atividade atual
            }
        });
    }
}
