# E-Sports League Manager

Trabajo final de **Lenguajes de Programación III** (UCSM).
Integrantes: **Walter Figueroa, Gabriel Cruz y Octavio Peña**.

Aplicación de escritorio en **Java Swing** para organizar una liga de e-sports "todos contra todos".
Las series son al mejor de 3 mapas (Bo3). El programa genera el calendario, simula partidas con tres estrategias intercambiables, permite deshacer y rehacer cualquier acción y guarda todo en **SQLite**.

---

## Cómo abrirlo en Eclipse

1. **File > Import > General > Existing Projects into Workspace**. Elegir la carpeta `esports-league-manager`; el proyecto se llama `ESportsLeagueManager`.
2. El driver `lib/sqlite-jdbc-3.42.0.0.jar` ya está en el Build Path.
   - Si Eclipse marca error, agrégalo a mano: clic derecho en el proyecto > **Build Path > Configure Build Path > Libraries > Add JARs…** > `lib/sqlite-jdbc-3.42.0.0.jar`.
3. Para ejecutar, clic derecho en `src/App.java` > **Run As > Java Application**.
   - La primera vez se crea el archivo `torneo.db` en la carpeta del proyecto.
4. Para probar el modelo sin ventana, clic derecho en `src/pruebas/PruebaModelo.java` > **Run As > Java Application**. Debe terminar con **«41 de 41 pruebas correctas»**.

> Con Java 24 o más nuevo puede aparecer en la consola un aviso `WARNING: A restricted method...`. Lo genera el driver de SQLite y no afecta en nada. Para quitarlo, usa la configuración incluida **Run > Run Configurations > ESportsLeagueManager**, que ya trae `--enable-native-access=ALL-UNNAMED`.

Requisitos: JDK 17 o superior. Probado con JDK 26.

---

## Cómo se usa

| Acción | Menú | Atajo |
|---|---|---|
| Agregar equipo (nombre + nivel 1–100) | Torneo | Ctrl+T |
| Cargar 8 equipos de ejemplo | Torneo | Ctrl+M |
| Eliminar equipo | Torneo | Supr |
| Simular la siguiente partida | Torneo | F5 |
| Simular la jornada actual | Torneo | F6 |
| Simular todo el torneo (barra de progreso) | Torneo | F7 |
| Cancelar la simulación | Torneo | Esc |
| Registrar un resultado a mano | doble clic en la pestaña Partidos | — |
| Deshacer / Rehacer | Editar | Ctrl+Z / Ctrl+Y |
| Reiniciar resultados (mismos equipos) | Torneo | Ctrl+R |
| Guardar en SQLite / Cargar | Archivo | Ctrl+S / Ctrl+O |
| Exportar a CSV | Archivo | Ctrl+E |
| Registrar campeón en el Salón de la Fama | Torneo | Ctrl+H |
| Cambiar de pestaña | Ver | Ctrl+1 … Ctrl+4 |

La estrategia de simulación se elige en el combo de la cabecera o en **Torneo > Estrategia de simulación**.

---

## Estructura del proyecto

```
src/
├─ App.java                  ← único lugar donde se crean y conectan Modelo, Vista y Controlador
├─ modelo/                   ← NO importa javax.swing ni imprime en consola
│   ├─ Torneo                (sujeto Observer + contexto Strategy + receptor Command)
│   ├─ Equipo, Partida, Campeon, Validador, Serie
│   ├─ TorneoListener        (interfaz Observer)
│   └─ EstrategiaSimulacion  (interfaz Strategy) + SimulacionPorNivel, SimulacionAleatoria, SimulacionPorRacha
├─ controlador/
│   ├─ TorneoController      (coordina vista, modelo, comandos y DAO)
│   ├─ SimulacionWorker      (SwingWorker con progreso y cancelación)
│   ├─ Comando, HistorialComandos (pilas deshacer / rehacer)
│   └─ AgregarEquipoCmd, EliminarEquipoCmd, SimularPartidaCmd, ComandoCompuesto,
│      ReiniciarResultadosCmd, NuevoTorneoCmd
├─ vista/
│   ├─ VentanaPrincipal      (JFrame, JMenuBar con atajos, JTabbedPane, JProgressBar)
│   ├─ PanelCabecera, PanelPosiciones, PanelPartidos, PanelEstadisticas, BarraEstado  (observadores)
│   ├─ TablaPosicionesModel, PartidosTableModel, CampeonesTableModel (AbstractTableModel)
│   ├─ PosicionRenderer, PartidoRenderer (renderers)
│   ├─ DialogoEquipo (JDialog), PanelCampeones, Sonido, Colores
├─ persistencia/
│   ├─ TorneoDAO             (SQLite + JDBC: PreparedStatement, transacciones, GROUP BY)
│   └─ ExportadorCsv
├─ excepciones/              TorneoException, EquipoInvalidoException, EquipoDuplicadoException,
│                            ResultadoInvalidoException, PersistenciaException
├─ util/PilaGenerica<T>      (Generics, la usa HistorialComandos)
└─ pruebas/PruebaModelo      (41 pruebas por consola, sin ventana)
```

---

## Dónde está cada requisito (rúbrica)

| Requisito | Dónde |
|---|---|
| **Swing básico** | JFrame, JPanel, JLabel, JButton, JTextField, JComboBox, JSpinner, JSlider, JList, JCheckBoxMenuItem; BorderLayout, FlowLayout, GridLayout, GridBagLayout, BoxLayout; JOptionPane para validar y confirmar |
| **Swing avanzado** | 3 JTable con AbstractTableModel propio, 2 renderers, TableRowSorter y RowFilter; JMenuBar con atajos y mnemónicos; JTabbedPane; JFileChooser; SwingWorker + JProgressBar con cancelación; gráfico propio con Graphics2D; sonido con Clip |
| **Observer** | `TorneoListener`. Lo implementan 6 clases: PanelCabecera, PanelPosiciones, PanelPartidos, PanelEstadisticas, BarraEstado y VentanaPrincipal. `Torneo` tiene `addListener` y `removeListener` y notifica después de cada cambio |
| **Strategy** | `EstrategiaSimulacion`, con 3 algoritmos distintos elegibles en tiempo de ejecución. `Torneo` no tiene ningún if/switch sobre el tipo |
| **Command** | `Comando` con `ejecutar()` y `deshacer()`. `HistorialComandos` usa dos `PilaGenerica` (deshacer y rehacer). Al rehacer se repite el mismo marcador |
| **MVC** | El modelo no conoce Swing (lo demuestra `PruebaModelo`). La vista no tiene reglas: solo llama al controlador. La validación vive en `Validador` |
| **Excepciones propias** | 5 excepciones comprobadas; el controlador las convierte en mensajes con `JOptionPane` |
| **Persistencia** | `TorneoDAO` con SQLite. Guardado dentro de una transacción con `commit` y `rollback`. Usa `PreparedStatement`, `NULL` para las partidas pendientes y el Salón de la Fama con `GROUP BY` |

---

## Reparto sugerido (para que todos tengan commits en GitHub)

| Integrante | Paquetes | Expone |
|---|---|---|
| Walter Figueroa | `modelo/` (Torneo, Equipo, Partida, estrategias, Validador) | MVC y Strategy |
| Octavio Peña | `vista/` (ventana, tablas, renderers, gráfico, diálogos) | Swing básico y avanzado, Observer |
| Gabriel Cruz | `controlador/`, `persistencia/`, `excepciones/` | Command, SwingWorker y SQLite |
