package com.dias.navios.model;

public class Tripulante {

    private int id;
    private String nome;
    private String numeroCertificado;
    private FuncaoTripulante funcao;
    private boolean disponivel;
    private String nacionalidade;

    public Tripulante() {}

    public Tripulante(int id, String nome, String numeroCertificado,
                      FuncaoTripulante funcao, boolean disponivel, String nacionalidade) {
        this.id = id;
        this.nome = nome;
        this.numeroCertificado = numeroCertificado;
        this.funcao = funcao;
        this.disponivel = disponivel;
        this.nacionalidade = nacionalidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String numeroCertificado) { this.numeroCertificado = numeroCertificado; }

    public FuncaoTripulante getFuncao() { return funcao; }
    public void setFuncao(FuncaoTripulante funcao) { this.funcao = funcao; }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }

    @Override
    public String toString() {
        return nome + " [" + funcao + "]";
    }
}
