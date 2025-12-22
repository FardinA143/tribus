const { contextBridge, ipcRenderer } = require('electron');

const validSendChannels = ['to-java'];
const validOnChannels = ['java-response', 'java-error', 'java-exit'];
const validInvokeChannels = ['dialog:openFile', 'dialog:saveFile'];

contextBridge.exposeInMainWorld('backend', {
  //  window.backend.send('to-java', 'COMMAND|ARG1|ARG2|...|ARGn')
  send: (channel, data) => {
    if (validSendChannels.includes(channel)) {
      ipcRenderer.send(channel, data);
    } else {
      console.warn('not allowed:', channel);
    }
  },

  on: (channel, func) => {
    if (!validOnChannels.includes(channel)) {
      console.warn('not allowed:', channel);
      return;
    }
    const handler = (event, ...args) => func(...args);
    ipcRenderer.on(channel, handler);
    return () => ipcRenderer.removeListener(channel, handler);
  },

  openFileDialog: async (options) => {
    if (!validInvokeChannels.includes('dialog:openFile')) return null;
    return ipcRenderer.invoke('dialog:openFile', options || {});
  },
  saveFileDialog: async (options) => {
    if (!validInvokeChannels.includes('dialog:saveFile')) return null;
    return ipcRenderer.invoke('dialog:saveFile', options || {});
  },
});
