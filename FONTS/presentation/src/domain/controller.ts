// Lightweight controller that encapsulates communication with Java (Electron backend)
const isElectron = typeof window !== 'undefined' && (window as any).backend;

const sendCommand = (command: string) => {
  if (isElectron) {
    try {
      // @ts-ignore
      (window as any).backend.send('to-java', command);
    } catch (e) {
      console.error('Error sending to Java:', e);
    }
  } else {
    console.warn('Not running inside Electron. Command not sent:', command);
  }
};

// Subscribe to raw responses from Java. Returns an unsubscribe function.
const onResponse = (cb: (data: any) => void): (() => void) => {
  if (!isElectron) return () => {};

  // @ts-ignore
  const unsubscribe = (window as any).backend.on('java-response', (responseString: string) => {
    try {
      const data = JSON.parse(responseString);
      cb(data);
    } catch (e) {
      console.error('Error parsing Java response:', e, responseString);
    }
  });

  return unsubscribe || (() => {});
};

// High-level helpers that the UI can call.
const createSurvey = (title: string, description: string, clusterSize: number) => {
  sendCommand(`CREATE_SURVEY|${(title || '').replace(/\|/g, '/') }|${(description || '').replace(/\|/g, '/')}|${clusterSize}`);
};

const deleteSurvey = (id: string) => sendCommand(`DELETE_SURVEY|${id}`);

const importSurveyFile = (path: string) => sendCommand(`IMPORT_SURVEY|${path}`);

const importResponsesFile = (path: string) => sendCommand(`IMPORT_RESPONSES|${path}`);

const requestSurveys = () => sendCommand('GET_SURVEYS');

const updateSurvey = (survey: any) => sendCommand(`UPDATE_SURVEY|${encodeURIComponent(JSON.stringify(survey))}`);

// User-related commands
const createUser = (user: any) => sendCommand(`CREATE_USER|${encodeURIComponent(JSON.stringify(user))}`);
const updateUser = (user: any) => sendCommand(`UPDATE_USER|${encodeURIComponent(JSON.stringify(user))}`);
const deleteUser = (id: string) => sendCommand(`DELETE_USER|${id}`);
// const requestUsers = () => sendCommand('GET_USERS');

const registerUser = (username: string, name: string, password: string) => {
    // Protocolo: REGISTER|username|name|password
    const command = `REGISTER|${username}|${name}|${password}`;
    sendCommand(command);
};

const login = (username: string, password: string) => {
    // Protocolo: LOGIN|username|password
    const command = `LOGIN|${username}|${password}`;
    sendCommand(command);
};

// Response-related commands
const createResponse = (response: any) => sendCommand(`CREATE_RESPONSE|${encodeURIComponent(JSON.stringify(response))}`);
const requestResponses = () => sendCommand('GET_RESPONSES');

export const controller = {
  isElectron,
  sendCommand,
  onResponse,
  // surveys
  createSurvey,
  deleteSurvey,
  importSurveyFile,
  importResponsesFile,
  requestSurveys,
  // users
  createUser,
  updateUser,
  deleteUser,
//   requestUsers,
  registerUser,
  login,
  // surveys
  updateSurvey,
  // responses
  createResponse,
  requestResponses,
};

export default controller;
