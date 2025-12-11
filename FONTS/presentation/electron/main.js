const { app, BrowserWindow, ipcMain } = require('electron');
const { spawn } = require('child_process');
const path = require('path');

let javaProcess = null;
let win = null;

function createWindow() {
  win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  // En desarrollo Vite por defecto
  const devUrl = process.env.ELECTRON_DEV_URL || 'http://localhost:3000';
  // win.loadURL(devUrl);
  // production
  win.loadFile(path.join(__dirname, '../build/index.html'));
}

app.whenReady().then(() => {
  createWindow();

  // Path al JAR o a tus clases — ajusta según tu empaquetado
  const jarPath = path.join(__dirname, '../../../EXE/app.jar');

  // Ejecutar la clase app.ElectronDriver
  try {
    javaProcess = spawn('java', ['-cp', jarPath, 'app.ElectronDriver'], { cwd: path.join(__dirname, '..') });

    javaProcess.stdout.on('data', (data) => {
      const str = data.toString().trim();
      if (win && !win.isDestroyed()) win.webContents.send('java-response', str);
    });

    javaProcess.stderr.on('data', (data) => {
      const str = data.toString().trim();
      if (win && !win.isDestroyed()) win.webContents.send('java-error', str);
    });

    javaProcess.on('exit', (code) => {
      if (win && !win.isDestroyed()) win.webContents.send('java-exit', { code });
      javaProcess = null;
    });
  } catch (err) {
    console.error('No se pudo iniciar Java:', err);
  }
});

// Recibir desde React y escribir a stdin de Java
ipcMain.on('to-java', (event, command) => {
  if (javaProcess && javaProcess.stdin.writable) {
    javaProcess.stdin.write(command + '\n');
  } else {
    event.reply('java-error', 'Proceso Java no está en ejecución');
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    if (javaProcess) {
      try { javaProcess.kill(); } catch (e) { /* ignore */ }
      javaProcess = null;
    }
    app.quit();
  }
});
