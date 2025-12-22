import { useEffect, useRef, useState } from 'react';
import { useApp } from '../store';
import controller from '../domain/controller';

import { ArrowLeft, Table as TableIcon, Activity, Trash2, Download, Upload, Edit3 } from 'lucide-react';

interface SurveyAnalyzerProps {
  surveyId: string;
  onClose: () => void;
  onEditResponse?: (payload: { responseId: string; surveyId: string; answers: Record<string, any> }) => void;
}

const COLORS = ['#008DCD', '#CD004D', '#00CD80', '#CD8D00', '#8D00CD', '#00CDCD'];

type ClusterPoint = { id: string; x: number; y: number; cluster: number };
type ClusterCentroid = { clusterId: number; x: number; y: number };

const finiteOr = (value: any, fallback: number) => {
  const n = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(n) ? n : fallback;
};

const projectToSvg = (
  points: ClusterPoint[],
  centroids: ClusterCentroid[],
  width: number,
  height: number,
  padding: number,
) => {
  const xs: number[] = [];
  const ys: number[] = [];
  for (const p of points) {
    xs.push(finiteOr(p.x, 0));
    ys.push(finiteOr(p.y, 0));
  }
  for (const c of centroids) {
    xs.push(finiteOr(c.x, 0));
    ys.push(finiteOr(c.y, 0));
  }

  if (xs.length === 0 || ys.length === 0) {
    // No data: map everything to center.
    const cx = width / 2;
    const cy = height / 2;
    return {
      toX: (_: number) => cx,
      toY: (_: number) => cy,
    };
  }

  let minX = Math.min(...xs);
  let maxX = Math.max(...xs);
  let minY = Math.min(...ys);
  let maxY = Math.max(...ys);

  // Avoid zero spans.
  if (minX === maxX) {
    minX -= 1;
    maxX += 1;
  }
  if (minY === maxY) {
    minY -= 1;
    maxY += 1;
  }

  const spanX = maxX - minX;
  const spanY = maxY - minY;
  const innerW = Math.max(1, width - padding * 2);
  const innerH = Math.max(1, height - padding * 2);
  const scale = Math.min(innerW / spanX, innerH / spanY);
  const usedW = spanX * scale;
  const usedH = spanY * scale;
  const offsetX = padding + (innerW - usedW) / 2;
  const offsetY = padding + (innerH - usedH) / 2;

  return {
    toX: (x: number) => offsetX + (finiteOr(x, 0) - minX) * scale,
    toY: (y: number) => offsetY + (finiteOr(y, 0) - minY) * scale,
  };
};

export const SurveyAnalyzer = ({ surveyId, onClose, onEditResponse }: SurveyAnalyzerProps) => {
  const { surveys, responses, analyses, currentUser } = useApp();
  const [tab, setTab] = useState<'responses' | 'analysis'>('responses');
  const [importError, setImportError] = useState('');
  const importUnsubRef = useRef<null | (() => void)>(null);

  useEffect(() => {
    controller.requestResponses(surveyId);
    controller.requestAnalysis(surveyId);
  }, [surveyId]);

  const survey = surveys.find(s => s.id === surveyId);
  const surveyResponses = responses.filter(r => r.surveyId === surveyId);
  const analysis = analyses[surveyId];

  if (!survey) return <div>Enquesta no trobada</div>;

  const ensureElectron = () => {
    if (!controller.isElectron || !(window as any).backend?.openFileDialog) {
      alert("Aquesta funcionalitat només està disponible a l'app d'Electron.");
      return false;
    }
    return true;
  };

  const armOneShotImportListener = () => {
    // Remove previous one-shot listener (if any).
    if (importUnsubRef.current) {
      try { importUnsubRef.current(); } catch { /* ignore */ }
      importUnsubRef.current = null;
    }

    const unsubscribe = controller.onResponse((data: any) => {
      // Only handle terminal outcomes.
      if (!data) return;
      if (data.error) {
        setImportError(String(data.error));
        try { unsubscribe(); } catch { /* ignore */ }
        importUnsubRef.current = null;
        return;
      }
      if (data.status === 'ok') {
        setImportError('');
        try { unsubscribe(); } catch { /* ignore */ }
        importUnsubRef.current = null;
      }
    });
    importUnsubRef.current = unsubscribe;
  };

  const importResponsesTbs = async () => {
    if (!ensureElectron()) return;
    setImportError('');
    const path = await (window as any).backend.openFileDialog({
      title: "Importa respostes (.tbs)",
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!path) return;
    armOneShotImportListener();
    controller.importResponsesFile(path);
    // best-effort refresh (també es refresca via status:ok -> refresh:responses)
    controller.requestResponses(surveyId);
  };

  const exportResponsesTbs = async () => {
    if (!ensureElectron()) return;
    const filePath = await (window as any).backend.saveFileDialog({
      title: "Exporta respostes (.tbs)",
      defaultPath: `respostes_${(survey.title || 'tribus').replace(/\s+/g, '_')}.tbs`,
      filters: [{ name: 'Tribus Survey', extensions: ['tbs'] }],
    });
    if (!filePath) return;
    controller.exportResponsesFile(surveyId, filePath);
  };

  const formatAnswer = (q: any, val: any): string => {
    if (val === undefined || val === null) return '';

    const options = Array.isArray(q?.options) ? q.options : [];
    const findLabel = (raw: any): string => {
      const n = typeof raw === 'number' ? raw : (typeof raw === 'string' && raw.trim() !== '' && !Number.isNaN(Number(raw)) ? Number(raw) : null);
      if (n === null) return String(raw);
      const opt = options.find((o: any) => Number(o?.id) === n);
      return opt?.label ? String(opt.label) : String(raw);
    };

    if ((q?.type === 'single' || q?.type === 'multiple') && options.length > 0) {
      if (Array.isArray(val)) return val.map(findLabel).join(', ');
      if (typeof val === 'string' && val.includes(',')) return val.split(',').map(s => findLabel(s.trim())).join(', ');
      return findLabel(val);
    }

    if (Array.isArray(val)) return val.join(', ');
    return String(val);
  };

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

    const width = 820;
    const height = 320;

    const points: ClusterPoint[] = Array.isArray(analysis.points) ? analysis.points.map((p: any) => ({
      id: String(p?.id ?? ''),
      x: finiteOr(p?.x, 0),
      y: finiteOr(p?.y, 0),
      cluster: Number(p?.cluster ?? 0),
    })) : [];
    const centroids: ClusterCentroid[] = Array.isArray(analysis.centroids) ? analysis.centroids.map((c: any) => ({
      clusterId: Number(c?.clusterId ?? 0),
      x: finiteOr(c?.x, 0),
      y: finiteOr(c?.y, 0),
    })) : [];

    // Project data-space coordinates into the SVG viewport.
    const padding = 16;
    const proj = projectToSvg(points, centroids, width, height, padding);

    // Cluster IDs that exist in current payload.
    const clusterIds = Array.from(
      new Set([
        ...points.map(p => p.cluster),
        ...centroids.map(c => c.clusterId),
        ...chartData.map(r => r.clusterId),
      ].filter((n) => Number.isFinite(n))),
    ).sort((a, b) => a - b);

    const pointsByCluster = new Map<number, Array<{ id: string; x: number; y: number }>>();
    for (const p of points) {
      const arr = pointsByCluster.get(p.cluster) ?? [];
      arr.push({ id: p.id, x: proj.toX(p.x), y: proj.toY(p.y) });
      pointsByCluster.set(p.cluster, arr);
    }

    const centroidByCluster = new Map<number, { x: number; y: number }>();
    for (const c of centroids) {
      centroidByCluster.set(c.clusterId, { x: proj.toX(c.x), y: proj.toY(c.y) });
    }

    const clusterCircles = clusterIds.map((clusterId) => {
      const pts = pointsByCluster.get(clusterId) ?? [];
      if (pts.length === 0) {
        return null;
      }

      const centroid = centroidByCluster.get(clusterId);
      const cx = centroid?.x ?? pts.reduce((sum, p) => sum + p.x, 0) / pts.length;
      const cy = centroid?.y ?? pts.reduce((sum, p) => sum + p.y, 0) / pts.length;
      let r = 0;
      for (const p of pts) {
        const dx = p.x - cx;
        const dy = p.y - cy;
        r = Math.max(r, Math.sqrt(dx * dx + dy * dy));
      }
      // Ensure visible even for 1-point clusters.
      const minR = 18;
      r = Math.max(r, minR);

      return { clusterId, cx, cy, r, pts };
    }).filter(Boolean) as Array<{ clusterId: number; cx: number; cy: number; r: number; pts: Array<{ id: string; x: number; y: number }> }>;

    return (
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white dark:bg-zinc-900 border-2 border-black dark:border-white p-4">
          <h3 className="text-xl font-bold uppercase mb-4">Visualització de clústers</h3>
          <div className="h-[320px]">
            <div className="w-full overflow-hidden text-black dark:text-white">
              <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
                {clusterCircles.map((c, i) => (
                  <g key={`cluster-${c.clusterId}`} style={{ color: COLORS[i % COLORS.length] }}>
                    <circle
                      cx={c.cx}
                      cy={c.cy}
                      r={c.r}
                      fill="currentColor"
                      fillOpacity={0.12}
                      stroke="currentColor"
                      strokeOpacity={0.6}
                      strokeWidth={2}
                    />
                    {c.pts.map((p) => (
                      <g key={p.id}>
                        <circle cx={p.x} cy={p.y} r={5} fill="currentColor" fillOpacity={0.9} />
                        <title>{`Resposta: ${p.id} (Grup ${c.clusterId + 1})`}</title>
                      </g>
                    ))}
                  </g>
                ))}
              </svg>
            </div>
          </div>

          <h3 className="text-xl font-bold uppercase mt-8 mb-4">Mètriques</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Clusters</div>
              <div className="text-2xl font-black">{analysis.clusters}</div>
            </div>
            <div className="border-2 border-black/10 dark:border-white/10 p-4">
              <div className="uppercase font-bold text-xs opacity-60">Inèrcia</div>
              <div className="text-2xl font-black">{Number(analysis.inertia).toFixed(2)}</div>
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
      <div>
        {importError && (
          <div className="mb-4 p-4 bg-red-100 dark:bg-red-900/30 border-l-4 border-red-500 text-red-700 dark:text-red-300 text-sm font-bold">
            {importError}
          </div>
        )}
        <div className="flex justify-end gap-3 mb-3">
          <button
            onClick={importResponsesTbs}
            className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            title="Importa respostes (.tbs)"
          >
            <Upload size={16} />
            Importa respostes
          </button>
          <button
            onClick={exportResponsesTbs}
            className="flex items-center gap-2 text-sm font-bold uppercase hover:text-[#008DCD] transition-colors"
            title="Exporta respostes (.tbs)"
          >
            <Download size={16} />
            Exporta respostes
          </button>
        </div>

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
                    const val = (r as any).answers?.[q.id];
                    return (
                      <td key={q.id} className="p-4 text-sm">
                        {formatAnswer(q, val)}
                      </td>
                    );
                  })}
                  <td className="p-4">
                    {currentUser && r.respondentId === currentUser.id ? (
                      <div className="flex items-center gap-2">
                        {onEditResponse ? (
                          <button
                            className="p-1 rounded hover:text-[#008DCD] hover:bg-black/5 dark:hover:bg-white/10 transition-colors"
                            title="Edita la resposta"
                            onClick={() => onEditResponse({ responseId: r.id, surveyId: r.surveyId, answers: (r as any).answers || {} })}
                          >
                            <Edit3 size={16} />
                          </button>
                        ) : null}

                        <button
                          className="p-1 rounded hover:text-red-600 hover:bg-red-600/10 dark:hover:bg-red-500/20 transition-colors"
                          title="Esborra la resposta"
                          onClick={() => {
                            if (window.confirm('Segur que vols esborrar aquesta resposta?')) {
                              controller.deleteResponse(r.id);
                            }
                          }}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
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
