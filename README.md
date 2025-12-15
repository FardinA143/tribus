# **EXTRACTOR DE PROTOTIPUS DE COMPORTAMENT O PERFILS**


Projecte de PROP - Tardor 2025-26. Es tracta de construir un entorn on es pot interactuar amb diferents enquestes amb les seves
respectives preguntes i respostes i extraur-ne diferents perfils.

Consta de diferents funcionalitats com donar d'alta a un usuari, importar i exportar enquestes, modificar-les,
i analitzar perfils de semblança entre respostes d'enquestes.

## **PARTICIPANTS**

    - Wenqiang He : wenqiang.he@estudiantat.upc.edu
    - Mateus Grandolfi: mateus.grandolfi@estudiantat.upc.edu
    - Pol Gay: pol.gay@estudiantat.upc.edu
    - Fardin Arafat: fardin.arafat.mia@estudiantat.upc.edu

## Executar el driver de terminal

S'ha afegit un driver principal basat en una Terminal User Interface (`app.TerminalDriver`) que permet:

- Registrar i gestionar usuaris (alta, login, logout).
- Crear enquestes des de zero o importar-les des d'un fitxer de text (`TxtSurveySerializer`).
- Respondre enquestes disponibles i persistir les respostes en memòria local (`LocalPersistence`).
- Llançar una anàlisi bàsica de perfils utilitzant `OneHotEncoder`, `KMeans` i la mètrica de `Silhouette`.


### Com executar amb Makefile (Linux/macOS o Make per Windows)

```pwsh
cd FONTS
make run               # Compila i executa automàticament
make compile           # Només compilar
make test              # Executa tests
make docs               # Genera documentació Javadoc
make clean             # Eliminar artifacts de compilació
make cleandocs          # Eliminar documentació generada
make help              # Mostra aquest missatge d'ajuda
```



## Estructura de carpetes

A continuació una breu descripció de les carpetes principals generades i usades pel projecte:

- **EXE**: Conté les classes compilades i fitxers binaris generats per javac (sortida de la compilació). És la carpeta que cal empaquetar/comprimir per lliurar l'executable o per executar el projecte sense recompilar.
- **FONTS**: Conté tot el codi font Java (*.java) del projecte — paquets, controladors, models, i tests. Aquí es fa el manteniment del projecte i les edicions de codi.
- **DOCS**: Conté la documentació generada (Javadoc) i recursos estàtics relacionats amb la documentació del projecte.
- **LIBS**: Llibreries de tercers i JARs necessaris per compilar i executar els tests (per exemple, JUnit). No modificar sense coneixement de dependències.
