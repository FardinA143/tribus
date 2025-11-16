.PHONY: compile run clean help test

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
	@echo "  make test       - Compila y ejecuta los tests JUnit (requiere libs/junit-4.13.2.jar)"
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

test: compile
	@echo "[*] Compilando tests..."
	@mkdir -p $(OUT_DIR)
	@JARS=""; \
	if [ -f libs/junit-4.13.2.jar ]; then JARS="libs/junit-4.13.2.jar"; fi; \
	if [ -f libs/hamcrest-core-1.3.jar ]; then \
		if [ -z "$$JARS" ]; then JARS="libs/hamcrest-core-1.3.jar"; else JARS="$$JARS:libs/hamcrest-core-1.3.jar"; fi; \
	fi; \
	if [ -z "$$JARS" ]; then \
		echo "[!] No se encontraron jars de JUnit en libs/. Compila los tests sin classpath extra."; \
		javac -cp "$(OUT_DIR)" -d $(OUT_DIR) $(FONTS_DIR)/Junit/*.java; \
	else \
		echo "[*] Usando classpath: $$JARS"; \
		javac -cp "$(OUT_DIR):$$JARS" -d $(OUT_DIR) $(FONTS_DIR)/Junit/*.java; \
	fi
	@echo "[*] Ejecutando tests..."
	@if [ -f libs/junit-4.13.2.jar ]; then \
		CP="$(OUT_DIR):libs/junit-4.13.2.jar"; \
		if [ -f libs/hamcrest-core-1.3.jar ]; then CP="$$CP:libs/hamcrest-core-1.3.jar"; fi; \
		CLASSES=""; \
		for f in $(FONTS_DIR)/Junit/*.java; do base=$$(basename $$f .java); CLASSES="$$CLASSES Junit.$$base"; done; \
		java -cp "$$CP" org.junit.runner.JUnitCore $$CLASSES || true; \
	else \
		echo "[!] No se encontró junit en libs/. Coloca junit-4.13.2.jar y ejecuta de nuevo."; exit 1; \
	fi

clean:
	@echo "[*] Limpiando archivos compilados..."
	@rm -rf $(OUT_DIR)
	@echo "[✓] Directorio $(OUT_DIR)/ eliminado"
