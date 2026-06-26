CREATE TABLE Porto (
    id INT IDENTITY(1,1) NOT NULL,
    nome NVARCHAR(100) NOT NULL,
    pais NVARCHAR(100) NOT NULL,
    codigo NVARCHAR(10) NOT NULL,
    CONSTRAINT PK_Porto PRIMARY KEY (id),
    CONSTRAINT UQ_Porto_codigo UNIQUE (codigo)
);

CREATE TABLE TipoNavio (
    id INT IDENTITY(1,1) NOT NULL,
    nome NVARCHAR(100) NOT NULL,
    maxCargas INT NOT NULL,
    CONSTRAINT PK_TipoNavio PRIMARY KEY (id),
    CONSTRAINT UQ_TipoNavio_nome UNIQUE (nome),
    CONSTRAINT CK_TipoNavio_maxCargas CHECK (maxCargas > 0)
);

CREATE TABLE TipoCarga (
    id INT IDENTITY(1,1) NOT NULL,
    nome NVARCHAR(100) NOT NULL,
    inflamavel BIT NOT NULL DEFAULT 0,
    corrosiva BIT NOT NULL DEFAULT 0,
    toxica BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_TipoCarga PRIMARY KEY (id),
    CONSTRAINT UQ_TipoCarga_nome UNIQUE (nome)
);

CREATE TABLE TipoNavioTipoCarga (
    tipoNavioId INT NOT NULL,
    tipoCargaId INT NOT NULL,
    CONSTRAINT PK_TipoNavioTipoCarga PRIMARY KEY (tipoNavioId, tipoCargaId),
    CONSTRAINT FK_TipoNavioTipoCarga_TipoNavio FOREIGN KEY (tipoNavioId) REFERENCES TipoNavio (id),
    CONSTRAINT FK_TipoNavioTipoCarga_TipoCarga FOREIGN KEY (tipoCargaId) REFERENCES TipoCarga (id)
);

CREATE TABLE Navio (
    id INT IDENTITY(1,1) NOT NULL,
    nome NVARCHAR(100) NOT NULL,
    codigoIMO NVARCHAR(20) NOT NULL,
    tipoNavioId INT NOT NULL,
    capacidadeMaxima DECIMAL(12,2) NOT NULL,
    numCompartimentos INT NOT NULL,
    bandeira NVARCHAR(100) NOT NULL,
    anoFabrico INT NOT NULL,
    estadoOperacional NVARCHAR(20) NOT NULL CONSTRAINT DF_Navio_estado DEFAULT 'ATIVO',
    portoAtualId INT NULL,
    CONSTRAINT PK_Navio PRIMARY KEY (id),
    CONSTRAINT UQ_Navio_codigoIMO UNIQUE (codigoIMO),
    CONSTRAINT FK_Navio_TipoNavio FOREIGN KEY (tipoNavioId) REFERENCES TipoNavio (id),
    CONSTRAINT FK_Navio_Porto FOREIGN KEY (portoAtualId) REFERENCES Porto (id),
    CONSTRAINT CK_Navio_capacidade CHECK (capacidadeMaxima > 0),
    CONSTRAINT CK_Navio_compartimentos CHECK (numCompartimentos > 0),
    CONSTRAINT CK_Navio_anoFabrico CHECK (anoFabrico BETWEEN 1900 AND 2100),
    CONSTRAINT CK_Navio_estado CHECK (estadoOperacional IN ('ATIVO', 'MANUTENCAO', 'INATIVO'))
);

CREATE TABLE Tripulante (
    id INT IDENTITY(1,1) NOT NULL,
    nome NVARCHAR(100) NOT NULL,
    dataNascimento DATE NOT NULL,
    email NVARCHAR(150) NOT NULL,
    numCertificado NVARCHAR(50) NOT NULL,
    funcao NVARCHAR(20) NOT NULL,
    estadoDisponibilidade NVARCHAR(20) NOT NULL CONSTRAINT DF_Tripulante_estado DEFAULT 'DISPONIVEL',
    CONSTRAINT PK_Tripulante PRIMARY KEY (id),
    CONSTRAINT UQ_Tripulante_email UNIQUE (email),
    CONSTRAINT UQ_Tripulante_numCertificado UNIQUE (numCertificado),
    CONSTRAINT CK_Tripulante_funcao CHECK (funcao IN ('CAPITAO', 'OFICIAL', 'ENGENHEIRO', 'OPERADOR')),
    CONSTRAINT CK_Tripulante_estado CHECK (estadoDisponibilidade IN ('DISPONIVEL', 'EM_VIAGEM', 'INDISPONIVEL'))
);

CREATE TABLE Viagem (
    id INT IDENTITY(1,1) NOT NULL,
    navioId INT NOT NULL,
    portoOrigemId INT NOT NULL,
    portoDestinoId INT NOT NULL,
    dataPartida DATE NOT NULL,
    dataChegadaPrevista DATE NOT NULL,
    dataChegadaReal DATE NULL,
    estado NVARCHAR(20) NOT NULL CONSTRAINT DF_Viagem_estado DEFAULT 'PLANEADA',
    createdAt DATETIME2 NOT NULL CONSTRAINT DF_Viagem_createdAt DEFAULT GETDATE(),
    CONSTRAINT PK_Viagem PRIMARY KEY (id),
    CONSTRAINT FK_Viagem_Navio FOREIGN KEY (navioId) REFERENCES Navio (id),
    CONSTRAINT FK_Viagem_PortoOrigem FOREIGN KEY (portoOrigemId) REFERENCES Porto (id),
    CONSTRAINT FK_Viagem_PortoDestino FOREIGN KEY (portoDestinoId) REFERENCES Porto (id),
    CONSTRAINT CK_Viagem_estado CHECK (estado IN ('PLANEADA', 'EM_CURSO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT CK_Viagem_datas CHECK (dataChegadaPrevista >= dataPartida),
    CONSTRAINT CK_Viagem_portosDistintos CHECK (portoOrigemId <> portoDestinoId)
);

CREATE TABLE Carga (
    id INT IDENTITY(1,1) NOT NULL,
    designacao NVARCHAR(200) NOT NULL,
    tipoCargaId INT NOT NULL,
    volume DECIMAL(12,2) NOT NULL, 
    peso DECIMAL(12,2) NOT NULL,
    portoCargoId INT NOT NULL,
    portoDescargoId INT NOT NULL,
    CONSTRAINT PK_Carga PRIMARY KEY (id),
    CONSTRAINT FK_Carga_TipoCarga FOREIGN KEY (tipoCargaId) REFERENCES TipoCarga (id),
    CONSTRAINT FK_Carga_PortoCargo FOREIGN KEY (portoCargoId) REFERENCES Porto (id),
    CONSTRAINT FK_Carga_PortoDescargo FOREIGN KEY (portoDescargoId) REFERENCES Porto (id),
    CONSTRAINT CK_Carga_volume CHECK (volume > 0),
    CONSTRAINT CK_Carga_peso CHECK (peso > 0)
);

CREATE TABLE ViagemCarga (
    viagemId INT NOT NULL,
    cargaId  INT NOT NULL,
    CONSTRAINT PK_ViagemCarga PRIMARY KEY (viagemId, cargaId),
    CONSTRAINT FK_ViagemCarga_Viagem FOREIGN KEY (viagemId) REFERENCES Viagem (id),
    CONSTRAINT FK_ViagemCarga_Carga FOREIGN KEY (cargaId) REFERENCES Carga (id)
);

CREATE TABLE ViagemTripulante (
    viagemId INT NOT NULL,
    tripulanteId INT NOT NULL,
    funcaoNaViagem NVARCHAR(20) NOT NULL,
    CONSTRAINT PK_ViagemTripulante PRIMARY KEY (viagemId, tripulanteId),
    CONSTRAINT FK_ViagemTripulante_Viagem FOREIGN KEY (viagemId) REFERENCES Viagem (id),
    CONSTRAINT FK_ViagemTripulante_Tripulante FOREIGN KEY (tripulanteId) REFERENCES Tripulante (id),
    CONSTRAINT CK_ViagemTripulante_funcao CHECK (funcaoNaViagem IN ('CAPITAO', 'OFICIAL', 'ENGENHEIRO', 'OPERADOR'))
);

CREATE INDEX IX_Navio_tipoNavioId         ON Navio (tipoNavioId);
CREATE INDEX IX_Navio_estadoOperacional   ON Navio (estadoOperacional);
CREATE INDEX IX_Navio_portoAtualId        ON Navio (portoAtualId);

CREATE INDEX IX_Viagem_navioId            ON Viagem (navioId);
CREATE INDEX IX_Viagem_estado             ON Viagem (estado);
CREATE INDEX IX_Viagem_dataPartida        ON Viagem (dataPartida);
CREATE INDEX IX_Viagem_portoOrigemId      ON Viagem (portoOrigemId);
CREATE INDEX IX_Viagem_portoDestinoId     ON Viagem (portoDestinoId);

CREATE INDEX IX_ViagemCarga_cargaId       ON ViagemCarga (cargaId);
CREATE INDEX IX_VT_tripulanteId           ON ViagemTripulante (tripulanteId);

CREATE INDEX IX_Carga_tipoCargaId         ON Carga (tipoCargaId);
CREATE INDEX IX_Tripulante_estado         ON Tripulante (estadoDisponibilidade);


INSERT INTO TipoNavio (nome, maxCargas) VALUES
    ('CRUDE',            1),   -- Apenas petróleo bruto; um único tipo
    ('REFINADOS',        4),   -- Gasolina, diesel, jet fuel, fuelóleo
    ('QUIMICO',          5),   -- Vários produtos químicos líquidos
    ('QUIMICO_PRODUTOS', 4);   -- Híbrido: químicos + alguns refinados

INSERT INTO TipoCarga (nome, inflamavel, corrosiva, toxica) VALUES
    ('PETROLEO_BRUTO', 1, 0, 1),
    ('GASOLINA',       1, 0, 1),
    ('DIESEL',         1, 0, 0),
    ('JET_FUEL',       1, 0, 0),
    ('FUELOLEO',       1, 0, 0),
    ('QUIMICOS',       0, 1, 1);

-- Compatibilidade tipo navio <-> tipo carga
-- CRUDE (id=1): só petróleo bruto
INSERT INTO TipoNavioTipoCarga VALUES (1, 1);
-- REFINADOS (id=2): gasolina, diesel, jet fuel, fuelóleo
INSERT INTO TipoNavioTipoCarga VALUES (2, 2), (2, 3), (2, 4), (2, 5);
-- QUIMICO (id=3): apenas produtos químicos
INSERT INTO TipoNavioTipoCarga VALUES (3, 6);
-- QUIMICO_PRODUTOS (id=4): químicos + gasolina + diesel + jet fuel
INSERT INTO TipoNavioTipoCarga VALUES (4, 6), (4, 2), (4, 3), (4, 4);

-- ============================================================
-- SECÇÃO 4: VIEWS
-- ============================================================

-- View 1: Navios disponíveis para nova viagem (ativos e sem viagem ativa)
CREATE VIEW vw_NaviosDisponiveis AS
SELECT
    n.id,
    n.nome,
    n.codigoIMO,
    tn.nome           AS tipoNavio,
    n.capacidadeMaxima,
    n.numCompartimentos,
    n.bandeira,
    n.anoFabrico,
    p.nome            AS portoAtual,
    p.pais            AS paisPortoAtual
FROM  Navio n
INNER JOIN TipoNavio tn ON tn.id = n.tipoNavioId
LEFT  JOIN Porto      p  ON p.id  = n.portoAtualId
WHERE n.estadoOperacional = 'ATIVO'
  AND NOT EXISTS (
        SELECT 1 FROM Viagem v
        WHERE  v.navioId = n.id
          AND  v.estado  IN ('PLANEADA', 'EM_CURSO')
      );
GO

-- View 2: Viagens ativas com contagens de cargas e tripulantes
CREATE VIEW vw_ViagensAtivas AS
SELECT
    v.id                           AS viagemId,
    v.estado,
    n.nome                         AS navio,
    n.codigoIMO,
    po.nome                        AS portoOrigem,
    pd.nome                        AS portoDestino,
    v.dataPartida,
    v.dataChegadaPrevista,
    COUNT(DISTINCT vc.cargaId)     AS numCargas,
    COUNT(DISTINCT vt.tripulanteId) AS numTripulantes,
    dbo.fn_PesoTotalCargasViagem(v.id) AS pesoTotalCargas
FROM  Viagem v
INNER JOIN Navio  n  ON n.id  = v.navioId
INNER JOIN Porto  po ON po.id = v.portoOrigemId
INNER JOIN Porto  pd ON pd.id = v.portoDestinoId
LEFT  JOIN ViagemCarga      vc ON vc.viagemId = v.id
LEFT  JOIN ViagemTripulante vt ON vt.viagemId = v.id
WHERE v.estado IN ('PLANEADA', 'EM_CURSO')
GROUP BY v.id, v.estado, n.nome, n.codigoIMO,
         po.nome, pd.nome, v.dataPartida, v.dataChegadaPrevista;
GO

-- View 3: Histórico completo de viagens por tripulante
CREATE VIEW vw_HistoricoViagensTripulante AS
SELECT
    t.id            AS tripulanteId,
    t.nome          AS tripulante,
    t.funcao        AS funcaoBase,
    vt.funcaoNaViagem,
    v.id            AS viagemId,
    v.estado        AS estadoViagem,
    n.nome          AS navio,
    po.nome         AS portoOrigem,
    pd.nome         AS portoDestino,
    v.dataPartida,
    v.dataChegadaReal
FROM  Tripulante      t
INNER JOIN ViagemTripulante vt ON vt.tripulanteId = t.id
INNER JOIN Viagem           v  ON v.id  = vt.viagemId
INNER JOIN Navio            n  ON n.id  = v.navioId
INNER JOIN Porto            po ON po.id = v.portoOrigemId
INNER JOIN Porto            pd ON pd.id = v.portoDestinoId;
GO

-- View 4: Estatísticas globais de utilização por navio
CREATE VIEW vw_EstatisticasNavios AS
SELECT
    n.id,
    n.nome,
    n.codigoIMO,
    tn.nome                                                          AS tipoNavio,
    n.estadoOperacional,
    COUNT(v.id)                                                      AS totalViagens,
    SUM(CASE WHEN v.estado = 'CONCLUIDA' THEN 1 ELSE 0 END)         AS viagensConcluidas,
    SUM(CASE WHEN v.estado = 'CANCELADA' THEN 1 ELSE 0 END)         AS viagensCanceladas,
    SUM(CASE WHEN v.estado IN ('PLANEADA','EM_CURSO') THEN 1 ELSE 0 END) AS viagensAtivas,
    MAX(v.dataPartida)                                               AS ultimaPartida
FROM  Navio n
INNER JOIN TipoNavio tn ON tn.id = n.tipoNavioId
LEFT  JOIN Viagem    v  ON v.navioId = n.id
GROUP BY n.id, n.nome, n.codigoIMO, tn.nome, n.estadoOperacional;
GO

-- View 5: Detalhe das cargas por viagem com propriedades de segurança
CREATE VIEW vw_CargasPorViagem AS
SELECT
    v.id            AS viagemId,
    v.estado        AS estadoViagem,
    n.nome          AS navio,
    c.id            AS cargaId,
    c.designacao,
    tc.nome         AS tipoCarga,
    tc.inflamavel,
    tc.corrosiva,
    tc.toxica,
    c.volume,
    c.peso,
    pc.nome         AS portoCarga,
    pd.nome         AS portoDescarga
FROM  Viagem         v
INNER JOIN Navio       n  ON n.id  = v.navioId
INNER JOIN ViagemCarga vc ON vc.viagemId = v.id
INNER JOIN Carga       c  ON c.id  = vc.cargaId
INNER JOIN TipoCarga   tc ON tc.id = c.tipoCargaId
INNER JOIN Porto       pc ON pc.id = c.portoCargoId
INNER JOIN Porto       pd ON pd.id = c.portoDescargoId;
GO

-- ============================================================
-- SECÇÃO 5: FUNÇÕES
-- ============================================================

-- Função 1: Verifica se um navio está disponível para nova viagem
--           Retorna 1 (disponível) ou 0 (indisponível)
CREATE FUNCTION fn_NavioDisponivel (@navioId INT)
RETURNS BIT
AS
BEGIN
    DECLARE @resultado    BIT         = 0;
    DECLARE @estado       NVARCHAR(20);
    DECLARE @viagensAtivas INT;

    SELECT @estado = estadoOperacional FROM Navio WHERE id = @navioId;

    IF @estado = 'ATIVO'
    BEGIN
        SELECT @viagensAtivas = COUNT(*)
        FROM   Viagem
        WHERE  navioId = @navioId
          AND  estado  IN ('PLANEADA', 'EM_CURSO');

        IF @viagensAtivas = 0
            SET @resultado = 1;
    END

    RETURN @resultado;
END;
GO

-- Função 2: Calcula o peso total (ton) das cargas associadas a uma viagem
CREATE FUNCTION fn_PesoTotalCargasViagem (@viagemId INT)
RETURNS DECIMAL(14,2)
AS
BEGIN
    DECLARE @pesoTotal DECIMAL(14,2);

    SELECT @pesoTotal = ISNULL(SUM(c.peso), 0)
    FROM   ViagemCarga vc
    INNER  JOIN Carga c ON c.id = vc.cargaId
    WHERE  vc.viagemId = @viagemId;

    RETURN @pesoTotal;
END;
GO

-- Função 3: Verifica compatibilidade entre um tipo de carga e um navio
--           Retorna 1 (compatível) ou 0 (incompatível)
CREATE FUNCTION fn_CargaCompativelComNavio (@navioId INT, @tipoCargaId INT)
RETURNS BIT
AS
BEGIN
    DECLARE @resultado  BIT = 0;
    DECLARE @tipoNavioId INT;

    SELECT @tipoNavioId = tipoNavioId FROM Navio WHERE id = @navioId;

    IF EXISTS (
        SELECT 1 FROM TipoNavioTipoCarga
        WHERE  tipoNavioId = @tipoNavioId
          AND  tipoCargaId  = @tipoCargaId
    )
        SET @resultado = 1;

    RETURN @resultado;
END;
GO

-- Função 4: Retorna o nº de viagens concluídas de um tripulante
CREATE FUNCTION fn_NumViagensConcluídasTripulante (@tripulanteId INT)
RETURNS INT
AS
BEGIN
    DECLARE @total INT;

    SELECT @total = COUNT(*)
    FROM   ViagemTripulante vt
    INNER  JOIN Viagem v ON v.id = vt.viagemId
    WHERE  vt.tripulanteId = @tripulanteId
      AND  v.estado = 'CONCLUIDA';

    RETURN ISNULL(@total, 0);
END;
GO

-- Função 5: Verifica se adicionar uma carga excede a capacidade do navio
--           Retorna 1 (excede) ou 0 (dentro da capacidade)
CREATE FUNCTION fn_CapacidadeExcedida (@viagemId INT, @pesoCargaNova DECIMAL(12,2))
RETURNS BIT
AS
BEGIN
    DECLARE @capacidadeMaxima DECIMAL(12,2);
    DECLARE @pesoAtual        DECIMAL(14,2);

    SELECT @capacidadeMaxima = n.capacidadeMaxima
    FROM   Viagem v
    INNER  JOIN Navio n ON n.id = v.navioId
    WHERE  v.id = @viagemId;

    SET @pesoAtual = dbo.fn_PesoTotalCargasViagem(@viagemId);

    IF (@pesoAtual + @pesoCargaNova) > @capacidadeMaxima
        RETURN 1;

    RETURN 0;
END;
GO

-- ============================================================
-- SECÇÃO 6: STORED PROCEDURES
-- ============================================================

-- SP 1: Registar um novo navio com todas as validações de negócio
CREATE PROCEDURE sp_RegistarNavio
    @nome              NVARCHAR(100),
    @codigoIMO         NVARCHAR(20),
    @tipoNavioId       INT,
    @capacidadeMaxima  DECIMAL(12,2),
    @numCompartimentos INT,
    @bandeira          NVARCHAR(100),
    @anoFabrico        INT,
    @portoAtualId      INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM TipoNavio WHERE id = @tipoNavioId)
    BEGIN
        RAISERROR('Tipo de navio inválido.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Navio WHERE codigoIMO = @codigoIMO)
    BEGIN
        RAISERROR('Já existe um navio com o código IMO fornecido.', 16, 1);
        RETURN;
    END

    IF @anoFabrico < 1900 OR @anoFabrico > YEAR(GETDATE())
    BEGIN
        RAISERROR('Ano de fabrico inválido (deve estar entre 1900 e o ano atual).', 16, 1);
        RETURN;
    END

    IF @portoAtualId IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM Porto WHERE id = @portoAtualId)
    BEGIN
        RAISERROR('Porto atual inválido.', 16, 1);
        RETURN;
    END

    INSERT INTO Navio (nome, codigoIMO, tipoNavioId, capacidadeMaxima, numCompartimentos,
                       bandeira, anoFabrico, estadoOperacional, portoAtualId)
    VALUES (@nome, @codigoIMO, @tipoNavioId, @capacidadeMaxima, @numCompartimentos,
            @bandeira, @anoFabrico, 'ATIVO', @portoAtualId);

    SELECT SCOPE_IDENTITY() AS novoNavioId;
END;
GO

-- SP 2: Criar uma nova viagem com validações completas
CREATE PROCEDURE sp_RegistarViagem
    @navioId             INT,
    @portoOrigemId       INT,
    @portoDestinoId      INT,
    @dataPartida         DATE,
    @dataChegadaPrevista DATE
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM Navio WHERE id = @navioId AND estadoOperacional = 'ATIVO')
    BEGIN
        RAISERROR('O navio não existe ou não está no estado ATIVO.', 16, 1);
        RETURN;
    END

    IF dbo.fn_NavioDisponivel(@navioId) = 0
    BEGIN
        RAISERROR('O navio já possui uma viagem ativa (PLANEADA ou EM_CURSO).', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Porto WHERE id = @portoOrigemId)
       OR NOT EXISTS (SELECT 1 FROM Porto WHERE id = @portoDestinoId)
    BEGIN
        RAISERROR('Porto de origem ou destino inválido.', 16, 1);
        RETURN;
    END

    IF @portoOrigemId = @portoDestinoId
    BEGIN
        RAISERROR('Porto de origem e porto de destino não podem ser iguais.', 16, 1);
        RETURN;
    END

    IF @dataChegadaPrevista < @dataPartida
    BEGIN
        RAISERROR('A data de chegada prevista não pode ser anterior à data de partida.', 16, 1);
        RETURN;
    END

    INSERT INTO Viagem (navioId, portoOrigemId, portoDestinoId,
                        dataPartida, dataChegadaPrevista, estado)
    VALUES (@navioId, @portoOrigemId, @portoDestinoId,
            @dataPartida, @dataChegadaPrevista, 'PLANEADA');

    SELECT SCOPE_IDENTITY() AS novaViagemId;
END;
GO

-- SP 3: Adicionar uma carga a uma viagem com validações de compatibilidade e capacidade
CREATE PROCEDURE sp_AdicionarCargaViagem
    @viagemId INT,
    @cargaId  INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @estadoViagem  NVARCHAR(20);
    DECLARE @navioId       INT;
    DECLARE @tipoCargaId   INT;
    DECLARE @pesoCarga     DECIMAL(12,2);
    DECLARE @tipoNavioId   INT;
    DECLARE @maxCargas     INT;
    DECLARE @numCargasAtual INT;

    SELECT @estadoViagem = estado, @navioId = navioId
    FROM   Viagem WHERE id = @viagemId;

    IF @estadoViagem IS NULL
    BEGIN
        RAISERROR('Viagem não encontrada.', 16, 1);
        RETURN;
    END

    IF @estadoViagem <> 'PLANEADA'
    BEGIN
        RAISERROR('Só é possível adicionar cargas a viagens no estado PLANEADA.', 16, 1);
        RETURN;
    END

    SELECT @tipoCargaId = tipoCargaId, @pesoCarga = peso
    FROM   Carga WHERE id = @cargaId;

    IF @tipoCargaId IS NULL
    BEGIN
        RAISERROR('Carga não encontrada.', 16, 1);
        RETURN;
    END

    IF dbo.fn_CargaCompativelComNavio(@navioId, @tipoCargaId) = 0
    BEGIN
        RAISERROR('O tipo de carga não é compatível com o tipo de navio.', 16, 1);
        RETURN;
    END

    -- Verificar limite máximo de cargas para o tipo de navio
    SELECT @tipoNavioId = tipoNavioId FROM Navio WHERE id = @navioId;
    SELECT @maxCargas   = maxCargas   FROM TipoNavio WHERE id = @tipoNavioId;
    SELECT @numCargasAtual = COUNT(*) FROM ViagemCarga WHERE viagemId = @viagemId;

    IF @numCargasAtual >= @maxCargas
    BEGIN
        RAISERROR('Número máximo de cargas para este tipo de navio atingido (%d).', 16, 1, @maxCargas);
        RETURN;
    END

    IF dbo.fn_CapacidadeExcedida(@viagemId, @pesoCarga) = 1
    BEGIN
        RAISERROR('A adição desta carga excederia a capacidade máxima do navio.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM ViagemCarga WHERE viagemId = @viagemId AND cargaId = @cargaId)
    BEGIN
        RAISERROR('Esta carga já está associada à viagem.', 16, 1);
        RETURN;
    END

    INSERT INTO ViagemCarga (viagemId, cargaId) VALUES (@viagemId, @cargaId);
END;
GO

-- SP 4: Atribuir um tripulante a uma viagem com validações
CREATE PROCEDURE sp_AtribuirTripulanteViagem
    @viagemId       INT,
    @tripulanteId   INT,
    @funcaoNaViagem NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @estadoViagem     NVARCHAR(20);
    DECLARE @estadoTripulante NVARCHAR(20);

    SELECT @estadoViagem = estado FROM Viagem WHERE id = @viagemId;

    IF @estadoViagem IS NULL
    BEGIN
        RAISERROR('Viagem não encontrada.', 16, 1);
        RETURN;
    END

    IF @estadoViagem <> 'PLANEADA'
    BEGIN
        RAISERROR('Só é possível atribuir tripulantes a viagens no estado PLANEADA.', 16, 1);
        RETURN;
    END

    SELECT @estadoTripulante = estadoDisponibilidade FROM Tripulante WHERE id = @tripulanteId;

    IF @estadoTripulante IS NULL
    BEGIN
        RAISERROR('Tripulante não encontrado.', 16, 1);
        RETURN;
    END

    IF @estadoTripulante <> 'DISPONIVEL'
    BEGIN
        RAISERROR('O tripulante não está disponível (estado atual: %s).', 16, 1, @estadoTripulante);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM ViagemTripulante WHERE viagemId = @viagemId AND tripulanteId = @tripulanteId)
    BEGIN
        RAISERROR('O tripulante já está atribuído a esta viagem.', 16, 1);
        RETURN;
    END

    -- Cada viagem só pode ter um capitão
    IF @funcaoNaViagem = 'CAPITAO'
       AND EXISTS (SELECT 1 FROM ViagemTripulante WHERE viagemId = @viagemId AND funcaoNaViagem = 'CAPITAO')
    BEGIN
        RAISERROR('Já existe um Capitão atribuído a esta viagem.', 16, 1);
        RETURN;
    END

    INSERT INTO ViagemTripulante (viagemId, tripulanteId, funcaoNaViagem)
    VALUES (@viagemId, @tripulanteId, @funcaoNaViagem);

    UPDATE Tripulante
    SET    estadoDisponibilidade = 'EM_VIAGEM'
    WHERE  id = @tripulanteId;
END;
GO

-- SP 5: Alterar o estado de uma viagem com validação de transições e efeitos colaterais
CREATE PROCEDURE sp_AlterarEstadoViagem
    @viagemId   INT,
    @novoEstado NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @estadoAtual    NVARCHAR(20);
    DECLARE @navioId        INT;
    DECLARE @portoDestinoId INT;

    SELECT @estadoAtual = estado, @navioId = navioId, @portoDestinoId = portoDestinoId
    FROM   Viagem WHERE id = @viagemId;

    IF @estadoAtual IS NULL
    BEGIN
        RAISERROR('Viagem não encontrada.', 16, 1);
        RETURN;
    END

    -- Validar transições permitidas
    IF NOT (
           (@estadoAtual = 'PLANEADA' AND @novoEstado IN ('EM_CURSO', 'CANCELADA'))
        OR (@estadoAtual = 'EM_CURSO' AND @novoEstado IN ('CONCLUIDA', 'CANCELADA'))
    )
    BEGIN
        RAISERROR('Transição de estado inválida: %s -> %s.', 16, 1, @estadoAtual, @novoEstado);
        RETURN;
    END

    -- Para iniciar a viagem é obrigatório ter Capitão e pelo menos uma carga
    IF @novoEstado = 'EM_CURSO'
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM ViagemTripulante
            WHERE  viagemId = @viagemId AND funcaoNaViagem = 'CAPITAO'
        )
        BEGIN
            RAISERROR('Não é possível iniciar uma viagem sem Capitão atribuído.', 16, 1);
            RETURN;
        END

        IF NOT EXISTS (SELECT 1 FROM ViagemCarga WHERE viagemId = @viagemId)
        BEGIN
            RAISERROR('Não é possível iniciar uma viagem sem cargas associadas.', 16, 1);
            RETURN;
        END
    END

    UPDATE Viagem
    SET    estado         = @novoEstado,
           dataChegadaReal = CASE WHEN @novoEstado = 'CONCLUIDA' THEN CAST(GETDATE() AS DATE)
                                  ELSE dataChegadaReal END
    WHERE  id = @viagemId;

    -- Ao concluir: actualizar porto do navio e libertar toda a tripulação
    IF @novoEstado = 'CONCLUIDA'
    BEGIN
        UPDATE Navio
        SET    portoAtualId = @portoDestinoId
        WHERE  id = @navioId;

        UPDATE Tripulante
        SET    estadoDisponibilidade = 'DISPONIVEL'
        WHERE  id IN (SELECT tripulanteId FROM ViagemTripulante WHERE viagemId = @viagemId);
    END

    -- Ao cancelar: libertar toda a tripulação
    IF @novoEstado = 'CANCELADA'
    BEGIN
        UPDATE Tripulante
        SET    estadoDisponibilidade = 'DISPONIVEL'
        WHERE  id IN (SELECT tripulanteId FROM ViagemTripulante WHERE viagemId = @viagemId);
    END
END;
GO

-- ============================================================
-- SECÇÃO 7: TRIGGERS
-- ============================================================

-- Trigger 1: Impede criação de viagem para navio indisponível ou com viagem ativa
CREATE TRIGGER trg_ValidarNavioAoCriarViagem
ON Viagem
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Impedir navio em MANUTENCAO ou INATIVO
    IF EXISTS (
        SELECT 1
        FROM   inserted i
        INNER  JOIN Navio n ON n.id = i.navioId
        WHERE  n.estadoOperacional IN ('MANUTENCAO', 'INATIVO')
    )
    BEGIN
        RAISERROR('Não é possível criar uma viagem para um navio em MANUTENCAO ou INATIVO.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Impedir segunda viagem ativa no mesmo navio
    IF EXISTS (
        SELECT 1
        FROM   inserted i
        INNER  JOIN Viagem v ON v.navioId = i.navioId AND v.id <> i.id
        WHERE  v.estado IN ('PLANEADA', 'EM_CURSO')
    )
    BEGIN
        RAISERROR('O navio já possui uma viagem ativa. Um navio só pode ter uma viagem ativa de cada vez.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

-- Trigger 2: Valida transições de estado diretamente na tabela Viagem
--            Complementa o SP garantindo a regra mesmo em atualizações diretas
CREATE TRIGGER trg_ValidarTransicaoEstadoViagem
ON Viagem
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(estado)
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM   inserted i
            INNER  JOIN deleted d ON d.id = i.id
            WHERE  NOT (
                   (d.estado = 'PLANEADA'  AND i.estado IN ('PLANEADA',  'EM_CURSO',  'CANCELADA'))
                OR (d.estado = 'EM_CURSO'  AND i.estado IN ('EM_CURSO',  'CONCLUIDA', 'CANCELADA'))
                OR (d.estado = 'CONCLUIDA' AND i.estado = 'CONCLUIDA')
                OR (d.estado = 'CANCELADA' AND i.estado = 'CANCELADA')
            )
        )
        BEGIN
            RAISERROR('Transição de estado de viagem inválida.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
    END
END;
GO

-- Trigger 3: Valida compatibilidade de carga e capacidade do navio ao associar carga a viagem
CREATE TRIGGER trg_ValidarCargaNaViagem
ON ViagemCarga
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @viagemId   INT;
    DECLARE @cargaId    INT;
    DECLARE @navioId    INT;
    DECLARE @tipoCargaId INT;
    DECLARE @pesoCarga  DECIMAL(12,2);
    DECLARE @capacidadeMaxima DECIMAL(12,2);
    DECLARE @pesoSemNovaCarga DECIMAL(14,2);

    SELECT @viagemId = viagemId, @cargaId = cargaId FROM inserted;

    SELECT @navioId = navioId FROM Viagem WHERE id = @viagemId;

    SELECT @tipoCargaId = tipoCargaId, @pesoCarga = peso FROM Carga WHERE id = @cargaId;

    -- Verificar compatibilidade carga/navio
    IF dbo.fn_CargaCompativelComNavio(@navioId, @tipoCargaId) = 0
    BEGIN
        RAISERROR('O tipo de carga não é compatível com este tipo de navio.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Verificar capacidade: somar peso das cargas já existentes (excluindo a recém-inserida)
    SELECT @pesoSemNovaCarga = ISNULL(SUM(c.peso), 0)
    FROM   ViagemCarga vc
    INNER  JOIN Carga c ON c.id = vc.cargaId
    WHERE  vc.viagemId = @viagemId AND vc.cargaId <> @cargaId;

    SELECT @capacidadeMaxima = n.capacidadeMaxima
    FROM   Navio n
    INNER  JOIN Viagem v ON v.navioId = n.id
    WHERE  v.id = @viagemId;

    IF (@pesoSemNovaCarga + @pesoCarga) > @capacidadeMaxima
    BEGIN
        RAISERROR('A carga associada excede a capacidade máxima do navio.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

-- Trigger 4: Liberta tripulante quando removido de uma viagem ainda ativa
CREATE TRIGGER trg_LiberarTripulanteAoRemover
ON ViagemTripulante
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Só liberta se a viagem ainda está ativa (não concluída/cancelada pelo SP)
    UPDATE Tripulante
    SET    estadoDisponibilidade = 'DISPONIVEL'
    FROM   Tripulante t
    INNER  JOIN deleted d ON d.tripulanteId = t.id
    INNER  JOIN Viagem  v ON v.id = d.viagemId
    WHERE  v.estado IN ('PLANEADA', 'EM_CURSO')
      AND  t.estadoDisponibilidade = 'EM_VIAGEM';
END;
GO

-- Trigger 5: Actualiza automaticamente o porto do navio ao concluir uma viagem
--            Actua como salvaguarda adicional além do SP sp_AlterarEstadoViagem
CREATE TRIGGER trg_AtualizarPortoNavioAoConcluir
ON Viagem
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(estado)
    BEGIN
        UPDATE Navio
        SET    portoAtualId = i.portoDestinoId
        FROM   Navio n
        INNER  JOIN inserted i ON i.navioId = n.id
        INNER  JOIN deleted  d ON d.id = i.id
        WHERE  i.estado = 'CONCLUIDA'
          AND  d.estado <> 'CONCLUIDA';
    END
END;
GO

-- ============================================================
-- FIM DO SCRIPT
-- ============================================================
