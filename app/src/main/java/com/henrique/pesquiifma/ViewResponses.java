package com.henrique.pesquiifma;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
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
import java.util.List;

public class ViewResponses extends AppCompatActivity {

    private List<String> respostasList;
    private TextView respostasTextView;
    private PieChart pieChart;
    private CheckBox checkSim, checkNao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);

        respostasTextView = findViewById(R.id.respostasTextView);
        pieChart = findViewById(R.id.pieChart);
        checkSim = findViewById(R.id.checkSim);
        checkNao = findViewById(R.id.checkNao);

        // Pega o formId passado da activity anterior
        String formId = getIntent().getStringExtra("formId");

        // Log para verificar o valor do formId
        Log.d("RespostasActivity", "formId recebido: " + formId);

        if (formId != null) {
            carregarRespostas(formId);
        } else {
            Toast.makeText(this, "Erro: Formulário não encontrado!", Toast.LENGTH_SHORT).show();
        }

        // Adiciona listeners aos CheckBoxes
        checkSim.setOnCheckedChangeListener((buttonView, isChecked) -> filtrarRespostas());
        checkNao.setOnCheckedChangeListener((buttonView, isChecked) -> filtrarRespostas());
    }

    private void carregarRespostas(String formId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar as respostas na subcoleção "respostas" usando o formId
        db.collection("formularios")  // A coleção onde as respostas estão armazenadas
                .document(formId)    // Usando o formId como documento para as respostas desse formulário
                .collection("respostas") // Subcoleção onde as respostas individuais estão
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    respostasList = new ArrayList<>();

                    // Processa as respostas
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> respostas = (List<String>) document.get("respostas");
                        if (respostas != null) {
                            respostasList.addAll(respostas);  // Adiciona todas as respostas
                        }
                    }

                    if (!respostasList.isEmpty()) {
                        StringBuilder respostasText = new StringBuilder();
                        for (String resposta : respostasList) {
                            respostasText.append(resposta).append("\n");
                        }
                        respostasTextView.setText(respostasText.toString());
                        filtrarRespostas();  // Mostrar as respostas com base nos filtros
                    } else {
                        respostasTextView.setText("Nenhuma resposta encontrada.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar as respostas!", Toast.LENGTH_SHORT).show();
                });
    }

    private void filtrarRespostas() {
        List<String> respostasFiltradas = new ArrayList<>();

        // Verifica se o filtro "Sim" está selecionado
        if (checkSim.isChecked()) {
            for (String resposta : respostasList) {
                if (resposta.equals("Sim")) {
                    respostasFiltradas.add(resposta);
                }
            }
        }

        // Verifica se o filtro "Não" está selecionado
        if (checkNao.isChecked()) {
            for (String resposta : respostasList) {
                if (resposta.equals("Não")) {
                    respostasFiltradas.add(resposta);
                }
            }
        }

        // Se nenhum filtro for selecionado, mostra todas as respostas
        if (!checkSim.isChecked() && !checkNao.isChecked()) {
            respostasFiltradas.addAll(respostasList);
        }

        // Atualiza o gráfico com as respostas filtradas
        atualizarGrafico(respostasFiltradas);
    }

    private void atualizarGrafico(List<String> respostasFiltradas) {
        int simCount = 0, naoCount = 0;

        // Contagem de respostas "Sim" e "Não"
        for (String resposta : respostasFiltradas) {
            if (resposta.equals("Sim")) {
                simCount++;
            } else if (resposta.equals("Não")) {
                naoCount++;
            }
        }

        // Criação das entradas para o gráfico
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Adiciona entradas para "Sim" e "Não" se houverem respostas
        if (simCount > 0) {
            entries.add(new PieEntry(simCount, "Sim"));
        }
        if (naoCount > 0) {
            entries.add(new PieEntry(naoCount, "Não"));
        }

        // Se não houver respostas para "Sim" ou "Não", mostra "Nenhuma resposta"
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "Nenhuma resposta"));
        }

        // Criação do dataset para o gráfico com cores diferentes para "Sim" e "Não"
        PieDataSet dataSet = new PieDataSet(entries, "Respostas");
        ArrayList<Integer> colors = new ArrayList<>();

        // Adiciona cores para "Sim" e "Não"
        colors.add(getResources().getColor(R.color.simColor)); // Cor para "Sim"
        colors.add(getResources().getColor(R.color.naoColor)); // Cor para "Não"

        dataSet.setColors(colors);  // Define as cores para o gráfico

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Atualiza o gráfico
    }
}