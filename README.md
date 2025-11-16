# **EXTRACTOR DE PROTOTIPUS DE COMPORTAMENT O PERFILS**


Projecte de PROP - Tardor 2025-26. Es tracta de construir un entorn on es pot interactuar amb diferents enquestes amb les seves
respectives preguntes i respostes i extraur-ne diferents perfils.

Consta de diferents funcionalitats com donar d'alta a un usuari, importar i exportar enquestes, modificar-les,
i analitzar perfils de semblança entre respostes d'enquestes.

## Executar el driver de terminal

S'ha afegit un driver principal basat en una Terminal User Interface (`app.TerminalDriver`) que permet:

- Registrar i gestionar usuaris (alta, login, logout).
- Crear enquestes des de zero o importar-les des d'un fitxer de text (`TxtSurveySerializer`).
- Respondre enquestes disponibles i persistir les respostes en memòria local (`LocalPersistence`).
- Llançar una anàlisi bàsica de perfils utilitzant `OneHotEncoder`, `KMeans` i la mètrica de `Silhouette`.

### Opció 1: Amb PowerShell Script (Windows - Recomanat)

```pwsh
cd T:\JavaRepo\subgrup-prop14.4
.\build.ps1 run        # Compila i executa automàticament
```

Altres comandos:
```pwsh
.\build.ps1 compile    # Només compilar
.\build.ps1 clean      # Eliminar artifacts de compilació
.\build.ps1 help       # Mostra ajuda
```

### Opció 2: Amb Makefile (Linux/macOS o Make per Windows)

```pwsh
cd T:\JavaRepo\subgrup-prop14.4
make run               # Compila i executa automàticament
make compile           # Només compilar
make clean             # Eliminar artifacts de compilació
make help              # Mostra ajuda
```

### Opció 3: Manual (PowerShell)

```pwsh
cd T:\JavaRepo\subgrup-prop14.4
if (-not (Test-Path out)) { New-Item -ItemType Directory -Path out | Out-Null }
$sources = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $sources
java -cp out app.TerminalDriver
```

> Consell: Pots mantenir el directori `out/` per accelerar recompilacions posteriors; només cal esborrar-lo quan vulguis una compilació neta.

## **PARTICIPANTS**

    - Wenqiang He : wenqiang.he@estudiantat.upc.edu
    - Mateus Grandolfi: mateus.grandolfi@estudiantat.upc.edu
    - Pol Gay: pol.gay@estudiantat.upc.edu
    - Fardin Arafat: fardin.arafat.mia@estudiantat.upc.edu
