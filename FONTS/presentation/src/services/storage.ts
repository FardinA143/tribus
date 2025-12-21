// El servei d'emmagatzematge local està deshabilitat per seguretat.
// Qualsevol intent d'importar-lo llençarà un error en temps d'execució
// per evitar l'ús accidental d'emmagatzematge local des de la UI.

const message = "L'ús d'emmagatzematge local no està permès. Usa el backend via controller.";
throw new Error(message);

export {};
