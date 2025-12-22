const { app, BrowserWindow, ipcMain, dialog, Menu } = require('electron');
const { spawn } = require('child_process');
const path = require('path');
const { pathToFileURL } = require('url');

let javaProcess = null;
let win = null;

let stdoutBuffer = '';
let stderrBuffer = '';
let dev = true; // cambiar a false para producción, no funciona correctamente ahora

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

  // ip para vite
  const devUrl = 'http://localhost:3000';
  if (dev) {
    win.loadURL(devUrl);
  } else {
    const indexPath = path.join(__dirname, '../build/index.html');
    win.loadURL(pathToFileURL(indexPath).href);
  }
}

ipcMain.handle('dialog:openFile', async (_event, options = {}) => {
  if (!win || win.isDestroyed()) return null;
  const result = await dialog.showOpenDialog(win, {
    properties: ['openFile'],
    ...options,
  });
  if (result.canceled) return null;
  return (result.filePaths && result.filePaths[0]) ? result.filePaths[0] : null;
});

ipcMain.handle('dialog:saveFile', async (_event, options = {}) => {
  if (!win || win.isDestroyed()) return null;
  const result = await dialog.showSaveDialog(win, {
    ...options,
  });
  if (result.canceled) return null;
  return result.filePath || null;
});

app.whenReady().then(() => {
  createWindow();

  try {
    Menu.setApplicationMenu(null);
  } catch (e) {
  }

  let jarPath = path.join(__dirname, '../../../EXE/app.jar');
  // const jarPath = path.join(process.resourcesPath, './app.jar');
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
    console.error('Could not start Java:', err);
  }
});

ipcMain.on('to-java', (event, command) => {
  if (javaProcess && javaProcess.stdin.writable) {
    javaProcess.stdin.write(command + '\n');
  } else {
    event.reply('java-error', 'Backend not running.');
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
