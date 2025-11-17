.PHONY: compile run clean cleandocs help test docs

FONTS_DIR = FONTS
EXE_DIR = EXE
MAIN_CLASS = app.TerminalDriver

help:
	@echo "=========================================="
	@echo "  Sistema d'Enquestes - Makefile"
	@echo "=========================================="
	@echo ""
	@echo "Comandes disponibles:"
	@echo "  make compile    - Compila tots els fonts Java al directori EXE"
	@echo "  make run        - Executa el driver de terminal"
	@echo "  make test       - Compila i executa els tests JUnit (requereix libs/junit-4.13.2.jar)"
	@echo "  make clean      - Elimina el directori EXE"
	@echo "  make cleandocs  - Elimina el directori de documentació (javadocs/)"
	@echo "  make docs       - Genera la documentació Javadoc"
	@echo "  make help       - Mostra aquest missatge d'ajuda"
	@echo ""

compile:
	@echo "[*] Compilant fonts Java..."
	@mkdir -p $(EXE_DIR)
	@find $(FONTS_DIR) -path "$(FONTS_DIR)/Junit" -prune -o -name "*.java" -print | xargs javac -d $(EXE_DIR) -encoding UTF-8
	@echo "[✓] Compilació completada a $(EXE_DIR)/"

run: compile
	@echo "[*] Iniciant driver de terminal..."
	@java -cp $(EXE_DIR) $(MAIN_CLASS)

test: compile
	@echo "[*] Compilant tests..."
	@mkdir -p $(EXE_DIR)
	@JARS=""; \
	if [ -f libs/junit-4.13.2.jar ]; then JARS="libs/junit-4.13.2.jar"; fi; \
	if [ -f libs/hamcrest-core-1.3.jar ]; then \
		if [ -z "$$JARS" ]; then JARS="libs/hamcrest-core-1.3.jar"; else JARS="$$JARS:libs/hamcrest-core-1.3.jar"; fi; \
	fi; \
	if [ -z "$$JARS" ]; then \
		echo "[!] No s'han trobat jars de JUnit a libs/. Compila els tests sense classpath extra."; \
		javac -cp "$(EXE_DIR)" -d $(EXE_DIR) $(FONTS_DIR)/Junit/*.java; \
	else \
		echo "[*] Utilitzant classpath: $$JARS"; \
		javac -cp "$(EXE_DIR):$$JARS" -d $(EXE_DIR) $(FONTS_DIR)/Junit/*.java; \
	fi
	@echo "[*] Executant tests..."
	@if [ -f libs/junit-4.13.2.jar ]; then \
		CP="$(EXE_DIR):libs/junit-4.13.2.jar"; \
		if [ -f libs/hamcrest-core-1.3.jar ]; then CP="$$CP:libs/hamcrest-core-1.3.jar"; fi; \
		CLASSES=""; \
		for f in $(FONTS_DIR)/Junit/*.java; do base=$$(basename $$f .java); CLASSES="$$CLASSES Junit.$$base"; done; \
		java -cp "$$CP" app.TestSuiteRunner $$CLASSES; \
	else \
		echo "[!] No s'ha trobat junit a libs/. Col·loca junit-4.13.2.jar i executa de nou."; exit 1; \
	fi

clean:
	@echo "[*] Netejant fitxers compilats..."
	@if [ -d $(EXE_DIR) ]; then \
		find $(EXE_DIR) -name '*.class' -print -delete; \
		find $(EXE_DIR) -type d -empty -delete; \
		echo "[✓] Classfiles eliminats de $(EXE_DIR)/"; \
	else \
		echo "[!] No existeix $(EXE_DIR)/"; \
	fi

cleandocs:
	@echo "[*] Netejant documentació..."
	@rm -rf javadocs
	@echo "[✓] Directori javadocs/ eliminat"

docs:
	@echo "[*] Generant documentació Javadoc..."
	@javadoc -sourcepath $(FONTS_DIR) -d javadocs app distance Encoder Exceptions importexport kmeans kselector Response Survey user validation
	@echo "[✓] Documentació generada a javadocs/"
