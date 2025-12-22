import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import controller from './domain/controller';

// pseudoclases per poder utilitzar dins de interficie, no fa cap tipus de lògica, capa domini
export type QuestionType = 'text' | 'integer' | 'single' | 'multiple';

export interface ChoiceOption {
  id: number;
  label: string;
}

export interface Question {
  id: string;
  title: string;
  type: QuestionType;
  mandatory: boolean;
  options?: ChoiceOption[]; 
}

export interface Survey {
  id: string;
  authorId: string;
  title: string;
  description: string;
  clusterSize: number;
  analysisMethod: string;
  questions: Question[];
  createdAt: number;
}

export interface ClusteringMethodOption {
  id: string;
  label: string;
}

export interface Response {
  id: string;
  surveyId: string;
  respondentId: string; 
  answers: Record<string, string | string[] | number>; 
  timestamp: number;
}

export interface AnalysisPayload {
  clusters: number;
  inertia: number;
  averageSilhouette: number;
  clusterCounts: Record<string, number>;
  points?: Array<{ id: string; x: number; y: number; cluster: number }>;
  centroids?: Array<{ clusterId: number; x: number; y: number }>;
}

export interface User {
  id: string;
  username: string; 
  name: string;     
  password?: string; 

}

interface AppState {
  users: User[];
  currentUser: User | null;
  surveys: Survey[];
  responses: Response[];
  analyses: Record<string, AnalysisPayload>;
  clusteringMethods: ClusteringMethodOption[];
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


const AppContext = createContext<AppContextType | undefined>(undefined);


export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [surveys, setSurveys] = useState<Survey[]>([]);
  const [responses, setResponses] = useState<Response[]>([]);
  const [analyses, setAnalyses] = useState<Record<string, AnalysisPayload>>({});
  const [clusteringMethods, setClusteringMethods] = useState<ClusteringMethodOption[]>([]);
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [loaded, setLoaded] = useState(false);
  const unsubscribeRef = useRef<(() => void) | null>(null);

  const isDev = typeof import.meta !== 'undefined' && (import.meta as any).env && (import.meta as any).env.DEV;

  const isElectron = controller.isElectron;

  // Carregar dades inicials i connectar listener d'Electron/Java.
  useEffect(() => {
    const unsubscribe = controller.onResponse((data: any) => {
      if (isDev) {
        // Debug only: inspect what the Java backend is actually sending.
        console.debug('[Store] java-response', { data });
      }
      if (Array.isArray(data)) {
        // assume surveys array
        setSurveys(data as Survey[]);
      } else if (data && data.type === 'surveys') {
        setSurveys(data.payload || []);
      } else if (data && data.type === 'users') {
        setUsers(data.payload || []);
      } else if (data && data.type === 'responses') {
        setResponses(data.payload || []);
      } else if (data && data.type === 'analysis') {
        const surveyId = String(data.surveyId || '');
        if (surveyId) {
          setAnalyses(prev => ({ ...prev, [surveyId]: data.payload as AnalysisPayload }));
        }
      } else if (data && data.type === 'clusteringMethods') {
        setClusteringMethods(Array.isArray(data.payload) ? data.payload : []);
      } else if (data && data.status === 'ok') {
        if (data.refresh === 'responses' && data.surveyId) {
          controller.requestResponses(String(data.surveyId));
        }
        controller.requestSurveys();
      } else if (data && data.error) {
        console.error('Java error:', data.error);
      }
    });
    unsubscribeRef.current = unsubscribe || null;

    controller.requestSurveys();
    controller.requestClusteringMethods();
    setLoaded(true);

    return () => {
      if (unsubscribeRef.current) {
        try { unsubscribeRef.current(); } catch (e) { /* ignore */ }
        unsubscribeRef.current = null;
      }
    };
  }, [isDev]);


  const addUser = (user: User) => {
    controller.createUser(user);
  };

  const addSurvey = (survey: Survey) => {
    controller.createSurvey(survey);
  };
  
  const updateSurvey = (updated: Survey) => {
    controller.updateSurvey ? controller.updateSurvey(updated) : sendUpdateFallback(updated);
  };

  const sendUpdateFallback = (updated: Survey) => {
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
    if (data && data.path) {
      if (data.type === 'responses') controller.importResponsesFile(data.path);
      else controller.importSurveyFile(data.path);
      return;
    }

    if (data.surveys || data.responses) {
      controller.sendCommand(`IMPORT_PAYLOAD|${encodeURIComponent(JSON.stringify(data))}`);
    }
  };

  const escapePipe = (s?: string) => (s || '').replace(/\|/g, '/');

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  return (
    <AppContext.Provider value={{
      users, currentUser, surveys, responses, analyses, clusteringMethods, theme,
      setCurrentUser, addUser, addSurvey, updateSurvey, deleteSurvey, addResponse, deleteUser, updateUser, toggleTheme, importSurveys
    }}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("Electron not being used");
  return context;
};
