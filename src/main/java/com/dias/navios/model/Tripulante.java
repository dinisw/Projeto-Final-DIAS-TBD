package com.dias.navios.model;

import java.time.LocalDate;

public class Tripulante {

    private int id;
    private String nome;
    private String numeroCertificado;
    private FuncaoTripulante funcao;
    private String estadoDisponibilidade;
    private LocalDate dataNascimento;
    private String email;

    public Tripulante() {}

    public Tripulante(int id, String nome, String numeroCertificado,
                      FuncaoTripulante funcao, String estadoDisponibilidade,
                      LocalDate dataNascimento, String email) {
        this.id = id;
        this.nome = nome;
        this.numeroCertificado = numeroCertificado;
        this.funcao = funcao;
        this.estadoDisponibilidade = estadoDisponibilidade;
        this.dataNascimento = dataNascimento;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String numeroCertificado) { this.numeroCertificado = numeroCertificado; }

    public FuncaoTripulante getFuncao() { return funcao; }
    public void setFuncao(FuncaoTripulante funcao) { this.funcao = funcao; }

    public String getEstadoDisponibilidade() { return estadoDisponibilidade; }
    public void setEstadoDisponibilidade(String estado) { this.estadoDisponibilidade = estado; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return nome + " [" + funcao + "]";
    }
}
