# **EXTRACTOR DE PROTOTIPUS DE COMPORTAMENT O PERFILS**

Projecte de PROP - Tardor 2025-26. Aquest repositori conté tant la lògica de domini (Java) com una interfície de presentació moderna per treballar amb enquestes i respostes.

Principals funcionalitats:
- Gestionar usuaris (registre, login, logout).
- Crear, importar i exportar enquestes (.tbs / format propi).
- Respondre enquestes i persistir respostes (drivers locals o remots segons configuració).
- Analitzar respostes per extreure perfils amb codificadors (OneHotEncoder), KMeans i mètriques com Silhouette.

## **Participants**

    - He, Wenqiang: wenqiang.he@estudiantat.upc.edu
    - Gay, Pol: pol.gay@estudiantat.upc.edu
    - Grandolfi, Mateus Grandolfi: mateus.grandolfi@estudiantat.upc.edu
    - Mia, Fardin Arafat: fardin.arafat.mia@estudiantat.upc.edu

## Estructura i estat actual del projecte

- `FONTS/` – codi font principal (Java) i subprojectes. Conté la lògica de domini, persistència i drivers de terminal.
- `EXE/` – classes/artefactes Java compilats (sortida de `javac`/`make`).
- `DOCS/` – documentació generada (Javadoc) i recursos d'ajuda.
- `FONTS/presentation/` – interfície de presentació web (React + TypeScript) basada en Vite.
    - La interfície web està preparada amb `npm` i `vite` a `FONTS/presentation`.
    - S'han migrat alguns components de confirmació (alertboxes) per usar modals centrats renderitzats via React Portal (es garanteix el centrament a la pantalla).
    - S'ha creat una carpeta de quarantena per components potencialment no usats: `FONTS/presentation/src/components/ui/__unused__` (còpies segures de components candidates).

## Com executar la part web (FONTS/presentation)

Des del directori `FONTS/presentation`:

1. Instal·lar dependències (si encara no s'ha fet):

```pwsh
npm install
```

2. Executar en mode desenvolupament (hot-reload):

```pwsh
npm run dev
```

3. Construir per producció:

```pwsh
npm run build
```

4. Servir la build (opcional):

```pwsh
npx serve dist
```

> Nota: alguns mòduls utilitzen aliases específics a `vite.config.ts` (ex.: Radix UI pinned versions). Si hi ha problemes al fer `npm run dev` assegura't d'haver fet `npm install` i revisa `vite.config.ts`.

## Com executar la part Java (terminal / back-end)

Des del directori arrel o `FONTS` podeu usar el `Makefile` (Linux/macOS) o executar les comandes Java directes en Windows/Powershell si es disposa de `make`:

```pwsh
cd FONTS
make run        # Compila i executa el driver principal (TerminalDriver)
make compile    # Només compilar
make test       # Executa tests JUnit
make docs       # Genera documentació Javadoc
make clean      # Eliminar artefactes de compilació
```

Si no es disposa de `make` a Windows, executar manualment amb `javac`/`java` o utilitzar WSL / entorns compatibles.

## Notes de desenvolupament i estat actual

- S'han reemplaçat els `AlertDialog` per modals centrats en `SurveyList` i `SurveyAnalyzer` per garantir una experiència consistent amb l'alertbox d'eliminar compte.
- S'ha creat `FONTS/presentation/src/components/ui/__unused__` amb còpies de components candidates per revisar posteriorment.
- Encara queden pendents tasques de neteja (moure / eliminar originals amb `git mv` i verificar builds/tests abans d'eliminar components obsolets).

## Contribuir

1. Crear una branca per la tasca: `git checkout -b feat/mi-cambio`.
2. Fer commits petits i descriptius.
3. Provar la interfície amb `npm run dev` i córrer `make test` per la part Java.
4. Obrir un PR amb descripció clara i passos per reproduir.

---

Si necessites que actualitzi aquest README amb més detall (per exemple, llista de dependències, scripts addicionals o instruccions per Windows sense `make`), digues-me què vols incloure i ho actualitzo.
