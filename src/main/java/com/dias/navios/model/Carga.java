package com.dias.navios.model;

public class Carga {

    private int id;
    private String designacao;
    private TipoCarga tipo;
    private double volume;
    private double peso;
    private boolean inflamavel;
    private boolean corrosiva;
    private boolean toxica;
    private int portoCarregamentoId; // FK para Porto
    private int portoDescargaId;     // FK para Porto

    public Carga() {
    }

    public Carga(int id, String designacao, TipoCarga tipo, double volume, double peso,
                 boolean inflamavel, boolean corrosiva, boolean toxica,
                 int portoCarregamentoId, int portoDescargaId) {
        this.id = id;
        this.designacao = designacao;
        this.tipo = tipo;
        this.volume = volume;
        this.peso = peso;
        this.inflamavel = inflamavel;
        this.corrosiva = corrosiva;
        this.toxica = toxica;
        this.portoCarregamentoId = portoCarregamentoId;
        this.portoDescargaId = portoDescargaId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesignacao() {
        return designacao;
    }

    public void setDesignacao(String designacao) {
        this.designacao = designacao;
    }

    public TipoCarga getTipo() {
        return tipo;
    }

    public void setTipo(TipoCarga tipo) {
        this.tipo = tipo;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public boolean isInflamavel() {
        return inflamavel;
    }

    public void setInflamavel(boolean inflamavel) {
        this.inflamavel = inflamavel;
    }

    public boolean isCorrosiva() {
        return corrosiva;
    }

    public void setCorrosiva(boolean corrosiva) {
        this.corrosiva = corrosiva;
    }

    public boolean isToxica() {
        return toxica;
    }

    public void setToxica(boolean toxica) {
        this.toxica = toxica;
    }

    public int getPortoCarregamentoId() {
        return portoCarregamentoId;
    }

    public void setPortoCarregamentoId(int portoCarregamentoId) {
        this.portoCarregamentoId = portoCarregamentoId;
    }

    public int getPortoDescargaId() {
        return portoDescargaId;
    }

    public void setPortoDescargaId(int portoDescargaId) {
        this.portoDescargaId = portoDescargaId;
    }

    @Override
    public String toString() {
        return designacao + " [" + tipo + "]";
    }
}
