-- Base de dados: Sistema de Gestao de Navios Petroleiros
-- SQL Server

CREATE DATABASE navios_db;
GO

USE navios_db;
GO

CREATE TABLE portos (
    id     INT IDENTITY(1,1) PRIMARY KEY,
    nome   NVARCHAR(100) NOT NULL,
    pais   NVARCHAR(60)  NOT NULL,
    codigo NVARCHAR(10)  NOT NULL UNIQUE
);
GO

CREATE TABLE navios (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    nome              NVARCHAR(100) NOT NULL,
    codigo_imo        NVARCHAR(20)  NOT NULL UNIQUE,
    tipo              NVARCHAR(20)  NOT NULL CHECK (tipo IN ('CRUDE','REFINADO','QUIMICO','QUIMICO_PRODUTO')),
    capacidade_maxima FLOAT         NOT NULL,
    num_tanques       INT           NOT NULL,
    bandeira          NVARCHAR(60),
    ano_fabrico       INT,
    estado            NVARCHAR(20)  NOT NULL DEFAULT 'ATIVO' CHECK (estado IN ('ATIVO','EM_MANUTENCAO','INATIVO')),
    porto_atual_id    INT           NULL,
    FOREIGN KEY (porto_atual_id) REFERENCES portos(id)
);
GO

CREATE TABLE cargas (
    id                    INT IDENTITY(1,1) PRIMARY KEY,
    designacao            NVARCHAR(100) NOT NULL,
    tipo                  NVARCHAR(30)  NOT NULL CHECK (tipo IN ('PETROLEO_BRUTO','GASOLINA','DIESEL','JET_FUEL','FUELOLEO','PRODUTO_QUIMICO')),
    volume                FLOAT         NOT NULL,
    peso                  FLOAT         NOT NULL,
    inflamavel            BIT           NOT NULL DEFAULT 0,
    corrosiva             BIT           NOT NULL DEFAULT 0,
    toxica                BIT           NOT NULL DEFAULT 0,
    porto_carregamento_id INT           NULL,
    porto_descarga_id     INT           NULL,
    FOREIGN KEY (porto_carregamento_id) REFERENCES portos(id),
    FOREIGN KEY (porto_descarga_id)     REFERENCES portos(id)
);
GO

CREATE TABLE tripulantes (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    nome               NVARCHAR(100) NOT NULL,
    numero_certificado NVARCHAR(50)  NOT NULL UNIQUE,
    funcao             NVARCHAR(20)  NOT NULL CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    disponivel         BIT           NOT NULL DEFAULT 1,
    nacionalidade      NVARCHAR(60)
);
GO

CREATE TABLE viagens (
    id                    INT IDENTITY(1,1) PRIMARY KEY,
    porto_origem_id       INT  NOT NULL,
    porto_destino_id      INT  NOT NULL,
    data_partida          DATE NOT NULL,
    data_chegada_prevista DATE NULL,
    navio_id              INT  NOT NULL,
    estado                NVARCHAR(20) NOT NULL DEFAULT 'PLANEADA' CHECK (estado IN ('PLANEADA','EM_CURSO','CONCLUIDA','CANCELADA')),
    FOREIGN KEY (porto_origem_id)  REFERENCES portos(id),
    FOREIGN KEY (porto_destino_id) REFERENCES portos(id),
    FOREIGN KEY (navio_id)         REFERENCES navios(id)
);
GO

-- Associacao: uma viagem tem varias cargas
CREATE TABLE viagem_carga (
    viagem_id INT NOT NULL,
    carga_id  INT NOT NULL,
    PRIMARY KEY (viagem_id, carga_id),
    FOREIGN KEY (viagem_id) REFERENCES viagens(id),
    FOREIGN KEY (carga_id)  REFERENCES cargas(id)
);
GO

-- Associacao: uma viagem tem varios tripulantes
CREATE TABLE viagem_tripulante (
    viagem_id     INT NOT NULL,
    tripulante_id INT NOT NULL,
    PRIMARY KEY (viagem_id, tripulante_id),
    FOREIGN KEY (viagem_id)     REFERENCES viagens(id),
    FOREIGN KEY (tripulante_id) REFERENCES tripulantes(id)
);
GO
