# Diagrama de clases — E-Sports League Manager

Corresponde al código final de `src/`. GitHub lo muestra como diagrama. La imagen `../diagrama-clases.png` es la versión simplificada que se usó en la presentación.

```mermaid
classDiagram
direction TB

namespace modelo {
    class Torneo {
        -String nombre
        -List~Equipo~ equipos
        -List~Partida~ partidas
        -EstrategiaSimulacion estrategia
        -List~TorneoListener~ listeners
        +agregarEquipo(Equipo e) void
        +quitarEquipo(Equipo e) int
        +simular(Partida p) int[]
        +calcularResultado(Partida p) int[]
        +registrarResultado(Partida p, int[] r) void
        +anularResultado(Partida p) void
        +setEstrategia(EstrategiaSimulacion e) void
        +getTabla() List~Equipo~
        +addListener(TorneoListener l) void
        +removeListener(TorneoListener l) void
        -notificar() void
        -generarCalendario() void
    }
    class Equipo {
        -String nombre
        -int nivel
        -int puntos
        +getDiferencia() int
    }
    class Partida {
        -int jornada
        -Equipo local
        -Equipo visitante
        -int[] resultado
        +getGanador() Equipo
    }
    class TorneoListener {
        <<interface>>
        +torneoActualizado(Torneo t) void
    }
    class EstrategiaSimulacion {
        <<interface>>
        +getNombre() String
        +simular(Equipo local, Equipo visitante) int[]
    }
    class SimulacionPorNivel
    class SimulacionAleatoria
    class SimulacionPorRacha
    class Validador {
        +nombreEquipo(String t)$ String
        +nivel(int n)$ int
        +resultado(int l, int v)$ int[]
    }
}

namespace controlador {
    class TorneoController {
        -Torneo torneo
        -HistorialComandos historial
        +agregarEquipo(String nombre, int nivel) void
        +simularTodo() void
        +deshacer() void
        +rehacer() void
        +guardar() void
        +cargar() void
    }
    class HistorialComandos {
        -PilaGenerica~Comando~ pilaDeshacer
        -PilaGenerica~Comando~ pilaRehacer
        +ejecutar(Comando c) void
        +deshacer() Comando
        +rehacer() Comando
    }
    class Comando {
        <<interface>>
        +ejecutar() void
        +deshacer() void
    }
    class AgregarEquipoCmd
    class EliminarEquipoCmd
    class SimularPartidaCmd
    class ComandoCompuesto
    class ReiniciarResultadosCmd
    class NuevoTorneoCmd
    class SimulacionWorker {
        #doInBackground() Integer
        #process(List chunks) void
        #done() void
    }
}

namespace persistencia {
    class TorneoDAO {
        -Connection conexion
        +conectar() void
        +guardar(Torneo t) void
        +cargar(Torneo t, List estrategias) boolean
        +guardarCampeon(Campeon c) void
        +contarTitulos() Map
    }
    class ExportadorCsv
}

namespace vista {
    class VentanaPrincipal
    class PanelCabecera
    class PanelPosiciones
    class PanelPartidos
    class PanelEstadisticas
    class BarraEstado
    class TablaPosicionesModel
    class DialogoEquipo
}

Torneo "1" o-- "1..*" Equipo
Torneo "1" o-- "*" Partida
Partida --> "2" Equipo
Torneo --> EstrategiaSimulacion : usa
EstrategiaSimulacion <|.. SimulacionPorNivel
EstrategiaSimulacion <|.. SimulacionAleatoria
EstrategiaSimulacion <|.. SimulacionPorRacha
Torneo --> "*" TorneoListener : notifica

TorneoListener <|.. VentanaPrincipal
TorneoListener <|.. PanelCabecera
TorneoListener <|.. PanelPosiciones
TorneoListener <|.. PanelPartidos
TorneoListener <|.. PanelEstadisticas
TorneoListener <|.. BarraEstado
PanelPosiciones --> TablaPosicionesModel
VentanaPrincipal o-- PanelPosiciones
VentanaPrincipal o-- PanelPartidos
VentanaPrincipal o-- PanelEstadisticas
VentanaPrincipal ..> DialogoEquipo

TorneoController --> Torneo
TorneoController --> VentanaPrincipal
TorneoController --> HistorialComandos
TorneoController --> TorneoDAO
TorneoController ..> SimulacionWorker : lanza
HistorialComandos o-- "*" Comando
Comando <|.. AgregarEquipoCmd
Comando <|.. EliminarEquipoCmd
Comando <|.. SimularPartidaCmd
Comando <|.. ComandoCompuesto
Comando <|.. ReiniciarResultadosCmd
Comando <|.. NuevoTorneoCmd
ComandoCompuesto o-- Comando : agrupa
```
