import React, { useState } from 'react';
import { useApp, Survey, Response } from '../store';
import { ArrowLeft, CheckCircle } from 'lucide-react';

interface SurveyResponderProps {
  surveyId: string;
  onClose: () => void;
}

export const SurveyResponder: React.FC<SurveyResponderProps> = ({ surveyId, onClose }) => {
  const { surveys, addResponse, currentUser } = useApp();
  const survey = surveys.find(s => s.id === surveyId);

  const [answers, setAnswers] = useState<Record<string, any>>({});
  const [submitted, setSubmitted] = useState(false);

  if (!survey) return <div>Survey not found</div>;

  const handleInputChange = (qId: string, value: any) => {
    setAnswers(prev => ({ ...prev, [qId]: value }));
  };

  const handleCheckboxChange = (qId: string, option: string, checked: boolean) => {
    setAnswers(prev => {
      const current = (prev[qId] as string[]) || [];
      if (checked) {
        return { ...prev, [qId]: [...current, option] };
      } else {
        return { ...prev, [qId]: current.filter(o => o !== option) };
      }
    });
  };

  const handleSubmit = () => {
    // Validate
    for (const q of survey.questions) {
      if (q.mandatory) {
        const val = answers[q.id];
        if (val === undefined || val === '' || (Array.isArray(val) && val.length === 0)) {
          alert(`La pregunta "${q.title}" es obligatoria.`);
          return;
        }
      }
    }

    const response: Response = {
      id: Math.random().toString(36).substr(2, 9),
      surveyId: survey.id,
      respondentId: currentUser ? currentUser.id : 'anon',
      answers,
      timestamp: Date.now()
    };

    addResponse(response);
    setSubmitted(true);
  };

  if (submitted) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 animate-in fade-in zoom-in duration-500">
        <CheckCircle size={80} className="text-[#008DCD] mb-6" />
        <h2 className="text-4xl font-black uppercase mb-4">¡Gracias!</h2>
        <p className="text-lg opacity-70 mb-8 max-w-md">Tu respuesta ha sido guardada. Puedes analizar los resultados o tomar otra encuesta.</p>
        <button onClick={onClose} className="px-8 py-3 border-2 border-black dark:border-white font-bold uppercase hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors">
          Volver al Inicio
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-6 min-h-screen">
      <button onClick={onClose} className="flex items-center gap-2 mb-8 opacity-60 hover:opacity-100 transition-opacity">
        <ArrowLeft size={20} /> Cancelar
      </button>

      <div className="mb-10 border-b-2 border-black dark:border-white pb-6">
        <h1 className="text-3xl font-black uppercase mb-2 text-[#008DCD]">{survey.title}</h1>
        <p className="opacity-70 text-lg">{survey.description}</p>
      </div>

      <div className="flex flex-col gap-10">
        {survey.questions.map((q) => (
          <div key={q.id} className="flex flex-col gap-3">
            <h3 className="font-bold text-xl flex gap-2">
              {q.title}
              {q.mandatory && <span className="text-[#008DCD]">*</span>}
            </h3>

            {q.type === 'text' && (
              <textarea 
                className="w-full bg-transparent border-2 border-black/20 dark:border-white/20 p-4 focus:border-[#008DCD] outline-none min-h-[100px]"
                onChange={e => handleInputChange(q.id, e.target.value)}
              />
            )}

            {q.type === 'integer' && (
              <input 
                type="number"
                className="w-full bg-transparent border-2 border-black/20 dark:border-white/20 p-4 focus:border-[#008DCD] outline-none"
                onChange={e => handleInputChange(q.id, Number(e.target.value))}
              />
            )}

            {q.type === 'single' && (
              <div className="flex flex-col gap-2">
                {q.options?.map(opt => (
                  <label key={opt} className="flex items-center gap-3 p-3 border-2 border-transparent hover:border-black/10 dark:hover:border-white/10 cursor-pointer transition-colors">
                    <input 
                      type="radio" 
                      name={q.id} 
                      value={opt}
                      onChange={e => handleInputChange(q.id, e.target.value)}
                      className="w-5 h-5 accent-[#008DCD]"
                    />
                    <span className="text-lg">{opt}</span>
                  </label>
                ))}
              </div>
            )}

            {q.type === 'multiple' && (
              <div className="flex flex-col gap-2">
                {q.options?.map(opt => (
                  <label key={opt} className="flex items-center gap-3 p-3 border-2 border-transparent hover:border-black/10 dark:hover:border-white/10 cursor-pointer transition-colors">
                    <input 
                      type="checkbox" 
                      onChange={e => handleCheckboxChange(q.id, opt, e.target.checked)}
                      className="w-5 h-5 accent-[#008DCD]"
                    />
                    <span className="text-lg">{opt}</span>
                  </label>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>

      <button 
        onClick={handleSubmit}
        className="mt-12 w-full bg-[#008DCD] text-white py-4 font-black uppercase text-lg border-2 border-black dark:border-white hover:brightness-110 shadow-xl mb-12"
      >
        Enviar Respuesta
      </button>
    </div>
  );
};
