package com.dias.navios.model;

/**
 * Tipos de carga liquida suportados.
 *
 * As propriedades de perigo (inflamavel / corrosiva / toxica) sao uma
 * caracteristica do TIPO de carga, nao de cada carga individual — por isso
 * vivem aqui (e na tabela TipoCarga da BD), e nao na classe {@link Carga}.
 * Os valores espelham exatamente o seed do script SQL (TipoCarga).
 */
public enum TipoCarga {
    PETROLEO_BRUTO(true,  false, true),
    GASOLINA      (true,  false, true),
    DIESEL        (true,  false, false),
    JET_FUEL      (true,  false, false),
    FUELOLEO      (true,  false, false),
    QUIMICOS      (false, true,  true);

    private final boolean inflamavel;
    private final boolean corrosiva;
    private final boolean toxica;

    TipoCarga(boolean inflamavel, boolean corrosiva, boolean toxica) {
        this.inflamavel = inflamavel;
        this.corrosiva  = corrosiva;
        this.toxica     = toxica;
    }

    public boolean isInflamavel() { return inflamavel; }
    public boolean isCorrosiva()  { return corrosiva; }
    public boolean isToxica()     { return toxica; }
}
