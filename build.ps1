#!/usr/bin/env pwsh
# build.ps1 - Script de compilación y ejecución para Windows PowerShell

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("compile", "run", "clean", "cleandocs", "help", "test", "docs")]
    [string]$Command = "help"
)

$FONTS_DIR = "FONTS"
$OUT_DIR = "out"
$MAIN_CLASS = "app.TerminalDriver"

function Show-Help {
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  Sistema de Encuestas - Build Script" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Uso: .\build.ps1 [comando]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Comandos disponibles:" -ForegroundColor Green
    Write-Host "  compile    - Compila todos los fuentes Java" -ForegroundColor White
    Write-Host "  run        - Ejecuta el driver de terminal" -ForegroundColor White
    Write-Host "  clean      - Elimina el directorio de salida (out/)" -ForegroundColor White
    Write-Host "  cleandocs  - Elimina el directorio de documentación (javadocs/)" -ForegroundColor White
    Write-Host "  docs       - Genera la documentación Javadoc" -ForegroundColor White
    Write-Host "  help       - Muestra este mensaje de ayuda" -ForegroundColor White
    Write-Host ""
}

function Invoke-Compile {
    Write-Host "[*] Compilando fuentes Java..." -ForegroundColor Yellow
    
    if (-not (Test-Path $OUT_DIR)) {
        New-Item -ItemType Directory -Path $OUT_DIR | Out-Null
    }
    
    $sources = Get-ChildItem -Path $FONTS_DIR -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
    if ($sources.Count -eq 0) {
        Write-Host "[!] No se encontraron fuentes Java en $FONTS_DIR" -ForegroundColor Red
        return $false
    }
    
    try {
        javac -d $OUT_DIR -encoding UTF-8 $sources 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[✓] Compilación completada en $OUT_DIR/" -ForegroundColor Green
            return $true
        } else {
            Write-Host "[!] Errores durante la compilación" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "[!] Error: $_" -ForegroundColor Red
        return $false
    }
}

function Invoke-Run {
    if (-not (Test-Path "$OUT_DIR/app/TerminalDriver.class")) {
        Write-Host "[*] Ejecutando compilación primero..." -ForegroundColor Yellow
        if (-not (Invoke-Compile)) {
            Write-Host "[!] Compilación fallida. Abortando ejecución." -ForegroundColor Red
            return
        }
    }
    
    Write-Host "[*] Iniciando driver de terminal..." -ForegroundColor Yellow
    Write-Host ""
    java -cp $OUT_DIR $MAIN_CLASS
}

function Invoke-Test {
    Write-Host "[*] Ejecutando suite de tests JUnit..." -ForegroundColor Yellow

    # Requiere compilación previa
    if (-not (Invoke-Compile)) {
        Write-Host "[!] Compilación fallida. Abortando tests." -ForegroundColor Red
        return
    }

    $junitJar = Join-Path -Path "libs" -ChildPath "junit-4.13.2.jar"
    $hamcrestJar = Join-Path -Path "libs" -ChildPath "hamcrest-core-1.3.jar"

    if (-not (Test-Path $junitJar)) {
        Write-Host "[!] No se encontró '$junitJar'. Coloca junit-4.13.2.jar en la carpeta libs\ y vuelve a intentar." -ForegroundColor Red
        return
    }

    # Compilar tests
    $testSources = Get-ChildItem -Path "$FONTS_DIR\Junit" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
    if ($testSources.Count -eq 0) {
        Write-Host "[!] No se encontraron tests en $FONTS_DIR\Junit" -ForegroundColor Yellow
        return
    }

    $cpParts = @($OUT_DIR, $junitJar)
    if (Test-Path $hamcrestJar) { $cpParts += $hamcrestJar }
    $cp = ($cpParts -join ";")

    Write-Host "[*] Compilando tests (classpath: $cp)..." -ForegroundColor Yellow
    try {
        & javac -cp $cp -d $OUT_DIR $testSources
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[!] Errores durante la compilación de tests" -ForegroundColor Red
            return
        }
    } catch {
        Write-Host "[!] Error al compilar tests: $_" -ForegroundColor Red
        return
    }

    # Construir lista de clases de test (asumimos paquete 'Junit' y nombre de clase == fichero)
    $testClasses = Get-ChildItem -Path "$FONTS_DIR\Junit" -Filter "*.java" | ForEach-Object { "Junit.$($_.BaseName)" }
    $testArg = $testClasses -join ' '

    Write-Host "[*] Ejecutando tests: $testArg" -ForegroundColor Yellow
    try {
        & java -cp $cp org.junit.runner.JUnitCore $testArg
    } catch {
        Write-Host "[!] Error al ejecutar tests: $_" -ForegroundColor Red
    }
}

function Invoke-Clean {
    Write-Host "[*] Limpiando archivos compilados..." -ForegroundColor Yellow
    
    if (Test-Path $OUT_DIR) {
        Remove-Item -Path $OUT_DIR -Recurse -Force
        Write-Host "[✓] Directorio $OUT_DIR/ eliminado" -ForegroundColor Green
    } else {
        Write-Host "[!] No hay directorio $OUT_DIR para limpiar" -ForegroundColor Yellow
    }
}

function Invoke-CleanDocs {
    Write-Host "[*] Limpiando documentación..." -ForegroundColor Yellow
    
    if (Test-Path "javadocs") {
        Remove-Item -Path "javadocs" -Recurse -Force
        Write-Host "[✓] Directorio javadocs/ eliminado" -ForegroundColor Green
    } else {
        Write-Host "[!] No hay directorio javadocs para limpiar" -ForegroundColor Yellow
    }
}

function Invoke-Docs {
    Write-Host "[*] Generando documentación Javadoc..." -ForegroundColor Yellow
    
    try {
        javadoc -sourcepath $FONTS_DIR -d javadocs app distance Encoder Exceptions importexport kmeans kselector Response Survey user validation 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[✓] Documentación generada en javadocs/" -ForegroundColor Green
        } else {
            Write-Host "[!] Errores durante la generación de docs" -ForegroundColor Red
        }
    } catch {
        Write-Host "[!] Error: $_" -ForegroundColor Red
    }
}

# Main
switch ($Command) {
    "compile" { Invoke-Compile | Out-Null }
    "run" { Invoke-Run }
    "test" { Invoke-Test }
    "clean" { Invoke-Clean }
    "cleandocs" { Invoke-CleanDocs }
    "docs" { Invoke-Docs }
    "help" { Show-Help }
    default { Show-Help }
}
