const isElectron = typeof window !== 'undefined' && (window as any).backend;

// const isDev = typeof import.meta !== 'undefined' && (import.meta as any).env && (import.meta as any).env.DEV;

const isDev = true; 

const enc = (s: any): string => encodeURIComponent(String(s ?? ''));

const buildQuestionsPayload = (survey: any): string => {
  const qs = Array.isArray(survey?.questions) ? survey.questions : [];
  const specs: string[] = [];
  for (const q of qs) {
    const type = String(q?.type ?? 'text');
    const mandatory = q?.mandatory ? '1' : '0';
    const title = String(q?.title ?? '');
    const options = Array.isArray(q?.options) ? q.options : [];
    const labels = options.map((o: any) => String(o?.label ?? '')).filter(Boolean);
    const optJoined = labels.join(',');
    specs.push([enc(type), mandatory, enc(title), enc(optJoined)].join('~'));
  }
  return encodeURIComponent(specs.join(';;'));
};

const extractJsonChunks = (input: string): string[] => {
  const chunks: string[] = [];
  if (!input) return chunks;

  const lines = input.split(/\r?\n/);
  for (const rawLine of lines) {
    const line = (rawLine || '').trim();
    if (!line) continue;

    if ((line.startsWith('{') && line.endsWith('}')) || (line.startsWith('[') && line.endsWith(']'))) {
      chunks.push(line);
      continue;
    }
    // extraccio json
    let start = -1;
    let depth = 0;
    let inString = false;
    let escape = false;
    let openChar: '{' | '[' | null = null;
    let closeChar: '}' | ']' | null = null;

    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (escape) { escape = false; continue; }
      if (ch === '\\' && inString) { escape = true; continue; }
      if (ch === '"') { inString = !inString; continue; }
      if (inString) continue;

      if (start === -1) {
        if (ch === '{') { start = i; depth = 1; openChar = '{'; closeChar = '}'; continue; }
        if (ch === '[') { start = i; depth = 1; openChar = '['; closeChar = ']'; continue; }
        continue;
      }

      
      if (openChar === '{') {
        if (ch === '{') depth++;
        else if (ch === '}') depth--;
      } else if (openChar === '[') {
        if (ch === '[') depth++;
        else if (ch === ']') depth--;
      }

      if (depth === 0 && start !== -1 && closeChar && ch === closeChar) {
        const candidate = line.slice(start, i + 1).trim();
        if (candidate) chunks.push(candidate);
        start = -1;
        openChar = null;
        closeChar = null;
      }
    }
  }

  return chunks;
};

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

const onResponse = (cb: (data: any) => void): (() => void) => {
  if (!isElectron) return () => {};

  const unsubscribe = (window as any).backend.on('java-response', (responseString: string) => {
    const raw = typeof responseString === 'string' ? responseString : String(responseString);
    const candidates = extractJsonChunks(raw);

    if (candidates.length === 0) {
      try {
        const parsed = JSON.parse(raw);
        if (isDev) {
          console.debug('[controller] java-response', { data: parsed });
        }
        cb(parsed);
      } catch (e) {
        console.error('Error parsing Java response:', e, raw);
      }
      return;
    }

    for (const candidate of candidates) {
      try {
        const parsed = JSON.parse(candidate);
        if (isDev) {
          console.debug('[controller] java-response', { data: parsed });
        }
        cb(parsed);
      } catch (e) {
        console.error('Error parsing Java response chunk:', e, candidate, 'from:', raw);
      }
    }
  });

  return unsubscribe || (() => {});
};
// funcions d'alt nivell per a l'aplicació
const createSurvey = (survey: any) => {
  // CREATE_SURVEY_FULL|title|description|k|analysisMethod|questions
  const title = enc(survey?.title ?? '');
  const description = enc(survey?.description ?? '');
  const k = Number(survey?.clusterSize ?? 3);
  const method = String(survey?.analysisMethod ?? 'kmeans++');
  const questions = buildQuestionsPayload(survey);
  sendCommand(`CREATE_SURVEY_FULL|${title}|${description}|${k}|${method}|${questions}`);
};

const deleteSurvey = (id: string) => sendCommand(`DELETE_SURVEY|${id}`);

const exportSurveyFile = (surveyId: string, path: string) => sendCommand(`EXPORT_SURVEY|${surveyId}|${path}`);

const exportResponsesFile = (surveyId: string, path: string) => sendCommand(`EXPORT_RESPONSES|${surveyId}|${path}`);

const importSurveyFile = (path: string) => sendCommand(`IMPORT_SURVEY|${path}`);

const importResponsesFile = (path: string) => sendCommand(`IMPORT_RESPONSES|${path}`);

const requestSurveys = () => sendCommand('GET_SURVEYS');

const requestClusteringMethods = () => sendCommand('GET_CLUSTERING_METHODS');

const updateSurvey = (survey: any) => {
  const id = String(survey?.id ?? '');
  const title = enc(survey?.title ?? '');
  const description = enc(survey?.description ?? '');
  const k = Number(survey?.clusterSize ?? 3);
  const method = String(survey?.analysisMethod ?? 'kmeans++');
  const questions = buildQuestionsPayload(survey);
  sendCommand(`UPDATE_SURVEY_FULL|${id}|${title}|${description}|${k}|${method}|${questions}`);
};

const answerSurvey = (surveyId: string, answers: string) => {
  sendCommand(`ANSWER_SURVEY|${surveyId}|${answers ?? ''}`);
};

const createUser = (user: any) => sendCommand(`CREATE_USER|${encodeURIComponent(JSON.stringify(user))}`);
const updateUser = (user: any) => sendCommand(`UPDATE_USER|${encodeURIComponent(JSON.stringify(user))}`);
const deleteUser = (id: string) => sendCommand(`DELETE_USER|${id}`);
// const requestUsers = () => sendCommand('GET_USERS'); // no l'implementem

const registerUser = (username: string, name: string, password: string) => {
    const command = `REGISTER|${username}|${name}|${password}`;
    sendCommand(command);
};

const login = (username: string, password: string) => {
    const command = `LOGIN|${username}|${password}`;
    sendCommand(command);
};

const createResponse = (response: any) => {
  if (!response || !response.surveyId || !response.answers) {
    console.warn('createResponse called with invalid payload', response);
    return;
  }
  const pairs: string[] = [];
  for (const [qid, val] of Object.entries(response.answers)) {
    if (val === undefined || val === null) continue;
    if (Array.isArray(val)) pairs.push(`${qid}:${val.join(',')}`);
    else pairs.push(`${qid}:${String(val)}`);
  }
  answerSurvey(response.surveyId, pairs.join(';'));
};
const requestResponses = (surveyId: string) => sendCommand(`LIST_RESPONSES|${surveyId}`);

const deleteResponse = (responseId: string) => sendCommand(`DELETE_RESPONSE|${responseId}`);

const requestAnalysis = (surveyId: string) => sendCommand(`PERFORM_ANALYSIS|${surveyId}`);

export const controller = {  // registrem
  isElectron,
  sendCommand,
  onResponse,
  // enquestes
  createSurvey,
  deleteSurvey,
  exportSurveyFile,
  exportResponsesFile,
  importSurveyFile,
  importResponsesFile,
  requestSurveys,
  requestClusteringMethods,
  // usuaris
  createUser,
  updateUser,
  deleteUser,
  registerUser,
  login,
  // enquestes
  updateSurvey,
  // respostes
  createResponse,
  requestResponses,
  deleteResponse,
  // analisis
  requestAnalysis,
  answerSurvey,
};

export default controller;
