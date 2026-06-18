# Relatório de Análise — Sistema de Gestão de Navios Petroleiros

**Trabalho:** 2.º trabalho de DIAS 2025/2026
**Análise feita a:** 11/06/2026
**Base:** enunciado (`2_trabalho_DIAS_2026-texto.pdf`), modelos entregues (Diagrama de Casos de Uso, Diagrama de Classes, Modelo de Base de Dados) e código-fonte do repositório.

**Legenda de severidade:** 🔴 Crítico (impede o funcionamento) · 🟠 Importante (requisito do enunciado por cumprir) · 🟡 Menor (qualidade / boas práticas)

---

## 1. Resumo executivo

O projeto tem a **estrutura/arquitetura montada e coerente** (separação `model` / `dal` (DAO) / `bll` (Service) / `ui` (MVC com FXML), enums de domínio, esqueleto de Factory). A interface gráfica arranca e a navegação entre módulos funciona.

**Porém, o núcleo funcional está por implementar e a aplicação não persiste dados:**

1. A **ligação à base de dados está partida** — o `.env` e o `DatabaseConnection` não combinam (procura `DB_URL`, que não existe no `.env`).
2. **Quase todos os métodos de escrita dos DAO são stubs** — preparam o SQL mas **nunca preenchem os parâmetros nem executam** (`executeUpdate()` em falta). Resultado: o utilizador vê "guardado com sucesso" mas **nada é gravado**.
3. **Não há associação de cargas e tripulantes às viagens** (as tabelas de junção existem no SQL mas não são usadas no código).
4. **Regras de negócio centrais por implementar** (capacidade do navio, compatibilidade navio/carga).
5. **Inconsistências entre os modelos entregues e a implementação** (sobretudo `TipoNavio`/`TipoCarga` e a tabela de compatibilidade).

> Em resumo: a app **compila e abre**, mas **não funciona end-to-end** (não grava, não lê dados reais, e nem sequer liga à BD com a configuração atual).

---

## 2. Checklist — Enunciado vs. Implementado

| Requisito do enunciado | Estado | Notas |
|---|---|---|
| Aplicação Java + JavaFX | ✅ Feito | JavaFX 21, FXML, MVC |
| Persistência em BD relacional | 🔴 Parcial/Partido | SQL Server (JDBC). Ligação não funciona; DAO não gravam |
| Registar/editar/consultar **navios** | 🟠 Parcial | Consulta e insert por terminar; **editar não existe** na UI |
| Suportar **tipos de navio** | 🟠 Fraco | Apenas enum; sem nº máx. de cargas/tipo nem gestão própria |
| Registar/gerir **cargas** | 🟠 Parcial | Insert/Update por terminar |
| Suportar **tipos de carga** + propriedades (inflamável/corrosiva/tóxica) | 🟠 Parcial | Propriedades no sítio errado (em `Carga`, não em `TipoCarga`) |
| Criar/acompanhar **viagens** | 🟠 Parcial | Insert por terminar; estados OK; **sem cargas/tripulação associadas** |
| Associar navio + cargas + tripulação a viagens | 🔴 Em falta | Tabelas de junção não usadas; sem UI |
| **Tripulação** (registo, função, disponibilidade, histórico) | 🟠 Parcial | Insert/Update por terminar; **histórico inexistente** |
| Controlar disponibilidade dos navios | 🟠 Parcial | Existe `estado`, mas regra "1 viagem ativa" só verifica `EM_CURSO` |
| **Consultas e pesquisas** sobre a informação | 🔴 Em falta | Caso de uso "Consultar Informação" não implementado |
| Padrões de design (MVC, DAO) | ✅/🟡 | MVC e DAO presentes; **Factory criada mas nunca usada** |
| Interface: inserção/edição/consulta + mensagens de erro | 🟠 Parcial | Falta edição, tabelas vêm vazias, validação mínima |
| **Código SQL** da BD (entregável obrigatório) | 🟠 Frágil | Só existe em `target/` (build), não no código-fonte |
| Casos de uso "Gerir Tipo de Navio" | 🔴 Em falta | Sem UI/serviço/DAO |

---

## 3. 🔴 Erros e bugs (impedem o funcionamento)

### 3.1 Ligação à base de dados não funciona — incompatibilidade `.env` ↔ código
- `src/main/resources/.env` define `DB_SERVER`, `DB_DATABASE`, `DB_USER`, `DB_PASSWORD`.
- `DatabaseConnection.java:20` lê **`DB_URL`**, que **não existe** no `.env` → `url` fica `null` → `DriverManager.getConnection(null, ...)` falha.
- **Não há** construção do URL JDBC a partir de `DB_SERVER` + `DB_DATABASE`.

**Correção:** ou acrescentar ao `.env` uma linha
`DB_URL=jdbc:sqlserver://CTESPBD.DEI.ISEP.IPP.PT;databaseName=riczao;encrypt=true;trustServerCertificate=true`
ou construir o URL no código a partir de `DB_SERVER`/`DB_DATABASE`.

### 3.2 Caminho do `.env` errado em tempo de execução
- `DatabaseConnection.java:18`: `props.load(new FileInputStream(".env"));` procura `.env` na **pasta de trabalho do processo**, não no classpath. Ao correr com `mvn javafx:run`, a pasta corrente é a raiz do projeto, mas o ficheiro está em `src/main/resources/.env` → `FileNotFoundException`.
- **Correção:** usar `getClass().getResourceAsStream("/.env")` (e garantir que o `.env` é copiado para `target/classes`).

### 3.3 Métodos de escrita dos DAO são stubs — não gravam nada
Preparam o `PreparedStatement` mas **não preenchem parâmetros nem chamam `executeUpdate()`**:
- `NavioDAO.inserir()` (`NavioDAO.java:14`) e `NavioDAO.atualizar()` (`:23`)
- `CargaDAO.inserir()` (`CargaDAO.java:13`); `CargaDAO.atualizar()` (`:22`) está **completamente vazio**
- `ViagemDAO.inserir()` (`ViagemDAO.java:13`)
- `PortoDAO.inserir()` (`PortoDAO.java:12`)
- `TripulanteDAO.inserir()` (`TripulanteDAO.java:13`); `TripulanteDAO.atualizar()` (`:22`) **vazio**

**Efeito visível:** os controllers mostram "guardado com sucesso" e recarregam a tabela (vazia), dando a falsa impressão de funcionamento. As chaves geradas (`RETURN_GENERATED_KEYS`) também nunca são lidas.

### 3.4 Tabelas (TableView) aparecem sempre vazias
- Nenhuma coluna tem `cellValueFactory` configurado.
- `NavioController.initialize()` (`NavioController.java:35`) tem o TODO por fazer (`PropertyValueFactory` não configurado), apesar de as colunas terem `fx:id`.
- Em `cargas.fxml`, `viagens.fxml` e `tripulantes.fxml` as `<TableColumn>` **nem têm `fx:id`** e os respetivos controllers **não as declaram** → mesmo com dados, as tabelas mostrariam linhas em branco.

### 3.5 `NullPointerException` potencial em `ViagemService.criarViagem`
- `ViagemService.java:18`: `Navio navio = navioDAO.buscarPorId(...)` pode devolver `null` (id inexistente) e a linha seguinte faz `navio.getEstado()` → **NPE**. Falta validar `null`.

### 3.6 `NullPointerException` em `ViagemDAO.mapearResultSet`
- `ViagemDAO.java:97`: `rs.getDate("data_chegada_prevista").toLocalDate()`. No SQL, `data_chegada_prevista` é **`NULL`** permitido → se vier `null`, `.toLocalDate()` rebenta. Validar antes de converter.

### 3.7 Script SQL não corre no servidor da escola
- `schema.sql` faz `CREATE DATABASE navios_db; USE navios_db;`. No servidor partilhado `CTESPBD.DEI.ISEP.IPP.PT` cada grupo só tem a sua BD (`riczao`); não é possível criar/usar outra BD. Além disso o nome `navios_db` **≠** `riczao` do `.env`.
- **Correção:** remover `CREATE DATABASE`/`USE`, criar as tabelas diretamente na BD `riczao`.

---

## 4. 🟠 Funcionalidades em falta (face ao enunciado e aos casos de uso)

1. **Associação Viagem ↔ Cargas ↔ Tripulantes** — `Viagem` tem `cargasIds`/`tripulantesIds`, mas **nunca são gravados nem lidos**. As tabelas `viagem_carga` e `viagem_tripulante` não são usadas em nenhum DAO. Não há UI para escolher cargas/tripulantes de uma viagem.
2. **Edição (Update) na interface** — todos os módulos só têm **Guardar** (insert) e **Apagar**. Falta: selecionar uma linha → carregar no formulário → atualizar. Os métodos `editar...` dos serviços e `atualizar` dos DAO existem mas **não são acionáveis** pela UI (e estão por implementar).
3. **Gerir Tipo de Navio** (caso de uso do diagrama) — sem entidade rica, sem serviço, sem controller, sem FXML. O enunciado pede "número máximo de cargas por viagem e de que tipo" por tipo de navio — **não existe**.
4. **Gestão de Portos** — há `Porto` e um `PortoDAO` parcial, mas **sem** `PortoService`, **sem** controller e **sem** FXML. Os portos são referidos por ID escrito à mão nos formulários de viagem.
5. **Consultar Informação / pesquisas e filtros** (caso de uso) — inexistente.
6. **Histórico de participação do tripulante em viagens** (pedido no enunciado) — inexistente.
7. **Regra: capacidade do navio não pode ser excedida pelas cargas** — não implementada.
8. **Regra: compatibilidade entre tipo de navio e tipo de carga** — `ViagemService.java:30` é um TODO; não há sequer estrutura (tabela de compatibilidade) no código.
9. **Seleção de navio/porto por objeto** nas viagens — atualmente usam-se `TextField` de IDs (`campoNavioId`, `campoPortoOrigemId`, ...), o que é frágil e permite IDs inválidos. Deviam ser `ComboBox<Navio>` / `ComboBox<Porto>`.
10. **Validações e mensagens** — muito básicas (ex.: não se valida ano de fabrico, datas coerentes — chegada ≥ partida —, formato do IMO, etc.).

---

## 5. 🟠 Inconsistências entre os Modelos entregues e a Implementação

> Os modelos (entregáveis da 1.ª entrega) e o código **não estão alinhados** — e em alguns pontos **os próprios modelos divergem entre si**. Há que decidir uma fonte de verdade e alinhar tudo.

| Elemento | Diagrama de Classes | Modelo de BD (.jpeg) | Código + `schema.sql` |
|---|---|---|---|
| **TipoNavio** | Classe (`IDTipoNavio`, `Designacao`, `CapacidadeMax`) | Tabela `TIPO_NAVIO` (+`max_cargas_viagem`) | **`enum`** (4 valores) — perde-se capacidade/máx. cargas |
| **TipoCarga** | Classe (`Categoria`, `IsInflamavel`, **`IsExplosivo`**, `IsCorrosivo`) | Tabela `TIPO_CARGA` (`is_inflamavel`, `is_corrosiva`, **`is_toxica`**) | **`enum`**; propriedades movidas para `Carga` |
| **Compatibilidade navio/carga** | (implícita) | Tabela `COMPATIBILIDADE_NAVIO_CARGA` | **Não existe** |
| **Propriedades inflamável/corrosiva/tóxica** | em `TipoCarga` | em `TIPO_CARGA` | em **`Carga`** (campo errado) |
| **Porto** | `IDPorto, Nome, Localizacao` | `id_porto, nome, localizacao` | `id, nome, **pais**, **codigo**` (sem `localizacao`) |
| **Tripulante** | `Funcao`, `EstadoDisponibilidade` | `funcao`, `estado_disponibilidade` (string) | `funcao`, **`disponivel` (boolean)** + `numero_certificado` + `nacionalidade` |
| **Navio — porto atual** | (não tem) | (NAVIO não liga a PORTO) | tem **`porto_atual_id`** (correto face ao enunciado!) |
| **Nomes de tabelas/colunas** | — | `NAVIO`, `id_navio`, `id_tipo_navio`, `data_chegada_prev`... | `navios`, `id`, `tipo`, `data_chegada_prevista`... |

**Notas:**
- O enunciado fala em **inflamável, corrosiva e tóxica** — logo o Diagrama de Classes (que tem `IsExplosivo`) está desalinhado do enunciado; o Modelo de BD (`is_toxica`) está mais correto.
- O enunciado pede explicitamente o **porto atual/última localização do navio**: o código/`schema.sql` têm `porto_atual_id` (bem), mas o **Modelo de BD entregue não tem** essa ligação — o diagrama precisa de correção.
- A maior decisão de modelação: **`TipoNavio`/`TipoCarga` como `enum` vs. como entidade/tabela**. Como `enum`, é impossível guardar "nº máximo de cargas por viagem" e a "compatibilidade" pedidos no enunciado. Recomenda-se promover ambos a entidades/tabelas (como nos diagramas) **ou** assumir os `enum` e atualizar os diagramas — mas então perde-se parte dos requisitos.

---

## 6. Regras de Negócio (Secção 3 do enunciado) — estado

| Regra | Estado | Observação |
|---|---|---|
| Um navio só pode ter **uma viagem ativa** de cada vez | 🟠 Parcial | `ViagemDAO.navioTemViagemAtiva` só conta `EM_CURSO`; uma `PLANEADA` também ocupa o navio. E como `inserir` não grava, na prática nunca dispara |
| **Capacidade** do navio não excedida pelas cargas | 🔴 Em falta | Não implementada |
| **Compatibilidade** tipo de navio / tipo de carga | 🔴 Em falta | `ViagemService.java:30` (TODO) |
| Navios em **manutenção/inativos** não iniciam viagens | ✅ Feito | `ViagemService.java:21` e `NavioService.podeIniciarViagem` |
| Viagens evoluem por **estados lógicos** | ✅ Feito | `avancarEstado` (PLANEADA→EM_CURSO→CONCLUIDA), `cancelarViagem` |

---

## 7. 🟡 Qualidade, arquitetura e boas práticas

1. **Padrão Factory não usado** — `NavioFactory` e `CargaFactory` existem mas **nunca são invocados** (os controllers fazem `new Navio()`/`new Carga()` direto). Ou se integram (criar pelo tipo), ou são código morto. Além disso `CargaFactory` só cobre 3 dos 6 tipos de carga.
2. **Recursos JDBC sem fecho seguro** — `Connection`/`PreparedStatement`/`ResultSet` fechados manualmente, sem `try-with-resources`; em caso de exceção a meio, ficam por fechar (fugas de recursos/cursores no SQL Server).
3. **`Connection` singleton estático partilhado** — frágil para uma app com várias operações; uma exceção pode deixar a ligação num estado inconsistente.
4. **`target/` versionado no git (37 ficheiros) e sem `.gitignore`** — artefactos de build não deviam ir para o repositório. **Grave:** o `schema.sql` **só** existe em `target/classes/db/`, ou seja, a única cópia do entregável SQL é um artefacto de build. Deve estar em `src/main/resources/db/schema.sql` e versionado.
5. **Credenciais reais no repositório** — `.env` com utilizador/password da BD da escola está versionado (e aparece como modificado no `git status`). Devia estar em `.gitignore` (com um `.env.example` sem segredos).
6. **`.DS_Store` (macOS) versionados** — vários espalhados pelo projeto; juntar ao `.gitignore`.
7. **Sem testes** — nenhum teste unitário/integração.
8. **Relatório final** — entregável obrigatório da última entrega; ainda não existe no repositório.

---

## 8. Recomendações por prioridade

**Para a app funcionar (mínimo viável):**
1. Corrigir a ligação à BD (§3.1 e §3.2).
2. Implementar os métodos dos DAO (preencher parâmetros + `executeUpdate()` + ler `generated keys`) — §3.3.
3. Configurar as colunas das tabelas (`cellValueFactory` + `fx:id`) — §3.4.
4. Tratar os `null` em §3.5 e §3.6.
5. Mover `schema.sql` para `src/main/resources/db/`, remover `CREATE DATABASE`/`USE`, e versionar; remover `target/` do git + criar `.gitignore` — §3.7 e §7.4.

**Para cumprir o enunciado:**
6. Implementar associação Viagem↔Cargas↔Tripulantes (DAO + UI) — §4.1.
7. Implementar fluxo de **edição** em todos os módulos — §4.2.
8. Implementar regras de **capacidade** e **compatibilidade** — §6.
9. Acrescentar **Gestão de Portos** e **Gerir Tipo de Navio** (com máx. cargas/tipo) — §4.3/§4.4.
10. Implementar **Consultas/Pesquisas** e **histórico do tripulante** — §4.5/§4.6.

**Coerência do projeto:**
11. Decidir a fonte de verdade do modelo e **alinhar diagramas ↔ código ↔ SQL** (sobretudo `TipoNavio`/`TipoCarga`/compatibilidade) — §5.
12. Integrar (ou remover) o padrão Factory — §7.1.
13. Remover credenciais do repositório — §7.5.

---

## 9. Anexo — Inventário do código

- **Modelos:** `Navio`, `Carga`, `Viagem`, `Porto`, `Tripulante`; enums `TipoNavio`, `TipoCarga`, `EstadoNavio`, `EstadoViagem`, `FuncaoTripulante`.
- **DAO:** `NavioDAO`, `CargaDAO`, `ViagemDAO`, `PortoDAO`, `TripulanteDAO`, `DatabaseConnection`. (Sem DAO para tipos nem para junções.)
- **Serviços (BLL):** `NavioService`, `CargaService`, `ViagemService`, `TripulanteService`. (Sem `PortoService`.)
- **Factory:** `NavioFactory`, `CargaFactory` (não usadas).
- **UI:** `MainApp`, `MainController` + `NavioController`/`CargaController`/`ViagemController`/`TripulanteController`; FXML `main`, `navios`, `cargas`, `viagens`, `tripulantes`. (Sem UI de portos nem de tipos.)
- **SQL:** apenas `target/classes/db/schema.sql` (8 tabelas: `portos`, `navios`, `cargas`, `tripulantes`, `viagens`, `viagem_carga`, `viagem_tripulante`).
