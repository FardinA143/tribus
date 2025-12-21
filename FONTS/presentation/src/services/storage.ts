// El servicio de almacenamiento local fue deshabilitado por seguridad.
// Cualquier intento de importarlo lanzará un error en tiempo de ejecución
// para evitar el uso accidental de almacenamiento local desde la UI.

const message = 'El uso de almacenamiento local no está permitido. Use el backend via controller.';
throw new Error(message);

export {};
