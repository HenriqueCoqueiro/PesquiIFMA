package com.henrique.pesquiifma;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewResponses extends AppCompatActivity {

    private TextView perguntasTextView;
    private LinearLayout respostasContainer;
    private LinearLayout graficosContainer;
    private Switch switchModo;
    private Button btnAnterior, btnProxima;

    private List<Map<String, Object>> perguntas = new ArrayList<>();
    private List<List<String>> respostasPorUsuario = new ArrayList<>();
    private int indiceRespostaAtual = 0;

    private static final int[] ESTHETIC_COLORS = {
            Color.parseColor("#FFB6B9"),
            Color.parseColor("#FF677D"),
            Color.parseColor("#D4A5A5"),
            Color.parseColor("#392F5A"),
            Color.parseColor("#1D2D50"),
            Color.parseColor("#61C0BF"),
            Color.parseColor("#6B4226"),
            Color.parseColor("#D9BF77")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);

        perguntasTextView = findViewById(R.id.perguntasTextView);
        respostasContainer = findViewById(R.id.respostasContainer);
        graficosContainer = findViewById(R.id.graficosContainer);
        switchModo = findViewById(R.id.switchModo);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnProxima = findViewById(R.id.btnProxima);

        String formId = getIntent().getStringExtra("formId");

        if (formId != null) {
            carregarDadosFormulario(formId);
        } else {
            Toast.makeText(this, "Erro: Formulário não encontrado!", Toast.LENGTH_SHORT).show();
        }

        switchModo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            atualizarInterface();
        });

        btnAnterior.setOnClickListener(v -> {
            if (indiceRespostaAtual > 0) {
                indiceRespostaAtual--;
                atualizarInterface();
            }
        });

        btnProxima.setOnClickListener(v -> {
            if (indiceRespostaAtual < respostasPorUsuario.size() - 1) {
                indiceRespostaAtual++;
                atualizarInterface();
            }
        });
    }

    private void carregarDadosFormulario(String formId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("formularios").document(formId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    perguntas = (List<Map<String, Object>>) documentSnapshot.get("perguntas");
                    mostrarPerguntas();

                    db.collection("formularios").document(formId).collection("respostas")
                            .get()
                            .addOnSuccessListener(querySnapshots -> {
                                for (QueryDocumentSnapshot doc : querySnapshots) {
                                    List<String> respostas = (List<String>) doc.get("answers");
                                    respostasPorUsuario.add(respostas);
                                }
                                atualizarInterface();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao carregar respostas", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao carregar formulário", Toast.LENGTH_SHORT).show());
    }

    private void mostrarPerguntas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < perguntas.size(); i++) {
            Map<String, Object> pergunta = perguntas.get(i);
            sb.append(i + 1).append(". ").append(pergunta.get("pergunta")).append("\n");
        }
        perguntasTextView.setText(sb.toString());
    }

    private void atualizarInterface() {
        respostasContainer.removeAllViews();
        graficosContainer.removeAllViews();

        boolean modoUnico = switchModo.isChecked();
        btnAnterior.setVisibility(modoUnico ? View.VISIBLE : View.GONE);
        btnProxima.setVisibility(modoUnico ? View.VISIBLE : View.GONE);

        if (modoUnico) {
            if (!respostasPorUsuario.isEmpty()) {
                exibirRespostasUsuario(indiceRespostaAtual);
            }
        } else {
            for (int i = 0; i < respostasPorUsuario.size(); i++) {
                adicionarTitulo("Respostas do usuário " + (i + 1));
                exibirRespostasUsuario(i);
            }
        }

        mostrarGraficosSimNao();
    }

    private void exibirRespostasUsuario(int index) {
        List<String> respostas = respostasPorUsuario.get(index);
        for (int i = 0; i < perguntas.size(); i++) {
            String texto = (i + 1) + ". " + perguntas.get(i).get("pergunta") + "\nResposta: " + respostas.get(i);
            TextView tv = new TextView(this);
            tv.setText(texto);
            tv.setPadding(0, 16, 0, 16);
            respostasContainer.addView(tv);
        }
    }

    private void mostrarGraficosSimNao() {
        Map<Integer, Integer> contagemSim = new HashMap<>();
        Map<Integer, Integer> contagemNao = new HashMap<>();
        Map<Integer, Map<String, Integer>> contagemMultiplaEscolha = new HashMap<>();

        for (List<String> respostas : respostasPorUsuario) {
            for (int i = 0; i < perguntas.size(); i++) {
                String tipo = (String) perguntas.get(i).get("tipoResposta");
                String resposta = respostas.get(i);

                if (tipo.equalsIgnoreCase("Sim/Não")) {
                    if (resposta.equalsIgnoreCase("Sim")) {
                        contagemSim.put(i, contagemSim.getOrDefault(i, 0) + 1);
                    } else if (resposta.equalsIgnoreCase("Não")) {
                        contagemNao.put(i, contagemNao.getOrDefault(i, 0) + 1);
                    }
                } else if (tipo.equalsIgnoreCase("Múltipla Escolha")) {
                    // Separar as respostas múltiplas por vírgula e contar individualmente
                    String[] opcoesSelecionadas = resposta.split(",\\s*");
                    for (String opcao : opcoesSelecionadas) {
                        contagemMultiplaEscolha.putIfAbsent(i, new HashMap<>());
                        Map<String, Integer> contagemResposta = contagemMultiplaEscolha.get(i);
                        contagemResposta.put(opcao, contagemResposta.getOrDefault(opcao, 0) + 1);
                    }
                }
            }
        }

        for (int i = 0; i < perguntas.size(); i++) {
            String tipo = (String) perguntas.get(i).get("tipoResposta");
            if (tipo.equalsIgnoreCase("Sim/Não")) {
                int sim = contagemSim.getOrDefault(i, 0);
                int nao = contagemNao.getOrDefault(i, 0);
                adicionarTitulo((i + 1) + ". " + perguntas.get(i).get("pergunta"));
                criarGrafico(sim, nao);
            } else if (tipo.equalsIgnoreCase("Múltipla Escolha")) {
                Map<String, Integer> contagemResposta = contagemMultiplaEscolha.get(i);
                adicionarTitulo((i + 1) + ". " + perguntas.get(i).get("pergunta"));
                criarGraficoMultiplaEscolha(contagemResposta);
            }
        }
    }

    private void adicionarTitulo(String texto) {
        TextView titulo = new TextView(this);
        titulo.setText(texto);
        titulo.setTextSize(18f);
        titulo.setTextColor(Color.BLACK);
        titulo.setPadding(0, 32, 0, 8);
        graficosContainer.addView(titulo);
    }

    private void criarGrafico(int simCount, int naoCount) {
        PieChart pieChart = new PieChart(this);
        pieChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
        graficosContainer.addView(pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (simCount > 0) entries.add(new PieEntry(simCount, "Sim"));
        if (naoCount > 0) entries.add(new PieEntry(naoCount, "Não"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ESTHETIC_COLORS[0]);
        colors.add(ESTHETIC_COLORS[1]);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void criarGraficoMultiplaEscolha(Map<String, Integer> contagemRespostas) {
        PieChart pieChart = new PieChart(this);
        pieChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
        graficosContainer.addView(pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : contagemRespostas.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Respostas");
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            colors.add(ESTHETIC_COLORS[i % ESTHETIC_COLORS.length]);
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}