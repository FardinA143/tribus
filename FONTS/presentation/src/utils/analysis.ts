import { Survey, Response, Question } from '../store';

// Helper to encode categorical data
const oneHotEncode = (value: string, options: string[]) => {
  return options.map(opt => (opt === value ? 1 : 0));
};

// Helper to normalize data
const normalize = (val: number, min: number, max: number) => {
  if (max === min) return 0;
  return (val - min) / (max - min);
};

export interface DataPoint {
  id: string; // response ID
  x: number;
  y: number;
  cluster: number;
  original: any;
}

export interface ClusterResult {
  centroids: { x: number; y: number; clusterId: number }[];
  points: DataPoint[];
}

export const analyzeSurveyData = (survey: Survey, responses: Response[]): ClusterResult => {
  if (!responses.length) return { centroids: [], points: [] };

  // 1. Feature Extraction
  // We need to turn each response into a numeric vector.
  // For visualization, we ideally want 2D. 
  // We will take all numeric/choice features, flatten them, and then maybe just take the first 2 dimensions 
  // or average them to fake a 2D projection for this demo.

  const vectors: number[][] = responses.map(r => {
    const vector: number[] = [];
    survey.questions.forEach(q => {
      const val = r.answers[q.id];
      if (q.type === 'integer') {
        vector.push(Number(val) || 0);
      } else if (q.type === 'single' && q.options) {
        // Simple index encoding for 1D, or OneHot for multi-D
        // Let's use Index for simplicity in dimensionality
        const idx = q.options.indexOf(val as string);
        vector.push(idx >= 0 ? idx : -1);
      } else if (q.type === 'multiple' && q.options) {
        // Count how many selected? Or index of first?
        // Let's just use length of selection
        vector.push(Array.isArray(val) ? val.length : 0);
      } else if (q.type === 'text') {
        // Use length of text as a dummy numeric feature
        vector.push((val as string)?.length || 0);
      }
    });
    return vector;
  });

  // 2. Dimensionality Reduction (Fake PCA -> to 2D)
  // If we have > 2 dimensions, we just combine them arbitrarily for the X/Y plot
  const points2D = vectors.map((v, i) => {
    // If only 1 dim, y = 0
    // If 0 dim, x=0, y=0
    let x = v[0] || 0;
    let y = v[1] || 0;
    
    // Mix in other dimensions if present to spread points
    for(let d=2; d<v.length; d++) {
       if (d % 2 === 0) x += v[d];
       else y += v[d];
    }

    return { 
      id: responses[i].id,
      x, 
      y, 
      cluster: 0, 
      original: responses[i] 
    };
  });

  // Normalize points to 0-100 range for graph
  const minX = Math.min(...points2D.map(p => p.x));
  const maxX = Math.max(...points2D.map(p => p.x));
  const minY = Math.min(...points2D.map(p => p.y));
  const maxY = Math.max(...points2D.map(p => p.y));

  points2D.forEach(p => {
    p.x = normalize(p.x, minX, maxX) * 100;
    p.y = normalize(p.y, minY, maxY) * 100;
  });

  // 3. K-Means Clustering (Simplified)
  const k = Math.min(survey.clusterSize || 3, points2D.length);
  
  // Initialize Centroids (Random Pick)
  let centroids = points2D.slice(0, k).map((p, i) => ({ x: p.x, y: p.y, clusterId: i }));
  
  // Iterations (just doing 5 for demo speed)
  for (let iter = 0; iter < 5; iter++) {
    // Assign points
    points2D.forEach(p => {
      let minDist = Infinity;
      let cluster = 0;
      centroids.forEach(c => {
        const dist = Math.sqrt((p.x - c.x) ** 2 + (p.y - c.y) ** 2);
        if (dist < minDist) {
          minDist = dist;
          cluster = c.clusterId;
        }
      });
      p.cluster = cluster;
    });

    // Update Centroids
    centroids = centroids.map(c => {
      const clusterPoints = points2D.filter(p => p.cluster === c.clusterId);
      if (clusterPoints.length === 0) return c;
      const avgX = clusterPoints.reduce((sum, p) => sum + p.x, 0) / clusterPoints.length;
      const avgY = clusterPoints.reduce((sum, p) => sum + p.y, 0) / clusterPoints.length;
      return { ...c, x: avgX, y: avgY };
    });
  }

  return { centroids, points: points2D };
};
