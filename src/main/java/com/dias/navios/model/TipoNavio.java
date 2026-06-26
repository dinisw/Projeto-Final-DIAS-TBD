package com.dias.navios.model;

public enum TipoNavio {
    CRUDE,
    REFINADOS,
    QUIMICO,
    QUIMICO_PRODUTOS;

    public boolean aceitaCarga(TipoCarga tipoCarga) {
        return switch (this) {
            case CRUDE            -> tipoCarga == TipoCarga.PETROLEO_BRUTO;
            case REFINADOS        -> tipoCarga == TipoCarga.GASOLINA || tipoCarga == TipoCarga.DIESEL
                                     || tipoCarga == TipoCarga.JET_FUEL || tipoCarga == TipoCarga.FUELOLEO;
            case QUIMICO          -> tipoCarga == TipoCarga.QUIMICOS;
            case QUIMICO_PRODUTOS -> tipoCarga == TipoCarga.QUIMICOS || tipoCarga == TipoCarga.GASOLINA
                                     || tipoCarga == TipoCarga.DIESEL || tipoCarga == TipoCarga.JET_FUEL;
        };
    }

    public String descricaoCompativel() {
        return switch (this) {
            case CRUDE            -> "Petróleo Bruto";
            case REFINADOS        -> "Gasolina, Diesel, Jet Fuel, Fuel Óleo";
            case QUIMICO          -> "Químicos";
            case QUIMICO_PRODUTOS -> "Químicos, Gasolina, Diesel, Jet Fuel";
        };
    }
}
