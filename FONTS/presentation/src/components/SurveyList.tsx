import React, { useRef } from 'react';
import { useApp, Survey } from '../store';
import { Plus, Download, Upload, BarChart2, Edit3, MessageSquare } from 'lucide-react';

interface SurveyListProps {
  onAnalyze: (id: string) => void;
  onAnswer: (id: string) => void;
  onModify: (id: string) => void;
  onCreate: () => void;
  filterMode?: 'all' | 'my-surveys' | 'my-responses';
}

export const SurveyList: React.FC<SurveyListProps> = ({ onAnalyze, onAnswer, onModify, onCreate, filterMode = 'all' }) => {
  const { surveys, currentUser, importSurveys, responses } = useApp();
  const fileInputRef = useRef<HTMLInputElement>(null);

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
        case 'my-surveys': return 'Mis Encuestas';
        case 'my-responses': return 'Mis Respuestas';
        default: return 'Encuestas';
    }
  };
  const handleExport = (survey: Survey) => {
    const surveyResponses = responses.filter(r => r.surveyId === survey.id);
    const data = { surveys: [survey], responses: surveyResponses };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `tribus_encuesta_${survey.title.replace(/\s+/g, '_').toLowerCase()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImportClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const json = JSON.parse(event.target?.result as string);
        importSurveys(json);
        alert('¡Importación exitosa!');
      } catch (err) {
        alert('Fallo al analizar el archivo.');
      }
    };
    reader.readAsText(file);
    // Reset
    e.target.value = '';
  };

  return (
    <div className="p-6 max-w-5xl mx-auto min-h-[calc(100vh-80px)] relative">
      <div className="flex justify-between items-end mb-8 border-b-2 border-black dark:border-white pb-4">
        <div>
          <h1 className="text-4xl font-black uppercase tracking-tighter mb-2">{getPageTitle()}</h1>
          <p className="opacity-70">
            {filterMode === 'all' && 'Explora, Analiza, Crea.'}
            {filterMode === 'my-surveys' && 'Gestiona tus encuestas creadas.'}
            {filterMode === 'my-responses' && 'Historial de encuestas contestadas.'}
          </p>
        </div>
        <div className="flex gap-4">
            <button 
              onClick={onCreate}
              className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            >
              <Plus size={16} />
              Crear
            </button>
            <button 
              onClick={handleImportClick}
              className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            >
              <Upload size={16} />
              Importar
            </button>
        </div>
        <input 
          type="file" 
          ref={fileInputRef} 
          className="hidden" 
          accept=".json" 
          onChange={handleFileChange} 
        />
      </div>

      {filteredSurveys.length === 0 && (
          <div className="text-center py-20 opacity-50">
              <p className="text-xl font-bold uppercase">No se encontraron encuestas.</p>
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
                      title="Modificar Encuesta"
                    >
                      <Edit3 size={16} />
                    </button>
                  )}
                  <button 
                    onClick={() => handleExport(survey)}
                    className="p-1 hover:text-[#008DCD] transition-colors"
                    title="Descargar Encuesta y Respuestas"
                  >
                    <Download size={16} />
                  </button>
                </div>
              </div>
              <p className="text-sm opacity-70 mb-6 line-clamp-3">{survey.description}</p>
              
              <div className="text-xs font-mono mb-4 opacity-50">
                Método: {survey.analysisMethod} | Grupos: {survey.clusterSize}
              </div>
            </div>

            <div className="flex gap-2 mt-auto">
              <button 
                onClick={() => onAnswer(survey.id)}
                className="flex-1 border-2 border-black dark:border-white py-2 px-2 flex justify-center items-center gap-2 font-bold text-sm uppercase hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors"
              >
                <MessageSquare size={14} /> Responder
              </button>
              <button 
                onClick={() => onAnalyze(survey.id)}
                className="flex-1 border-2 border-black dark:border-white py-2 px-2 flex justify-center items-center gap-2 font-bold text-sm uppercase hover:bg-[#008DCD] hover:text-white transition-colors"
              >
                <BarChart2 size={14} /> Analizar
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
