import { useEffect, useState } from 'react';
import { useApp } from '../store';
import controller from '../domain/controller';

import { ArrowLeft, Table as TableIcon, Activity, Trash2 } from 'lucide-react';
import { ScatterChart, Scatter, XAxis, YAxis, ZAxis, Tooltip, Cell } from 'recharts';

interface SurveyAnalyzerProps {
  surveyId: string;
  onClose: () => void;
}

const COLORS = ['#008DCD', '#CD004D', '#00CD80', '#CD8D00', '#8D00CD', '#00CDCD'];

export const SurveyAnalyzer = ({ surveyId, onClose }: SurveyAnalyzerProps) => {
  const { surveys, responses, analyses, currentUser } = useApp();
  const [tab, setTab] = useState<'responses' | 'analysis'>('responses');

  useEffect(() => {
    controller.requestResponses(surveyId);
    controller.requestAnalysis(surveyId);
  }, [surveyId]);

  const survey = surveys.find(s => s.id === surveyId);
  const surveyResponses = responses.filter(r => r.surveyId === surveyId);
  const analysis = analyses[surveyId];

  if (!survey) return <div>Enquesta no trobada</div>;

  const renderAnalysis = () => {
    if (surveyResponses.length < 2) {
      return (
        <div className="flex flex-col items-center justify-center h-64 opacity-50">
          <Activity size={48} className="mb-4" />
          <p className="uppercase font-bold">No hi ha prou dades per analitzar (calen com a mínim 2 respostes)</p>
        </div>
      );
    }

    if (!analysis) {
      return (
        <div className="flex flex-col items-center justify-center h-64 opacity-50">
          <Activity size={48} className="mb-4" />
          <p className="uppercase font-bold">Carregant l'anàlisi...</p>
        </div>
      );
    }

    const chartData = Object.entries(analysis.clusterCounts || {})
      .map(([clusterId, count]) => ({
        clusterId: Number(clusterId),
        name: `Grup ${Number(clusterId) + 1}`,
        count: Number(count),
      }))
      .sort((a, b) => a.clusterId - b.clusterId);

    const points = Array.isArray(analysis.points) ? analysis.points : [];
    const centroids = Array.isArray(analysis.centroids) ? analysis.centroids : [];

    return (
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white dark:bg-zinc-900 border-2 border-black dark:border-white p-4">
          <h3 className="text-xl font-bold uppercase mb-4">Visualització de clústers</h3>
          <div className="h-[320px]">
            {/* Fixed size chart to avoid 0-size container issues */}
            <ScatterChart width={820} height={320} margin={{ top: 12, right: 12, bottom: 12, left: 12 }}>
              <XAxis type="number" dataKey="x" name="X" hide />
              <YAxis type="number" dataKey="y" name="Y" hide />
              <ZAxis range={[60, 250]} />
              <Tooltip
                cursor={{ strokeDasharray: '3 3' }}
                content={({ active, payload }: any) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    const isCentroid = data && typeof data.clusterId === 'number' && data.id === undefined;
                    return (
                      <div className="bg-white dark:bg-black border-2 border-black dark:border-white p-3 shadow-none">
                        <p className="font-bold uppercase mb-1">
                          {isCentroid ? `Centre del clúster ${data.clusterId + 1}` : `Resposta (${data.id || ''})`}
                        </p>
                        <div className="text-xs opacity-70">Clúster: {(isCentroid ? data.clusterId : data.cluster) + 1}</div>
                      </div>
                    );
                  }
                  return null;
                }}
              />

              <Scatter
                name="Respostes"
                data={points}
                shape={(props: any) => {
                  const { cx, cy, fill } = props;
                  return <circle cx={cx} cy={cy} r={5} fill={fill} />;
                }}
              >
                {points.map((p: any, index: number) => (
                  <Cell key={`p-${p.id || index}`} fill={COLORS[(p.cluster ?? 0) % COLORS.length]} />
                ))}
              </Scatter>

              <Scatter
                name="Centroides"
                data={centroids}
                shape={(props: any) => {
                  const { cx, cy, fill } = props;
                  return <circle cx={cx} cy={cy} r={9} fill={fill} stroke="#000" strokeWidth={2} />;
                }}
              >
                {centroids.map((c: any, index: number) => (
                  <Cell key={`c-${c.clusterId ?? index}`} fill={COLORS[(c.clusterId ?? 0) % COLORS.length]} />
                ))}
              </Scatter>
            </ScatterChart>
          </div>

          <h3 className="text-xl font-bold uppercase mt-8 mb-4">Mètriques</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Clusters</div>
              <div className="text-2xl font-black">{analysis.clusters}</div>
            </div>
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Inèrcia</div>
              <div className="text-2xl font-black">{Number(analysis.inertia).toFixed(2)}</div>
            </div>
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Silhouette</div>
              <div className="text-2xl font-black">{Number(analysis.averageSilhouette).toFixed(3)}</div>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <h3 className="text-xl font-bold uppercase mb-2">Detall</h3>
          {chartData.map((row, i) => (
            <div
              key={row.clusterId}
              className="border-l-4 p-4 bg-white dark:bg-zinc-900 border-2"
              style={{ borderColor: COLORS[i % COLORS.length] }}
            >
              <div className="font-bold uppercase">{row.name}</div>
              <div className="opacity-70 text-sm">{row.count} respostes</div>
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
              <th className="p-4">Accions</th>
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
                <td className="p-4">
                  {currentUser && r.respondentId === currentUser.id ? (
                    <button
                      className="p-1 hover:text-red-600 transition-colors"
                      title="Esborra la resposta"
                      onClick={() => {
                        if (window.confirm('Segur que vols esborrar aquesta resposta?')) {
                          controller.deleteResponse(r.id);
                        }
                      }}
                    >
                      <Trash2 size={16} />
                    </button>
                  ) : null}
                </td>
              </tr>
            ))}
            {surveyResponses.length === 0 && (
              <tr>
                <td colSpan={survey.questions.length + 2} className="p-8 text-center opacity-50 uppercase font-bold">
                  Encara no hi ha respostes.
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
          <ArrowLeft size={20} /> Tornar
        </button>
        <div className="text-right">
          <h1 className="text-3xl font-black uppercase text-[#008DCD]">{survey.title}</h1>
          <p className="opacity-50">Tauler d'anàlisi</p>
        </div>
      </div>

      <div className="flex gap-4 mb-8">
        <button 
          onClick={() => setTab('responses')}
          className={`px-6 py-2 border-2 font-bold uppercase transition-all ${tab === 'responses' ? 'bg-black text-white dark:bg-white dark:text-black border-black dark:border-white' : 'border-transparent opacity-50 hover:opacity-100 hover:border-black/20'}`}
        >
          <div className="flex items-center gap-2">
            <TableIcon size={16} /> Dades en brut
          </div>
        </button>
        <button 
          onClick={() => setTab('analysis')}
          className={`px-6 py-2 border-2 font-bold uppercase transition-all ${tab === 'analysis' ? 'bg-black text-white dark:bg-white dark:text-black border-black dark:border-white' : 'border-transparent opacity-50 hover:opacity-100 hover:border-black/20'}`}
        >
          <div className="flex items-center gap-2">
            <Activity size={16} /> Anàlisi de clústers
          </div>
        </button>
      </div>

      {tab === 'responses' ? renderResponses() : renderAnalysis()}

    </div>
  );
};
