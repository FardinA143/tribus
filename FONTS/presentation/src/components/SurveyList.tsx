import React from 'react';
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
  const { surveys, currentUser, importSurveys, responses } = useApp();

  // Filter logic
  const filteredSurveys = surveys.filter(s => {
    if (filterMode === 'my-surveys') return currentUser && s.authorId === currentUser.id;
    if (filterMode === 'my-responses') {
        if (!currentUser) return false;
        // Check if current user has responded to this survey
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

  const ensureElectron = () => {
    if (!controller.isElectron || !(window as any).backend?.openFileDialog) {
      alert("Aquesta funcionalitat només està disponible a l'app d'Electron.");
      return false;
    }
    return true;
  };

  const importSurveyTxt = async () => {
    if (!ensureElectron()) return;
    const path = await (window as any).backend.openFileDialog({
      title: "Importa una enquesta (.tbs)",
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!path) return;
    importSurveys({ type: 'surveys', path });
  };

  const importResponsesTxt = async () => {
    if (!ensureElectron()) return;
    const path = await (window as any).backend.openFileDialog({
      title: "Importa respostes (.tbs)",
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!path) return;
    importSurveys({ type: 'responses', path });
  };

  const exportSurveyTxt = async (survey: Survey) => {
    if (!ensureElectron()) return;
    const filePath = await (window as any).backend.saveFileDialog({
      title: "Exporta enquesta (.tbs)",
      defaultPath: `enquesta_${(survey.title || 'tribus').replace(/\s+/g, '_')}.tbs`,
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!filePath) return;
    controller.exportSurveyFile(survey.id, filePath);
  };

  const exportResponsesTxt = async (survey: Survey) => {
    if (!ensureElectron()) return;
    const filePath = await (window as any).backend.saveFileDialog({
      title: "Exporta respostes (.tbs)",
      defaultPath: `respostes_${(survey.title || 'tribus').replace(/\s+/g, '_')}.tbs`,
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!filePath) return;
    controller.exportResponsesFile(survey.id, filePath);
  };

  const deleteSurvey = (survey: Survey) => {
    if (!currentUser || currentUser.id !== survey.authorId) return;
    if (window.confirm("Segur que vols esborrar aquesta enquesta? Aquesta acció no es pot desfer.")) {
      controller.deleteSurvey(survey.id);
    }
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
            <button 
              onClick={importResponsesTxt}
              className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            >
              <Upload size={16} />
              Importa respostes
            </button>
        </div>
      </div>

      {filteredSurveys.length === 0 && (
          <div className="text-center py-20 opacity-50">
            <p className="text-xl font-bold uppercase">No s'han trobat enquestes.</p>
          </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredSurveys.map((survey) => (
          <div key={survey.id} className="border-2 border-black dark:border-white p-6 flex flex-col justify-between hover:translate-x-1 hover:-translate-y-1 transition-transform bg-white dark:bg-zinc-900">
            <div>
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-xl font-bold leading-tight">{survey.title}</h3>
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
                  <button 
                    onClick={() => exportResponsesTxt(survey)}
                    className="p-1 hover:text-[#008DCD] transition-colors"
                    title="Exporta les respostes (.tbs)"
                  >
                    <Download size={16} />
                  </button>
                  {currentUser?.id === survey.authorId && (
                    <button
                      onClick={() => deleteSurvey(survey)}
                      className="p-1 hover:text-red-600 transition-colors"
                      title="Esborra l'enquesta"
                    >
                      <Trash2 size={16} />
                    </button>
                  )}
                </div>
              </div>
              <p className="text-sm opacity-70 mb-6 line-clamp-3">{survey.description}</p>
              
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
