package com.dias.navios.model;

public enum TipoNavio {
    CRUDE,
    REFINADO,
    QUIMICO,
    QUIMICO_PRODUTO;

    public boolean aceitaCarga(TipoCarga tipoCarga) {
        switch (this) {
            case CRUDE:
                return tipoCarga == TipoCarga.PETROLEO_BRUTO;
            case REFINADO:
                return tipoCarga == TipoCarga.GASOLINA || tipoCarga == TipoCarga.DIESEL
                        || tipoCarga == TipoCarga.JET_FUEL || tipoCarga == TipoCarga.FUELOLEO;
            case QUIMICO:
                return tipoCarga == TipoCarga.PRODUTO_QUIMICO;
            case QUIMICO_PRODUTO:
                return tipoCarga == TipoCarga.GASOLINA || tipoCarga == TipoCarga.DIESEL
                        || tipoCarga == TipoCarga.JET_FUEL || tipoCarga == TipoCarga.FUELOLEO
                        || tipoCarga == TipoCarga.PRODUTO_QUIMICO;
            default:
                return false;
        }
    }

    public String descricaoCompativel() {
        switch (this) {
            case CRUDE:          return "Petróleo Bruto";
            case REFINADO:       return "Gasolina, Diesel, Jet Fuel, Fuel Óleo";
            case QUIMICO:        return "Produto Químico";
            case QUIMICO_PRODUTO: return "Gasolina, Diesel, Jet Fuel, Fuel Óleo, Produto Químico";
            default:             return "";
        }
    }
}
