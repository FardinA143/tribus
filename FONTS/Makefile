.PHONY: compile jar run test clean cleandocs help docs

# Directories (Makefile located inside FONTS/)
FONTS_DIR = .
EXE_DIR = ../EXE
MAIN_CLASS = app.TerminalDriver

help:
	@echo "=========================================="
	@echo "  Sistema d'Enquestes - Makefile (FONTS/)"
	@echo "=========================================="
	@echo ""
	@echo "Comandes disponibles:"
	@echo "  make compile    - Compila tots els fonts Java a $(EXE_DIR)/classes" 
	@echo "  make jar        - Empaqueta les classes a $(EXE_DIR)/app.jar" 
	@echo "  make run        - Executa el jar generat" 
	@echo "  make test       - Compila i executa els tests JUnit (requereix ../libs/junit-4.13.2.jar)"
	@echo "  make clean      - Elimina fitxers compilats i jars sota $(EXE_DIR)" 
	@echo "  make cleandocs  - Elimina el directori de documentació (../javadocs/)" 
	@echo "  make cleandocs  - Elimina el directori de documentació (../DOCS/DescripcioClasses/)" 
	@echo "  make docs       - Genera documentació Javadoc (a ../DOCS/DescripcioClasses/)" 
	@echo "  make help       - Mostra aquest missatge d'ajuda"
	@echo ""

compile:
	@echo "[*] Compilant fonts Java..."
	@mkdir -p $(EXE_DIR)/classes
	@find $(FONTS_DIR) -path "$(FONTS_DIR)/Junit" -prune -o -name "*.java" -print | xargs javac -d $(EXE_DIR)/classes -encoding UTF-8
	@echo "[✓] Compilació completada a $(EXE_DIR)/classes"

jar: compile
	@echo "[*] Creant jar executable..."
	@mkdir -p $(EXE_DIR)
	@printf "Main-Class: $(MAIN_CLASS)\n" > $(EXE_DIR)/manifest.txt
	@jar cfm $(EXE_DIR)/app.jar $(EXE_DIR)/manifest.txt -C $(EXE_DIR)/classes .
	@echo "[✓] Jar creat a $(EXE_DIR)/app.jar"

run: jar
	@echo "[*] Iniciant driver de terminal..."
	
	@java -jar $(EXE_DIR)/app.jar

test: compile
	@echo "[*] Compilant tests..."
	@mkdir -p $(EXE_DIR)/classes
	@JARS=""; \
	if [ -f ../libs/junit-4.13.2.jar ]; then JARS="../libs/junit-4.13.2.jar"; fi; \
	if [ -f ../libs/hamcrest-core-1.3.jar ]; then \
		if [ -z "$$JARS" ]; then JARS="../libs/hamcrest-core-1.3.jar"; else JARS="$$JARS:../libs/hamcrest-core-1.3.jar"; fi; \
	fi; \
	if [ -z "$$JARS" ]; then \
		echo "[!] No s'han trobat jars de JUnit a ../libs/. Compila els tests sense classpath extra."; \
		javac -cp "$(EXE_DIR)/classes" -d $(EXE_DIR)/classes $(FONTS_DIR)/Junit/*.java; \
	else \
		echo "[*] Utilitzant classpath: $$JARS"; \
		javac -cp "$(EXE_DIR)/classes:$$JARS" -d $(EXE_DIR)/classes $(FONTS_DIR)/Junit/*.java; \
	fi
	@echo "[*] Executant tests..."
	@if [ -f ../libs/junit-4.13.2.jar ]; then \
		CP="$(EXE_DIR)/classes:../libs/junit-4.13.2.jar"; \
		if [ -f ../libs/hamcrest-core-1.3.jar ]; then CP="$$CP:../libs/hamcrest-core-1.3.jar"; fi; \
		CLASSES=""; \
		for f in $(FONTS_DIR)/Junit/*.java; do base=$$(basename $$f .java); CLASSES="$$CLASSES Junit.$$base"; done; \
		java -cp "$$CP" app.TestSuiteRunner $$CLASSES; \
	else \
		echo "[!] No s'ha trobat junit a ../libs/. Col·loca junit-4.13.2.jar i executa de nou."; exit 1; \
	fi

clean:
	@echo "[*] Netejant fitxers compilats i jars..."
	@if [ -d $(EXE_DIR) ]; then \
		find $(EXE_DIR) -name '*.class' -print -delete; \
		rm -f $(EXE_DIR)/app.jar $(EXE_DIR)/manifest.txt; \
		rm -rf $(EXE_DIR)/classes; \
		find $(EXE_DIR) -type d -empty -delete; \
		echo "[✓] Neteja completada a $(EXE_DIR)/"; \
	else \
		echo "[!] No existeix $(EXE_DIR)/"; \
	fi

cleandocs:
	@echo "[*] Netejant documentació..."
	@rm -rf ../DOCS/DescripcioClasses
	@echo "[✓] Javadocs eliminats eliminat"

docs:
	@echo "[*] Generant documentació Javadoc..."
	@mkdir -p ../DOCS/DescripcioClasses
	@javadoc -sourcepath $(FONTS_DIR) -d ../DOCS/DescripcioClasses app distance Encoder Exceptions importexport kmeans kselector Response Survey user validation
	@echo "[✓] Documentació generada a ../DOCS/DescripcioClasses/"
