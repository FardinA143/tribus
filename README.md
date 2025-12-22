# **EXTRACTOR DE PROTOTIPUS DE COMPORTAMENT O PERFILS**

Projecte de PROP - Tardor 2025-26. Aquest repositori conté tant la lògica de domini (Java) com una interfície de presentació moderna per treballar amb enquestes i respostes.

Principals funcionalitats:
- Gestionar usuaris (registre, login, logout).
- Crear, importar i exportar enquestes (.tbs / format propi).
- Respondre enquestes i persistir respostes (drivers locals o remots segons configuració).
- Analitzar respostes per extreure perfils amb codificadors (OneHotEncoder), KMeans i mètriques com Silhouette.

## *Tribus*
El nom Tribus prové de la idea de grups d'usuaris amb característiques similars, que és el que aquest projecte busca identificar a través de l'anàlisi de les respostes a les enquestes.

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



## Requisits previs

- Node.js i npm +22 (ensure-node.sh en FONTS/presentation pot ajudar a configurar).
- Java 17+ (per al back-end).
- Make (Linux/Mac) o una terminal compatible amb `make` a Windows (com Git Bash o WSL).

## Instal·lació de dependències i execució

```bash
make run
```
Això instal·larà les dependències de Node.js (si no hi ha Node s'encarrega d'instal·lar-ho) i executarà el Electron app amb el back-end Java integrat.

## Manualment

1. Navega a `FONTS` 
   ```bash
    make jar
    ```
2. Navega a `FONTS/presentation`
3. Instal·la dependències:
   ```bash
   npm install
   ```
4. Executa l'aplicació:
   ```bash
    npm run electron
    ```


