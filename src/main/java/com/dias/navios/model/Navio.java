package com.dias.navios.model;

public class Navio {

    private int id;
    private String nome;
    private String codigoIMO;
    private TipoNavio tipo;
    private double capacidadeMaxima;
    private int numTanques;
    private String bandeira;
    private int anoFabrico;
    private EstadoNavio estado;
    private int portoAtualId; // FK para Porto

    public Navio() {}

    public Navio(int id, String nome, String codigoIMO, TipoNavio tipo,
                 double capacidadeMaxima, int numTanques, String bandeira,
                 int anoFabrico, EstadoNavio estado, int portoAtualId) {
        this.id = id;
        this.nome = nome;
        this.codigoIMO = codigoIMO;
        this.tipo = tipo;
        this.capacidadeMaxima = capacidadeMaxima;
        this.numTanques = numTanques;
        this.bandeira = bandeira;
        this.anoFabrico = anoFabrico;
        this.estado = estado;
        this.portoAtualId = portoAtualId;
    }

    // TODO: adicionar getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigoIMO() { return codigoIMO; }
    public void setCodigoIMO(String codigoIMO) { this.codigoIMO = codigoIMO; }

    public TipoNavio getTipo() { return tipo; }
    public void setTipo(TipoNavio tipo) { this.tipo = tipo; }

    public double getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(double capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }

    public int getNumTanques() { return numTanques; }
    public void setNumTanques(int numTanques) { this.numTanques = numTanques; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public int getAnoFabrico() { return anoFabrico; }
    public void setAnoFabrico(int anoFabrico) { this.anoFabrico = anoFabrico; }

    public EstadoNavio getEstado() { return estado; }
    public void setEstado(EstadoNavio estado) { this.estado = estado; }

    public int getPortoAtualId() { return portoAtualId; }
    public void setPortoAtualId(int portoAtualId) { this.portoAtualId = portoAtualId; }

    @Override
    public String toString() {
        return nome + " (" + codigoIMO + ")";
    }
}
