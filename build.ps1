#!/usr/bin/env pwsh
# build.ps1 - Script de compilación y ejecución para Windows PowerShell

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("compile", "run", "clean", "help")]
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

function Invoke-Clean {
    Write-Host "[*] Limpiando archivos compilados..." -ForegroundColor Yellow
    
    if (Test-Path $OUT_DIR) {
        Remove-Item -Path $OUT_DIR -Recurse -Force
        Write-Host "[✓] Directorio $OUT_DIR/ eliminado" -ForegroundColor Green
    } else {
        Write-Host "[!] No hay directorio $OUT_DIR para limpiar" -ForegroundColor Yellow
    }
}

# Main
switch ($Command) {
    "compile" { Invoke-Compile | Out-Null }
    "run" { Invoke-Run }
    "clean" { Invoke-Clean }
    "help" { Show-Help }
    default { Show-Help }
}
