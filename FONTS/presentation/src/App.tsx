import React, { useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { AppProvider, useApp } from './store';
import { NavBar } from './components/NavBar';
import { SurveyList } from './components/SurveyList';
import { Auth } from './components/Auth';
import { SurveyEditor } from './components/SurveyEditor';
import { SurveyResponder } from './components/SurveyResponder';
import { SurveyAnalyzer } from './components/SurveyAnalyzer';
import controller from './domain/controller';
// TwoFactorSetup import removed because 2FA is disabled

type View = 
  | { name: 'list', filterMode?: 'all' | 'my-surveys' | 'my-responses' }
  | { name: 'editor', surveyId?: string }
  | { name: 'responder', surveyId: string }
  | { name: 'edit-response', surveyId: string; responseId: string; initialAnswers: Record<string, any> }
  | { name: 'analyzer', surveyId: string }
  | { name: 'auth' }
  ;

const AppContent: React.FC = () => {
  const { currentUser, theme, deleteUser, surveys } = useApp();
  const [view, setView] = useState<View>({ name: 'list', filterMode: 'all' });

  // Background style based on theme
  const bgClass = theme === 'dark' ? 'bg-black text-white' : 'bg-[#FDFFF5] text-black';

  const pageVariants = {
    initial: { opacity: 0, x: -20 },
    animate: { opacity: 1, x: 0 },
    exit: { opacity: 0, x: 20 }
  };

  const pageTransition = {
    duration: 0.3
  };

  return (
    <div className={`min-h-screen transition-colors duration-300 font-sans ${bgClass} overflow-x-hidden`}>
      {view.name !== 'auth' && (
          <NavBar 
          onNavigateAuth={() => setView({ name: 'auth' })} 
          onNavigateHome={() => setView({ name: 'list', filterMode: 'all' })} 
          onNavigateMySurveys={() => setView({ name: 'list', filterMode: 'my-surveys' })}
          onNavigateMyResponses={() => setView({ name: 'list', filterMode: 'my-responses' })}
          onDeleteAccount={({ deleteSurveys }) => {
            if (!currentUser) return;
            if (deleteSurveys) {
              // Best-effort: delete user's surveys first.
              for (const s of surveys) {
                if (s.authorId === currentUser.id) {
                  // Domain enforces permissions.
                  // We intentionally don't block the UI here to keep UX simple.
                  // If a delete fails, backend will emit error.
                  controller.deleteSurvey(s.id);
                }
              }
            }
            deleteUser(currentUser.id);
            setView({ name: 'list', filterMode: 'all' });
          }}
        />
      )}

      <AnimatePresence mode="wait">
        {view.name === 'list' && (
          <motion.div 
            key="list"
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={pageTransition}
          >
            <SurveyList 
              filterMode={view.filterMode}
              onAnalyze={(id) => setView({ name: 'analyzer', surveyId: id })}
              onAnswer={(id) => setView({ name: 'responder', surveyId: id })}
              onModify={(id) => setView({ name: 'editor', surveyId: id })}
              onCreate={() => {
                if (!currentUser) {
                  setView({ name: 'auth' });
                } else {
                  setView({ name: 'editor' });
                }
              }}
            />
          </motion.div>
        )}

        {view.name === 'editor' && (
          <motion.div 
            key="editor"
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={pageTransition}
          >
            <SurveyEditor 
              surveyId={view.surveyId} 
              onClose={() => setView({ name: 'list' })} 
            />
          </motion.div>
        )}

        {view.name === 'responder' && (
          <motion.div 
            key="responder"
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={pageTransition}
          >
            <SurveyResponder 
              surveyId={view.surveyId} 
              onClose={() => setView({ name: 'list' })} 
            />
          </motion.div>
        )}

        {view.name === 'edit-response' && (
          <motion.div
            key="edit-response"
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={pageTransition}
          >
            <SurveyResponder
              surveyId={view.surveyId}
              mode="edit"
              responseId={view.responseId}
              initialAnswers={view.initialAnswers}
              onClose={() => setView({ name: 'analyzer', surveyId: view.surveyId })}
              onEdited={() => setView({ name: 'analyzer', surveyId: view.surveyId })}
            />
          </motion.div>
        )}

        {view.name === 'analyzer' && (
          <motion.div 
            key="analyzer"
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={pageTransition}
          >
            <SurveyAnalyzer 
              surveyId={view.surveyId} 
              onClose={() => setView({ name: 'list' })} 
              onEditResponse={({ responseId, surveyId, answers }) =>
                setView({ name: 'edit-response', responseId, surveyId, initialAnswers: answers })
              }
            />
          </motion.div>
        )}

        {/* 2FA setup view removed (feature disabled) */}

        {view.name === 'auth' && (
          <motion.div 
            key="auth"
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            transition={pageTransition}
            className="w-full h-full"
          >
             <Auth 
              onSuccess={() => setView({ name: 'list' })} 
              onCancel={() => setView({ name: 'list' })}
            />
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}
