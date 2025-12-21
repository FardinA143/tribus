import React, { useState, useEffect } from 'react';
import { useApp, Survey, Question, QuestionType, ChoiceOption } from '../store';
import { ArrowLeft, Save, Plus, Trash2, GripVertical } from 'lucide-react';

interface SurveyEditorProps {
  surveyId?: string; // If undefined, creating new
  onClose: () => void;
}

export const SurveyEditor: React.FC<SurveyEditorProps> = ({ surveyId, onClose }) => {
  const { surveys, addSurvey, updateSurvey, currentUser } = useApp();
  
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [clusterSize, setClusterSize] = useState(3);
  const [analysisMethod, setAnalysisMethod] = useState<'kmeans' | 'kmeans++'>('kmeans');
  const [questions, setQuestions] = useState<Question[]>([]);

  // Load existing if modifying
  useEffect(() => {
    if (surveyId) {
      const existing = surveys.find(s => s.id === surveyId);
      if (existing) {
        setTitle(existing.title);
        setDescription(existing.description);
        setClusterSize(existing.clusterSize);
        setAnalysisMethod(existing.analysisMethod);
        setQuestions(existing.questions);
      }
    }
  }, [surveyId, surveys]);

  const handleSave = () => {
    if (!title) return alert('El título es obligatorio');
    if (!currentUser) return alert('Debes iniciar sesión');

    const newSurvey: Survey = {
      id: surveyId || Math.random().toString(36).substr(2, 9),
      authorId: currentUser.id,
      title,
      description,
      clusterSize,
      analysisMethod,
      questions,
      createdAt: surveyId ? (surveys.find(s => s.id === surveyId)?.createdAt || Date.now()) : Date.now()
    };

    if (surveyId) {
      updateSurvey(newSurvey);
    } else {
      addSurvey(newSurvey);
    }
    onClose();
  };

  const addQuestion = () => {
    setQuestions([
      ...questions,
      { id: Math.random().toString(36).substr(2, 9), title: 'Nueva Pregunta', type: 'text', mandatory: false, options: [] }
    ]);
  };

  const updateQuestion = (idx: number, updates: Partial<Question>) => {
    const newQs = [...questions];
    newQs[idx] = { ...newQs[idx], ...updates };
    setQuestions(newQs);
  };

  const removeQuestion = (idx: number) => {
    setQuestions(questions.filter((_, i) => i !== idx));
  };

  const handleOptionsChange = (qIdx: number, val: string) => {
    // Expect comma separated or newline separated? Let's say one option per line for text area
    const labels = val.split('\n').map(s => s.trim()).filter(Boolean);
    const opts: ChoiceOption[] = labels.map((label, i) => ({ id: i + 1, label }));
    updateQuestion(qIdx, { options: opts });
  };

  return (
    <div className="p-6 max-w-4xl mx-auto min-h-screen pb-24">
      <button onClick={onClose} className="flex items-center gap-2 mb-6 opacity-60 hover:opacity-100 transition-opacity">
        <ArrowLeft size={20} /> Volver a Encuestas
      </button>

      <h1 className="text-4xl font-black uppercase text-[#008DCD] mb-8">{surveyId ? 'Modificar Encuesta' : 'Crear Encuesta'}</h1>

      <div className="grid gap-6">
        
        {/* Basic Info */}
        <section className="bg-white dark:bg-zinc-900 border-2 border-black dark:border-white p-6 flex flex-col gap-4">
          <h2 className="text-xl font-bold uppercase border-b-2 border-black/10 dark:border-white/10 pb-2">Detalles</h2>
          
          <div>
            <label className="block text-xs font-bold uppercase mb-1">Título de la Encuesta</label>
            <input 
              className="w-full bg-transparent border-2 border-black dark:border-white p-2 font-bold text-lg" 
              value={title} onChange={e => setTitle(e.target.value)} placeholder="Mi Encuesta Increíble"
            />
          </div>

          <div>
            <label className="block text-xs font-bold uppercase mb-1">Descripción</label>
            <textarea 
              className="w-full bg-transparent border-2 border-black dark:border-white p-2" 
              value={description} onChange={e => setDescription(e.target.value)} rows={3}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold uppercase mb-1">Tamaño del Grupo</label>
              <input 
                type="number" min={2} max={10}
                className="w-full bg-transparent border-2 border-black dark:border-white p-2" 
                value={clusterSize} onChange={e => setClusterSize(Number(e.target.value))}
              />
            </div>
            <div>
              <label className="block text-xs font-bold uppercase mb-1">Método de Análisis</label>
              <select 
                className="w-full bg-transparent border-2 border-black dark:border-white p-2" 
                value={analysisMethod} onChange={e => setAnalysisMethod(e.target.value as any)}
              >
                <option value="kmeans">K-Means</option>
                <option value="kmeans++">K-Means++</option>
              </select>
            </div>
          </div>
        </section>

        {/* Questions */}
        <section className="flex flex-col gap-4">
           <h2 className="text-xl font-bold uppercase flex justify-between items-center">
             <span>Preguntas</span>
             <button onClick={addQuestion} className="bg-black text-white dark:bg-white dark:text-black px-3 py-1 text-sm flex items-center gap-2 hover:bg-[#008DCD] dark:hover:bg-[#008DCD] hover:text-white transition-colors">
               <Plus size={14} /> Añadir Pregunta
             </button>
           </h2>

           {questions.map((q, idx) => (
             <div key={q.id} className="bg-white dark:bg-zinc-900 border-2 border-black dark:border-white p-6 relative group">
                <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => removeQuestion(idx)} className="text-red-500 hover:text-red-700">
                    <Trash2 size={20} />
                  </button>
                </div>

                <div className="grid gap-4">
                  <div className="flex gap-4 items-start">
                    <div className="mt-3 opacity-30"><GripVertical size={20} /></div>
                    <div className="flex-1 grid gap-4">
                      <input 
                        className="w-full bg-transparent border-b-2 border-black/20 focus:border-[#008DCD] p-2 font-bold text-lg outline-none" 
                        value={q.title} onChange={e => updateQuestion(idx, { title: e.target.value })} placeholder="Título de la Pregunta"
                      />
                      
                      <div className="flex flex-wrap gap-4">
                        <select 
                          className="bg-transparent border-2 border-black dark:border-white p-2 text-sm" 
                          value={q.type} onChange={e => updateQuestion(idx, { type: e.target.value as QuestionType })}
                        >
                          <option value="text">Texto Libre</option>
                          <option value="integer">Número Entero</option>
                          <option value="single">Selección Única</option>
                          <option value="multiple">Selección Múltiple</option>
                        </select>

                        <label className="flex items-center gap-2 text-sm font-bold uppercase cursor-pointer select-none">
                          <input 
                            type="checkbox" 
                            checked={q.mandatory} 
                            onChange={e => updateQuestion(idx, { mandatory: e.target.checked })}
                            className="w-5 h-5 accent-[#008DCD]"
                          />
                          Obligatoria
                        </label>
                      </div>

                      {(q.type === 'single' || q.type === 'multiple') && (
                        <div>
                          <label className="block text-xs font-bold uppercase mb-1 text-[#008DCD]">Opciones (una por línea)</label>
                          <textarea 
                             className="w-full bg-zinc-50 dark:bg-zinc-800 border-2 border-black/10 p-2 text-sm" 
                             rows={4}
                              value={(q.options || []).map(o => o.label).join('\n')}
                             onChange={e => handleOptionsChange(idx, e.target.value)}
                             placeholder="Opción 1&#10;Opción 2&#10;Opción 3"
                          />
                        </div>
                      )}
                    </div>
                  </div>
                </div>
             </div>
           ))}

           {questions.length === 0 && (
             <div className="text-center p-12 border-2 border-dashed border-black/20 dark:border-white/20 opacity-50 font-bold uppercase">
               No hay preguntas. Añade una arriba.
             </div>
           )}
        </section>

        <button 
          onClick={handleSave}
          className="bg-[#008DCD] text-white py-4 font-black uppercase text-lg border-2 border-black dark:border-white hover:brightness-110 sticky bottom-6 shadow-xl"
        >
          Guardar Encuesta
        </button>

      </div>
    </div>
  );
};
