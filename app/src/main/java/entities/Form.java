package entities;

import java.util.List;

public class Form {
    private String titulo;
    private String descricao;
    private List<String> perguntas;

    public Form() {
    }
    public Form(String titulo, String descricao, List<String> perguntas) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.perguntas = perguntas;
    }

    // Getters e Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<String> perguntas) {
        this.perguntas = perguntas;
    }
}
