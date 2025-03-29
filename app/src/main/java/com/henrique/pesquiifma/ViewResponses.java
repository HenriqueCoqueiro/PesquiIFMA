package com.henrique.pesquiifma;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewResponses extends AppCompatActivity {

    private List<String> respostasList;
    private TextView respostasTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);

        respostasTextView = findViewById(R.id.respostasTextView);

        // Pega o formId passado da activity anterior
        String formId = getIntent().getStringExtra("formId");

        // Log para verificar o valor do formId
        Log.d("RespostasActivity", "formId recebido: " + formId);

        if (formId != null) {
            carregarRespostas(formId);
        } else {
            Toast.makeText(this, "Erro: Formulário não encontrado!", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarRespostas(String formId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar as respostas na subcoleção "respostas" usando o formId
        db.collection("formularios")
                .document(formId)
                .collection("respostas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    respostasList = new ArrayList<>();

                    // Processa as respostas associadas à pergunta
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String perguntaId = document.getString("perguntaId");
                        String resposta = document.getString("resposta");

                        if (perguntaId != null && resposta != null) {
                            respostasList.add(perguntaId + ":" + resposta); // Combine perguntaId e resposta
                        }
                    }

                    if (!respostasList.isEmpty()) {
                        StringBuilder respostasText = new StringBuilder();
                        for (String resposta : respostasList) {
                            respostasText.append(resposta).append("\n");
                        }
                        respostasTextView.setText(respostasText.toString());
                        exibirGraficos();  // Exibir gráficos para todas as respostas
                    } else {
                        respostasTextView.setText("Nenhuma resposta encontrada.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar as respostas!", Toast.LENGTH_SHORT).show();
                });
    }

    private void exibirGraficos() {
        // Cria um mapa para armazenar a contagem de "Sim" e "Não" por pergunta
        Map<String, Map<String, Integer>> respostasPorPergunta = new HashMap<>();

        // Processa as respostas
        for (String resposta : respostasList) {
            String[] partes = resposta.split(":");  // Separar perguntaId e resposta
            if (partes.length == 2) {
                String perguntaId = partes[0];
                String respostaValor = partes[1];

                // Inicializa o mapa de respostas para a pergunta se necessário
                if (!respostasPorPergunta.containsKey(perguntaId)) {
                    respostasPorPergunta.put(perguntaId, new HashMap<>());
                }

                Map<String, Integer> contagemRespostas = respostasPorPergunta.get(perguntaId);

                // Conta as respostas "Sim" e "Não"
                contagemRespostas.put(respostaValor, contagemRespostas.getOrDefault(respostaValor, 0) + 1);
            }
        }

        // Para cada pergunta, gerar o gráfico com base nas respostas
        int index = 0;  // Para adicionar múltiplos gráficos em vez de um único
        for (String perguntaId : respostasPorPergunta.keySet()) {
            Map<String, Integer> contagemRespostas = respostasPorPergunta.get(perguntaId);
            int simCount = contagemRespostas.getOrDefault("Sim", 0);
            int naoCount = contagemRespostas.getOrDefault("Não", 0);

            // Gerar um novo gráfico para cada pergunta
            criarGrafico(simCount, naoCount, perguntaId, index);
            index++;
        }
    }

    private void criarGrafico(int simCount, int naoCount, String perguntaId, int index) {
        // Criação do gráfico
        PieChart pieChart = new PieChart(this);
        pieChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
        LinearLayout linearLayout = findViewById(R.id.linearLayoutGrafico);
        linearLayout.addView(pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();

        // Adiciona entradas para "Sim" e "Não" se houverem respostas
        if (simCount > 0) {
            entries.add(new PieEntry(simCount, "Sim"));
        }
        if (naoCount > 0) {
            entries.add(new PieEntry(naoCount, "Não"));
        }

        // Criação do dataset para o gráfico
        PieDataSet dataSet = new PieDataSet(entries, "Respostas para " + perguntaId);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.simColor)); // Cor para "Sim"
        colors.add(getResources().getColor(R.color.naoColor)); // Cor para "Não"
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Atualiza o gráfico
    }
}
