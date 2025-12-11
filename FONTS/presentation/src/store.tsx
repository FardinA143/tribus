import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import controller from './domain/controller';

// --- Types ---

export type QuestionType = 'text' | 'integer' | 'single' | 'multiple';

export interface Question {
  id: string;
  title: string;
  type: QuestionType;
  mandatory: boolean;
  options?: string[]; // For single/multiple choice
}

export interface Survey {
  id: string;
  authorId: string;
  title: string;
  description: string;
  clusterSize: number;
  analysisMethod: 'kmeans' | 'kmeans++';
  questions: Question[];
  createdAt: number;
}

export interface Response {
  id: string;
  surveyId: string;
  respondentId: string; // 'anon' or userId
  answers: Record<string, string | string[] | number>; // questionId -> answer
  timestamp: number;
}

export interface User {
  id: string;
  username: string; // The login handle
  name: string;     // The "user" name
  password?: string; // In a real app, this is hashed. Mocking here.
  // twoFactorEnabled?: boolean;
  // twoFactorSecret?: string;
}

interface AppState {
  users: User[];
  currentUser: User | null;
  surveys: Survey[];
  responses: Response[];
  theme: 'light' | 'dark';
}

interface AppContextType extends AppState {
  setCurrentUser: (user: User | null) => void;
  addUser: (user: User) => void;
  addSurvey: (survey: Survey) => void;
  updateSurvey: (survey: Survey) => void;
  deleteSurvey: (id: string) => void;
  addResponse: (response: Response) => void;
  deleteUser: (id: string) => void;
  updateUser: (user: User) => void;
  toggleTheme: () => void;
  importSurveys: (data: any) => void;
}

// --- Context ---

const AppContext = createContext<AppContextType | undefined>(undefined);

// --- Store Implementation ---

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [surveys, setSurveys] = useState<Survey[]>([]);
  const [responses, setResponses] = useState<Response[]>([]);
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [loaded, setLoaded] = useState(false);
  const unsubscribeRef = useRef<(() => void) | null>(null);

  const isElectron = controller.isElectron;

  // Load initial data (local fallback) and wire Electron listener if available
  useEffect(() => {
    // Siempre intentar comunicarse con Java para obtener datos iniciales.
    const unsubscribe = controller.onResponse((data: any) => {
      // The Java backend may return different payloads. Here we react to known patterns.
      if (Array.isArray(data)) {
        // assume surveys array
        setSurveys(data as Survey[]);
      } else if (data && data.type === 'surveys') {
        setSurveys(data.payload || []);
      } else if (data && data.type === 'users') {
        setUsers(data.payload || []);
      } else if (data && data.type === 'responses') {
        setResponses(data.payload || []);
      } else if (data && data.status === 'ok') {
        // refresh lists after successful mutating ops
        controller.requestSurveys();
        // controller.requestUsers();
        // controller.requestResponses();
      } else if (data && data.error) {
        console.error('Java error:', data.error);
      }
    });
    unsubscribeRef.current = unsubscribe || null;

    // Solicitar datos iniciales a Java
    controller.requestSurveys();
    // controller.requestUsers();
    // controller.requestResponses();
    setLoaded(true);

    return () => {
      if (unsubscribeRef.current) {
        try { unsubscribeRef.current(); } catch (e) { /* ignore */ }
        unsubscribeRef.current = null;
      }
    };
  }, []);

  // Ya no usamos localStorage: todas las operaciones de persistencia se delegan a Java.

  const addUser = (user: User) => {
    controller.createUser(user);
  };

  const addSurvey = (survey: Survey) => {
    controller.createSurvey(survey.title, survey.description, survey.clusterSize);
  };
  
  const updateSurvey = (updated: Survey) => {
    controller.updateSurvey ? controller.updateSurvey(updated) : sendUpdateFallback(updated);
  };

  const sendUpdateFallback = (updated: Survey) => {
    // If Java doesn't support UPDATE_SURVEY, fall back to sending full payload
    controller.sendCommand && controller.sendCommand(`UPDATE_SURVEY|${encodeURIComponent(JSON.stringify(updated))}`);
  };

  const deleteSurvey = (id: string) => {
    controller.deleteSurvey(id);
  };

  const addResponse = (response: Response) => {
    controller.createResponse(response);
  };

  const deleteUser = (id: string) => {
    controller.deleteUser(id);
    if (currentUser?.id === id) setCurrentUser(null);
  };

  const updateUser = (user: User) => {
    controller.updateUser(user);
    if (currentUser?.id === user.id) setCurrentUser(user);
  };

  const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');

  const importSurveys = (data: any) => {
    // Delegar la importación a Java. `data.path` contiene la ruta a importar.
    if (data && data.path) {
      if (data.type === 'responses') controller.importResponsesFile(data.path);
      else controller.importSurveyFile(data.path);
      return;
    }

    // Si recibimos payload directo (por ejemplo desde un archivo arrastrado), enviar a Java para procesarlo.
    if (data.surveys || data.responses) {
      controller.sendCommand(`IMPORT_PAYLOAD|${encodeURIComponent(JSON.stringify(data))}`);
    }
  };

  const escapePipe = (s?: string) => (s || '').replace(/\|/g, '/');

  useEffect(() => {
    // Sync theme with document class for Tailwind dark mode if we were using 'class' strategy
    // But we will handle colors manually via CSS variables or utility classes based on context
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  return (
    <AppContext.Provider value={{
      users, currentUser, surveys, responses, theme,
      setCurrentUser, addUser, addSurvey, updateSurvey, deleteSurvey, addResponse, deleteUser, updateUser, toggleTheme, importSurveys
    }}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("useApp must be used within AppProvider");
  return context;
};
