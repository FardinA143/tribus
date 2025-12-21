const { app, BrowserWindow, ipcMain } = require('electron');
const { spawn } = require('child_process');
const path = require('path');

let javaProcess = null;
let win = null;

// Buffer stdout/stderr so we can emit exactly one line at a time.
// Node streams may chunk multiple lines together; the frontend expects one JSON per message.
let stdoutBuffer = '';
let stderrBuffer = '';

function emitLines(buffer, chunk, emit) {
  buffer += chunk.toString();
  const lines = buffer.split(/\r?\n/);
  buffer = lines.pop() ?? '';
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    emit(trimmed);
  }
  return buffer;
}

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
  const devUrl = 'http://localhost:3000';
  win.loadURL(devUrl);
  // production
  // win.loadFile(path.join(__dirname, '../build/index.html'));
}

app.whenReady().then(() => {
  createWindow();

  const jarPath = path.join(__dirname, '../../../EXE/app.jar');

  // Ejecutar la clase app.ElectronDriver
  try {
    javaProcess = spawn('java', ['-cp', jarPath, 'app.DomainDriver'], { cwd: path.join(__dirname, '..') });

    javaProcess.stdout.on('data', (data) => {
      if (!win || win.isDestroyed()) return;
      stdoutBuffer = emitLines(stdoutBuffer, data, (line) => win.webContents.send('java-response', line));
    });

    javaProcess.stderr.on('data', (data) => {
      if (!win || win.isDestroyed()) return;
      stderrBuffer = emitLines(stderrBuffer, data, (line) => win.webContents.send('java-error', line));
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
