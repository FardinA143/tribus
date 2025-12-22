import React, { useState } from 'react';
import { useApp } from '../store';
import controller from '../domain/controller';
import { ArrowLeft, CheckCircle } from 'lucide-react';

interface SurveyResponderProps {
  surveyId: string;
  onClose: () => void;
  mode?: 'create' | 'edit';
  responseId?: string;
  initialAnswers?: Record<string, any>;
  onEdited?: () => void;
}

export const SurveyResponder: React.FC<SurveyResponderProps> = ({ surveyId, onClose, mode = 'create', responseId, initialAnswers, onEdited }) => {
  const { surveys, currentUser } = useApp();
  const survey = surveys.find(s => s.id === surveyId);

  const [answers, setAnswers] = useState<Record<string, any>>(initialAnswers || {});
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');

  if (!survey) return <div>Enquesta no trobada</div>;

  const handleInputChange = (qId: string, value: any) => {
    if (error) setError('');
    setAnswers(prev => ({ ...prev, [qId]: value }));
  };

  const handleCheckboxChange = (qId: string, optionId: number, checked: boolean) => {
    if (error) setError('');
    setAnswers(prev => {
      const current = (prev[qId] as number[]) || [];
      if (checked) {
        return { ...prev, [qId]: [...current, optionId] };
      } else {
        return { ...prev, [qId]: current.filter(o => o !== optionId) };
      }
    });
  };

  const buildAnswersString = () => {
    const parts: string[] = [];
    for (const q of survey.questions) {
      const qid = String(q.id);
      const val = answers[q.id];
      if (val === undefined || val === null || val === '') continue;

      if (q.type === 'multiple') {
        const arr = Array.isArray(val) ? (val as number[]) : [];
        if (arr.length === 0) continue;
        parts.push(`${qid}:${arr.join(',')}`);
      } else {
        parts.push(`${qid}:${String(val)}`);
      }
    }
    return parts.join(';');
  };

  const handleSubmit = () => {
    setError('');
    // Validate
    for (const q of survey.questions) {
      if (q.mandatory) {
        const val = answers[q.id];
        if (val === undefined || val === '' || (Array.isArray(val) && val.length === 0)) {
          setError(`La pregunta "${q.title}" és obligatòria.`);
          return;
        }
      }
    }

    const answersStr = buildAnswersString();
    if (mode === 'edit') {
      // esborrem la resposta antiga (si existeix) i creem una de nova.
      if (responseId) controller.deleteResponse(responseId);
      controller.answerSurvey(survey.id, answersStr);
      setSubmitted(true);
      if (onEdited) onEdited();
      return;
    }

    controller.answerSurvey(survey.id, answersStr);
    setSubmitted(true);
  };

  if (submitted && mode !== 'edit') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 animate-in fade-in zoom-in duration-500">
        <CheckCircle size={80} className="text-[#008DCD] mb-6" />
        <h2 className="text-4xl font-black uppercase mb-4">Gràcies!</h2>
        <p className="text-lg opacity-70 mb-8 max-w-md">La teva resposta s'ha desat. Pots analitzar els resultats o respondre una altra enquesta.</p>
        <button onClick={onClose} className="px-8 py-3 border-2 border-black dark:border-white font-bold uppercase hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors">
          Tornar a l'inici
        </button>
      </div>
    );
  }

  if (submitted && mode === 'edit') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 animate-in fade-in zoom-in duration-500">
        <CheckCircle size={80} className="text-[#008DCD] mb-6" />
        <h2 className="text-4xl font-black uppercase mb-4">Resposta actualitzada</h2>
        <p className="text-lg opacity-70 mb-8 max-w-md">S'han desat els canvis. </p>
        <button onClick={onClose} className="px-8 py-3 border-2 border-black dark:border-white font-bold uppercase hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors">
          Tornar a l'anàlisi
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-6 min-h-screen">
      <button onClick={onClose} className="flex items-center gap-2 mb-8 opacity-60 hover:opacity-100 transition-opacity">
        <ArrowLeft size={20} /> {mode === 'edit' ? 'Torna' : 'Cancel·la'}
      </button>

      {error && (
        <div className="mb-6 p-4 bg-red-100 dark:bg-red-900/30 border-l-4 border-red-500 text-red-700 dark:text-red-300 text-sm font-bold">
          {error}
        </div>
      )}

      <div className="mb-10 border-b-2 border-black dark:border-white pb-6">
        <h1 className="text-3xl font-black uppercase mb-2 text-[#008DCD] break-words">{mode === 'edit' ? 'Edita resposta' : survey.title}</h1>
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
                value={typeof answers[q.id] === 'string' ? answers[q.id] : ''}
                onChange={e => handleInputChange(q.id, e.target.value)}
              />
            )}

            {q.type === 'integer' && (
              <input 
                type="number"
                className="w-full bg-transparent border-2 border-black/20 dark:border-white/20 p-4 focus:border-[#008DCD] outline-none"
                value={typeof answers[q.id] === 'number' ? answers[q.id] : (answers[q.id] === '' ? '' : (answers[q.id] ?? ''))}
                onChange={e => handleInputChange(q.id, Number(e.target.value))}
              />
            )}

            {q.type === 'single' && (
              <div className="flex flex-col gap-2">
                {(q.options || []).map(opt => (
                  <label key={opt.id} className="flex items-center gap-3 p-3 border-2 border-transparent hover:border-black/10 dark:hover:border-white/10 cursor-pointer transition-colors">
                    <input 
                      type="radio" 
                      name={q.id} 
                      value={opt.id}
                      checked={Number(answers[q.id]) === opt.id}
                      onChange={e => handleInputChange(q.id, Number(e.target.value))}
                      className="w-5 h-5 accent-[#008DCD]"
                    />
                    <span className="text-lg">{opt.label}</span>
                  </label>
                ))}
              </div>
            )}

            {q.type === 'multiple' && (
              <div className="flex flex-col gap-2">
                {(q.options || []).map(opt => (
                  <label key={opt.id} className="flex items-center gap-3 p-3 border-2 border-transparent hover:border-black/10 dark:hover:border-white/10 cursor-pointer transition-colors">
                    <input 
                      type="checkbox" 
                      checked={Array.isArray(answers[q.id]) ? (answers[q.id] as number[]).includes(opt.id) : false}
                      onChange={e => handleCheckboxChange(q.id, opt.id, e.target.checked)}
                      className="w-5 h-5 accent-[#008DCD]"
                    />
                    <span className="text-lg">{opt.label}</span>
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
        {mode === 'edit' ? 'Desa canvis' : 'Envia la resposta'}
      </button>
    </div>
  );
};
