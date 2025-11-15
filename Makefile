.PHONY: compile run clean help

FONTS_DIR = FONTS
OUT_DIR = out
MAIN_CLASS = app.TerminalDriver

help:
	@echo "=========================================="
	@echo "  Sistema de Encuestas - Makefile"
	@echo "=========================================="
	@echo ""
	@echo "Comandos disponibles:"
	@echo "  make compile    - Compila todos los fuentes Java"
	@echo "  make run        - Ejecuta el driver de terminal"
	@echo "  make clean      - Elimina el directorio de salida (out/)"
	@echo "  make help       - Muestra este mensaje de ayuda"
	@echo ""

compile:
	@echo "[*] Compilando fuentes Java..."
	@mkdir -p $(OUT_DIR)
	@find $(FONTS_DIR) -name "*.java" | xargs javac -d $(OUT_DIR) -encoding UTF-8
	@echo "[✓] Compilación completada en $(OUT_DIR)/"

run: compile
	@echo "[*] Iniciando driver de terminal..."
	@java -cp $(OUT_DIR) $(MAIN_CLASS)

clean:
	@echo "[*] Limpiando archivos compilados..."
	@rm -rf $(OUT_DIR)
	@echo "[✓] Directorio $(OUT_DIR)/ eliminado"
