import React, { useState, useMemo } from 'react';
import { useApp, Survey } from '../store';
import { analyzeSurveyData, ClusterResult } from '../utils/analysis';
import { ArrowLeft, Table as TableIcon, Activity } from 'lucide-react';
import { ScatterChart, Scatter, XAxis, YAxis, ZAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

interface SurveyAnalyzerProps {
  surveyId: string;
  onClose: () => void;
}

const COLORS = ['#008DCD', '#CD004D', '#00CD80', '#CD8D00', '#8D00CD', '#00CDCD'];

export const SurveyAnalyzer: React.FC<SurveyAnalyzerProps> = ({ surveyId, onClose }) => {
  const { surveys, responses } = useApp();
  const [tab, setTab] = useState<'responses' | 'analysis'>('responses');

  const survey = surveys.find(s => s.id === surveyId);
  const surveyResponses = responses.filter(r => r.surveyId === surveyId);

  const analysis: ClusterResult = useMemo(() => {
    if (!survey || !surveyResponses.length) return { centroids: [], points: [] };
    return analyzeSurveyData(survey, surveyResponses);
  }, [survey, surveyResponses]);

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

    return (
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white dark:bg-zinc-900 border-2 border-black dark:border-white p-4 h-[500px]">
          <h3 className="text-xl font-bold uppercase mb-4 text-[#008DCD]">Visualización de Grupos</h3>
          <ResponsiveContainer width="100%" height="90%">
            <ScatterChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
              <XAxis type="number" dataKey="x" name="X" hide />
              <YAxis type="number" dataKey="y" name="Y" hide />
              <ZAxis range={[60, 400]} />
              <Tooltip 
                cursor={{ strokeDasharray: '3 3' }} 
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    const isCentroid = data.clusterId !== undefined;
                    return (
                      <div className="bg-white dark:bg-black border-2 border-black dark:border-white p-3 shadow-none">
                        <p className="font-bold uppercase mb-1">{isCentroid ? `Centro del Grupo ${data.clusterId + 1}` : 'Respuesta'}</p>
                        {!isCentroid && (
                           <div className="text-xs">
                             {Object.entries(data.original.answers).slice(0, 3).map(([k, v]) => (
                               <div key={k}>{v}</div>
                             ))}
                           </div>
                        )}
                      </div>
                    );
                  }
                  return null;
                }}
              />
              {/* Points */}
              <Scatter name="Respuestas" data={analysis.points} shape="circle">
                {analysis.points.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[entry.cluster % COLORS.length]} />
                ))}
              </Scatter>
              {/* Centroids */}
              <Scatter name="Centros" data={analysis.centroids} shape="star">
                {analysis.centroids.map((entry, index) => (
                  <Cell key={`cent-${index}`} fill={COLORS[entry.clusterId % COLORS.length]} stroke="#000" strokeWidth={2} />
                ))}
              </Scatter>
            </ScatterChart>
          </ResponsiveContainer>
        </div>

        <div className="flex flex-col gap-4">
          <h3 className="text-xl font-bold uppercase mb-2">Resultados de Grupos</h3>
          {analysis.centroids.map((c, i) => {
            const clusterPoints = analysis.points.filter(p => p.cluster === c.clusterId);
            return (
              <div key={i} className="border-l-4 p-4 bg-white dark:bg-zinc-900 border-2 border-r-2 border-b-2 border-t-2" style={{ borderColor: COLORS[i % COLORS.length] }}>
                <h4 className="font-bold uppercase text-lg mb-2" style={{ color: COLORS[i % COLORS.length] }}>Grupo {i + 1}</h4>
                <p className="text-sm font-bold opacity-70 mb-2">{clusterPoints.length} Respuestas</p>
                <div className="text-xs opacity-60">
                   Este grupo representa el {Math.round((clusterPoints.length / surveyResponses.length) * 100)}% de las respuestas totales.
                </div>
              </div>
            );
          })}
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
