const { contextBridge, ipcRenderer } = require('electron');

// Canales permitidos para mayor seguridad
const validSendChannels = ['to-java'];
const validOnChannels = ['java-response', 'java-error', 'java-exit'];

contextBridge.exposeInMainWorld('backend', {
  // React llama a: window.backend.send('to-java', 'COMMAND|ARGS...')
  send: (channel, data) => {
    if (validSendChannels.includes(channel)) {
      ipcRenderer.send(channel, data);
    } else {
      console.warn('Canal no permitido para send:', channel);
    }
  },

  // React escucha: window.backend.on('java-response', (payload) => { ... })
  on: (channel, func) => {
    if (!validOnChannels.includes(channel)) {
      console.warn('Canal no permitido para on:', channel);
      return;
    }
    const handler = (event, ...args) => func(...args);
    ipcRenderer.on(channel, handler);
    // Devolver una función para quitar el listener si hace falta
    return () => ipcRenderer.removeListener(channel, handler);
  }
});
