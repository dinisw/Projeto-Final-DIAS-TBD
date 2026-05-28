package com.dias.navios.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Viagem {

    private int id;
    private int portoOrigemId;   // FK para Porto
    private int portoDestinoId;  // FK para Porto
    private LocalDate dataPartida;
    private LocalDate dataChegadaPrevista;
    private int navioId;         // FK para Navio
    private EstadoViagem estado;
    private List<Integer> cargasIds;      // lista de IDs de Carga
    private List<Integer> tripulantesIds; // lista de IDs de Tripulante

    public Viagem() {
        cargasIds = new ArrayList<>();
        tripulantesIds = new ArrayList<>();
    }

    public Viagem(int id, int portoOrigemId, int portoDestinoId,
                  LocalDate dataPartida, LocalDate dataChegadaPrevista,
                  int navioId, EstadoViagem estado) {
        this.id = id;
        this.portoOrigemId = portoOrigemId;
        this.portoDestinoId = portoDestinoId;
        this.dataPartida = dataPartida;
        this.dataChegadaPrevista = dataChegadaPrevista;
        this.navioId = navioId;
        this.estado = estado;
        this.cargasIds = new ArrayList<>();
        this.tripulantesIds = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPortoOrigemId() { return portoOrigemId; }
    public void setPortoOrigemId(int portoOrigemId) { this.portoOrigemId = portoOrigemId; }

    public int getPortoDestinoId() { return portoDestinoId; }
    public void setPortoDestinoId(int portoDestinoId) { this.portoDestinoId = portoDestinoId; }

    public LocalDate getDataPartida() { return dataPartida; }
    public void setDataPartida(LocalDate dataPartida) { this.dataPartida = dataPartida; }

    public LocalDate getDataChegadaPrevista() { return dataChegadaPrevista; }
    public void setDataChegadaPrevista(LocalDate dataChegadaPrevista) { this.dataChegadaPrevista = dataChegadaPrevista; }

    public int getNavioId() { return navioId; }
    public void setNavioId(int navioId) { this.navioId = navioId; }

    public EstadoViagem getEstado() { return estado; }
    public void setEstado(EstadoViagem estado) { this.estado = estado; }

    public List<Integer> getCargasIds() { return cargasIds; }
    public void setCargasIds(List<Integer> cargasIds) { this.cargasIds = cargasIds; }

    public List<Integer> getTripulantesIds() { return tripulantesIds; }
    public void setTripulantesIds(List<Integer> tripulantesIds) { this.tripulantesIds = tripulantesIds; }

    @Override
    public String toString() {
        return "Viagem #" + id + " [" + estado + "] " + dataPartida;
    }
}
