package com.dias.navios.model;

public enum TipoNavio {
    CRUDE,
    REFINADOS,
    QUIMICO,
    QUIMICO_PRODUTOS;

    public boolean aceitaCarga(TipoCarga tipoCarga) {
        switch (this) {
            case CRUDE:
                return tipoCarga == TipoCarga.PETROLEO_BRUTO;
            case REFINADOS:
                return tipoCarga == TipoCarga.GASOLINA || tipoCarga == TipoCarga.DIESEL
                        || tipoCarga == TipoCarga.JET_FUEL || tipoCarga == TipoCarga.FUELOLEO;
            case QUIMICO:
                return tipoCarga == TipoCarga.QUIMICOS;
            case QUIMICO_PRODUTOS:
                return tipoCarga == TipoCarga.QUIMICOS || tipoCarga == TipoCarga.GASOLINA
                        || tipoCarga == TipoCarga.DIESEL || tipoCarga == TipoCarga.JET_FUEL;
            default:
                return false;
        }
    }

    public String descricaoCompativel() {
        switch (this) {
            case CRUDE:           return "Petróleo Bruto";
            case REFINADOS:       return "Gasolina, Diesel, Jet Fuel, Fuel Óleo";
            case QUIMICO:         return "Químicos";
            case QUIMICO_PRODUTOS: return "Químicos, Gasolina, Diesel, Jet Fuel";
            default:              return "";
        }
    }
}
