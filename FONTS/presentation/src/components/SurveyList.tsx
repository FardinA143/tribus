import React from 'react';
import { createPortal } from 'react-dom';
import { useApp, Survey } from '../store';
import controller from '../domain/controller';
import { Plus, Download, Upload, BarChart2, Edit3, MessageSquare, Trash2 } from 'lucide-react';

interface SurveyListProps {
  onAnalyze: (id: string) => void;
  onAnswer: (id: string) => void;
  onModify: (id: string) => void;
  onCreate: () => void;
  filterMode?: 'all' | 'my-surveys' | 'my-responses';
}

export const SurveyList: React.FC<SurveyListProps> = ({ onAnalyze, onAnswer, onModify, onCreate, filterMode = 'all' }) => {
  const { surveys, currentUser, responses } = useApp();
  const [deleteSurveyOpenFor, setDeleteSurveyOpenFor] = React.useState<string | null>(null);
  const [importError, setImportError] = React.useState('');
  const importUnsubRef = React.useRef<null | (() => void)>(null);

  const filteredSurveys = surveys.filter(s => {
    if (filterMode === 'my-surveys') return currentUser && s.authorId === currentUser.id;
    if (filterMode === 'my-responses') {
        if (!currentUser) return false; // per a registrar respostes de usuari
        return responses.some(r => r.surveyId === s.id && r.respondentId === currentUser.id);
    }
    return true;
  });

  const getPageTitle = () => {
    switch (filterMode) {
        case 'my-surveys': return "Les meves enquestes";
        case 'my-responses': return "Les meves respostes";
        default: return 'Enquestes';
    }
  };


  const importSurveyTxt = async () => {
    setImportError('');
    const path = await (window as any).backend.openFileDialog({
      title: "Importa una enquesta (.tbs)",
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!path) return;

    if (importUnsubRef.current) {
      try { importUnsubRef.current(); } catch { /* ignore */ }
      importUnsubRef.current = null;
    }
    const unsubscribe = controller.onResponse((data: any) => {
      if (!data) return;
      if (data.error) {
        setImportError(String(data.error));
        try { unsubscribe(); } catch { /* ignore */ }
        importUnsubRef.current = null;
        return;
      }
      if (data.status === 'ok' && data.refresh === 'surveys' && typeof data.id === 'string') {
        setImportError('');
        try { unsubscribe(); } catch { /* ignore */ }
        importUnsubRef.current = null;
      }
    });
    importUnsubRef.current = unsubscribe;

    controller.importSurveyFile(path);
  };

  const exportSurveyTxt = async (survey: Survey) => {
    const filePath = await (window as any).backend.saveFileDialog({
      title: "Exporta enquesta (.tbs)",
      defaultPath: `enquesta_${(survey.title || 'tribus').replace(/\s+/g, '_')}.tbs`,
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!filePath) return;
    controller.exportSurveyFile(survey.id, filePath);
  };

  const deleteSurvey = (survey: Survey) => {
    if (!currentUser || currentUser.id !== survey.authorId) return;
    controller.deleteSurvey(survey.id);
  };

  return (
    <div className="p-6 max-w-5xl mx-auto min-h-[calc(100vh-80px)] relative">
      <div className="flex justify-between items-end mb-8 border-b-2 border-black dark:border-white pb-4">
        <div>
          <h1 className="text-4xl font-black uppercase tracking-tighter mb-2">{getPageTitle()}</h1>
          <p className="opacity-70">
            {filterMode === 'all' && 'Explora, analitza, crea.'}
            {filterMode === 'my-surveys' && "Gestiona les teves enquestes."}
            {filterMode === 'my-responses' && 'Historial d\'enquestes respostes.'}
          </p>
        </div>
        <div className="flex gap-4">
            <button 
              onClick={onCreate}
              className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            >
              <Plus size={16} />
              Crea
            </button>
            <button 
              onClick={importSurveyTxt}
              className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            >
              <Upload size={16} />
              Importa enquestes
            </button>
        </div>
      </div>

      {importError && (
        <div className="mb-6 p-4 bg-red-100 dark:bg-red-900/30 border-l-4 border-red-500 text-red-700 dark:text-red-300 text-sm font-bold">
          {importError}
        </div>
      )}

      {filteredSurveys.length === 0 && (
          <div className="text-center py-20 opacity-50">
            <p className="text-xl font-bold uppercase">No s'han trobat enquestes.</p>
          </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredSurveys.map((survey) => (
          <div key={survey.id} className="border-2 border-black dark:border-white p-6 flex flex-col justify-between hover:translate-x-1 hover:-translate-y-1 transition-transform bg-white dark:bg-zinc-900">
            <div>
              <div className="flex justify-between items-start mb-4 min-w-0">
                <h3
                  className="text-xl font-bold leading-tight flex-1 min-w-0"
                  style={{ wordBreak: 'break-word', overflowWrap: 'anywhere' }}
                >
                  {survey.title}
                </h3>
                <div className="flex gap-2">
                  {currentUser?.id === survey.authorId && (
                    <button 
                      onClick={() => onModify(survey.id)}
                      className="p-1 hover:text-[#008DCD] transition-colors"
                      title="Modifica l'enquesta"
                    >
                      <Edit3 size={16} />
                    </button>
                  )}
                  <button 
                    onClick={() => exportSurveyTxt(survey)}
                    className="p-1 hover:text-[#008DCD] transition-colors"
                    title="Exporta l'enquesta (.tbs)"
                  >
                    <Download size={16} />
                  </button>
                  {currentUser?.id === survey.authorId && (
                    <>
                      <button
                        className="p-1 rounded hover:text-red-600 hover:bg-red-600/10 dark:hover:bg-red-500/20 transition-colors"
                        title="Esborra l'enquesta"
                        onClick={() => setDeleteSurveyOpenFor(survey.id)}
                      >
                        <Trash2 size={16} />
                      </button>

                      {deleteSurveyOpenFor === survey.id && typeof document !== 'undefined' && createPortal(
                        (
                          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4" role="dialog" aria-modal="true">
                            <div className="w-full max-w-md bg-white dark:bg-zinc-900 border-2 border-black dark:border-white shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] p-6 flex flex-col gap-4">
                              <div className="flex items-start justify-between gap-4">
                                <div className="space-y-2">
                                  <h2 className="text-2xl font-black uppercase">Segur que vols esborrar aquesta enquesta?</h2>
                                  <p className="text-sm opacity-70">Aquesta acció no es pot desfer.</p>
                                </div>
                                <button
                                  onClick={() => setDeleteSurveyOpenFor(null)}
                                  className="text-lg font-black leading-none hover:opacity-70"
                                  aria-label="Tancar"
                                >
                                  ×
                                </button>
                              </div>

                              <div className="flex justify-end gap-3 pt-2">
                                <button
                                  onClick={() => setDeleteSurveyOpenFor(null)}
                                  className="px-4 py-2 border-2 border-black dark:border-white uppercase font-bold hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black"
                                >
                                  No
                                </button>
                                <button
                                  onClick={() => { deleteSurvey(survey); setDeleteSurveyOpenFor(null); }}
                                  className="px-4 py-2 border-2 border-black dark:border-white uppercase font-bold hover:bg-red-700"
                                  style={{ backgroundColor: '#dc2626', color: '#ffffff' }}
                                >
                                  Sí
                                </button>
                              </div>
                            </div>
                          </div>
                        ),
                        document.body
                      )}
                    </>
                  )}
                </div>
              </div>
              <p
                className="text-sm opacity-70 mb-6 line-clamp-3"
                style={{ wordBreak: 'break-word', overflowWrap: 'anywhere' }}
              >
                {survey.description}
              </p>
              
              <div className="text-xs font-mono mb-4 opacity-50">
                Mètode: {survey.analysisMethod} | Clústers: {survey.clusterSize}
              </div>
            </div>

            <div className="flex gap-2 mt-auto">
              <button 
                onClick={() => onAnswer(survey.id)}
                className="flex-1 border-2 border-black dark:border-white py-2 px-2 flex justify-center items-center gap-2 font-bold text-sm uppercase hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors"
              >
                <MessageSquare size={14} /> Respon
              </button>
              <button 
                onClick={() => onAnalyze(survey.id)}
                className="flex-1 border-2 border-black dark:border-white py-2 px-2 flex justify-center items-center gap-2 font-bold text-sm uppercase hover:bg-[#008DCD] hover:text-white transition-colors"
              >
                <BarChart2 size={14} /> Analitza
              </button>
            </div>
          </div>
        ))}
      </div>

      <button 
        onClick={onCreate}
        className="fixed bottom-8 right-8 w-16 h-16 bg-[#008DCD] text-white border-2 border-black dark:border-white flex items-center justify-center rounded-full hover:scale-110 transition-transform shadow-none"
      >
        <Plus size={32} />
      </button>
    </div>
  );
};
