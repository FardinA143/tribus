.PHONY: compile jar run test clean cleandocs help docs

# Directories (Makefile located inside FONTS/)
FONTS_DIR = .
EXE_DIR = ../EXE
MAIN_CLASS = app.DomainDriver

help:
	@echo "=========================================="
	@echo "  Sistema d'Enquestes - Makefile (FONTS/)"
	@echo "=========================================="
	@echo ""
	@echo "Comandes disponibles:"
	@echo "  make compile    - Compila tots els fonts Java a $(EXE_DIR)/classes" 
	@echo "  make jar        - Empaqueta les classes a $(EXE_DIR)/app.jar" 
	@echo "  make run        - Executa el codi Java i la interfície Electron" 
	@echo "  make clean      - Elimina fitxers compilats i jars sota $(EXE_DIR)" 
	@echo "  make cleandocs  - Elimina el directori de documentació (../DOCS/DescripcioClasses/)" 
	@echo "  make docs       - Genera documentació Javadoc (a ../DOCS/DescripcioClasses/)" 
	@echo "  make help       - Mostra aquest missatge d'ajuda"
	@echo ""

compile:
	@echo "[*] Compilant fonts Java..."
	@mkdir -p $(EXE_DIR)/classes
	@find $(FONTS_DIR) -path "$(FONTS_DIR)/domain/Junit" -prune -o -name "*.java" -print | xargs javac -d $(EXE_DIR)/classes -encoding UTF-8
	@echo "[✓] Compilació completada a $(EXE_DIR)/classes"

jar: compile
	@echo "[*] Creant jar executable..."
	@mkdir -p $(EXE_DIR)
	@printf "Main-Class: $(MAIN_CLASS)\n" > $(EXE_DIR)/manifest.txt
	@jar cfm $(EXE_DIR)/app.jar $(EXE_DIR)/manifest.txt -C $(EXE_DIR)/classes .
	@echo "[✓] Jar creat a $(EXE_DIR)/app.jar"

run: jar
	@echo "[*] Preparant entorn dev..."
	@bash ./ensure_node.sh --cmd "cd presentation && npm install && npm run electron" || (echo "[!] run fallat" && exit 1)

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

.PHONY: ensure-node
ensure-node:
	@echo "[*] Assegurant Node (>=22) i nvm..."
	@bash ./ensure_node.sh || (echo "[!] ensure-node fallat" && exit 1)

cleandocs:
	@echo "[*] Netejant documentació..."
	@rm -rf ../DOCS/DescripcioClasses
	@echo "[✓] Javadocs eliminats eliminat"

docs:
	@echo "[*] Generant documentació Javadoc..."
	@mkdir -p ../DOCS/DescripcioClasses
	@javadoc -sourcepath $(FONTS_DIR) -d ../DOCS/DescripcioClasses app distance Encoder Exceptions importexport kmeans kselector Response Survey user validation
	@echo "[✓] Documentació generada a ../DOCS/DescripcioClasses/"
