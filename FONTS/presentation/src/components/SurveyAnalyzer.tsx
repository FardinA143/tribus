import React, { useEffect, useState } from 'react';
import { useApp } from '../store';
import controller from '../domain/controller';

import { ArrowLeft, Table as TableIcon, Activity } from 'lucide-react';
import { ScatterChart, Scatter, XAxis, YAxis, ZAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

interface SurveyAnalyzerProps {
  surveyId: string;
  onClose: () => void;
}

const COLORS = ['#008DCD', '#CD004D', '#00CD80', '#CD8D00', '#8D00CD', '#00CDCD'];

export const SurveyAnalyzer: React.FC<SurveyAnalyzerProps> = ({ surveyId, onClose }) => {
  const { surveys, responses, analyses } = useApp();
  const [tab, setTab] = useState<'responses' | 'analysis'>('responses');

  useEffect(() => {
    controller.requestResponses(surveyId);
    controller.requestAnalysis(surveyId);
  }, [surveyId]);

  const survey = surveys.find(s => s.id === surveyId);
  const surveyResponses = responses.filter(r => r.surveyId === surveyId);
  const analysis = analyses[surveyId];

  if (!survey) return <div>Encuesta no encontrada</div>;

  const renderAnalysis = () => {
    if (surveyResponses.length < 2) {
      return (
        <div className="flex flex-col items-center justify-center h-64 opacity-50">
          <Activity size={48} className="mb-4" />
          <p className="uppercase font-bold">No hay suficientes datos para analizar (Se necesitan al menos 2 respuestas)</p>
        </div>
      );
    }

    if (!analysis) {
      return (
        <div className="flex flex-col items-center justify-center h-64 opacity-50">
          <Activity size={48} className="mb-4" />
          <p className="uppercase font-bold">Cargando análisis...</p>
        </div>
      );
    }

    return (
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white dark:bg-zinc-900 border-2 border-black dark:border-white p-4">
          <h3 className="text-xl font-bold uppercase mb-4">Resultado del análisis</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Clusters</div>
              <div className="text-2xl font-black">{analysis.clusters}</div>
            </div>
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Inercia</div>
              <div className="text-2xl font-black">{Number(analysis.inertia).toFixed(2)}</div>
            </div>
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Silhouette</div>
              <div className="text-2xl font-black">{Number(analysis.averageSilhouette).toFixed(3)}</div>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <h3 className="text-xl font-bold uppercase mb-2">Distribución por cluster</h3>
          {Object.entries(analysis.clusterCounts || {}).map(([clusterId, count], i) => (
            <div
              key={clusterId}
              className="border-l-4 p-4 bg-white dark:bg-zinc-900 border-2"
              style={{ borderColor: COLORS[i % COLORS.length] }}
            >
              <div className="font-bold uppercase">Grupo {Number(clusterId) + 1}</div>
              <div className="opacity-70 text-sm">{count} respuestas</div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderResponses = () => {
    return (
      <div className="overflow-x-auto border-2 border-black dark:border-white">
        <table className="w-full text-left border-collapse">
          <thead className="bg-black text-white dark:bg-white dark:text-black uppercase text-sm font-bold">
            <tr>
              <th className="p-4 border-r border-white/20 dark:border-black/20">Hora</th>
              {survey.questions.map(q => (
                <th key={q.id} className="p-4 border-r border-white/20 dark:border-black/20 min-w-[150px]">{q.title}</th>
              ))}
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-zinc-900">
            {surveyResponses.map((r, i) => (
              <tr key={r.id} className="border-b border-black/10 dark:border-white/10 hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                <td className="p-4 text-xs font-mono opacity-50">{new Date(r.timestamp).toLocaleDateString()}</td>
                {survey.questions.map(q => {
                  const val = r.answers[q.id];
                  return (
                    <td key={q.id} className="p-4 text-sm">
                      {Array.isArray(val) ? val.join(', ') : String(val)}
                    </td>
                  );
                })}
              </tr>
            ))}
            {surveyResponses.length === 0 && (
              <tr>
                <td colSpan={survey.questions.length + 1} className="p-8 text-center opacity-50 uppercase font-bold">
                  Aún no hay respuestas.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div className="max-w-7xl mx-auto p-6 min-h-screen">
      <div className="flex justify-between items-start mb-8">
        <button onClick={onClose} className="flex items-center gap-2 opacity-60 hover:opacity-100 transition-opacity">
          <ArrowLeft size={20} /> Volver
        </button>
        <div className="text-right">
          <h1 className="text-3xl font-black uppercase text-[#008DCD]">{survey.title}</h1>
          <p className="opacity-50">Panel de Análisis</p>
        </div>
      </div>

      <div className="flex gap-4 mb-8">
        <button 
          onClick={() => setTab('responses')}
          className={`px-6 py-2 border-2 font-bold uppercase transition-all ${tab === 'responses' ? 'bg-black text-white dark:bg-white dark:text-black border-black dark:border-white' : 'border-transparent opacity-50 hover:opacity-100 hover:border-black/20'}`}
        >
          <div className="flex items-center gap-2">
            <TableIcon size={16} /> Datos Crudos
          </div>
        </button>
        <button 
          onClick={() => setTab('analysis')}
          className={`px-6 py-2 border-2 font-bold uppercase transition-all ${tab === 'analysis' ? 'bg-black text-white dark:bg-white dark:text-black border-black dark:border-white' : 'border-transparent opacity-50 hover:opacity-100 hover:border-black/20'}`}
        >
          <div className="flex items-center gap-2">
            <Activity size={16} /> Análisis de Grupos
          </div>
        </button>
      </div>

      {tab === 'responses' ? renderResponses() : renderAnalysis()}

    </div>
  );
};
