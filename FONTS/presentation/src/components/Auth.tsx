import React, { useState, useEffect } from 'react';
import { useApp, User } from '../store';
import controller from '../domain/controller';
import { ArrowLeft } from 'lucide-react';
import { AnimatePresence, motion } from 'motion/react';

interface AuthProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export const Auth: React.FC<AuthProps> = ({ onSuccess, onCancel }) => {
  const { setCurrentUser } = useApp();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  
  // Form State
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState(''); 
  const [error, setError] = useState('');

  useEffect(() => {
    const handleAuthResponse = (data: any) => {
      if (data.error) {
        setError(data.error);
      } else if (data.id && data.username && data.name) {
        setCurrentUser(data as User);
        onSuccess();
      }
    };

    const unsubscribe = controller.onResponse(handleAuthResponse);
    return () => unsubscribe();
  }, [setCurrentUser, onSuccess]);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Usuari i contrasenya són obligatoris');
      return;
    }
    controller.login(username, password);
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password || !name) {
      setError('Tots els camps són obligatoris');
      return;
    }
    controller.registerUser(username, name, password);
  };

  const formVariants = {
    initial: { opacity: 0, x: 20 },
    animate: { opacity: 1, x: 0 },
    exit: { opacity: 0, x: -20 }
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      
      {/* Brand Section */}
      <div className="md:w-1/2 bg-[#008DCD] p-12 flex flex-col justify-between text-white relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-full opacity-10 pointer-events-none">
           {/* Decorative pattern could go here */}
           <svg width="100%" height="100%">
             <pattern id="pattern-circles" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
               <circle cx="20" cy="20" r="2" fill="currentColor" />
             </pattern>
             <rect x="0" y="0" width="100%" height="100%" fill="url(#pattern-circles)" />
           </svg>
        </div>
        
        <div>
          <button 
            onClick={onCancel}
            className="flex items-center gap-2 font-bold uppercase hover:opacity-70 transition-opacity mb-8"
          >
            <ArrowLeft size={20} /> Tornar a l'inici
          </button>
          <motion.div
            key={mode}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <h1 className="text-6xl font-black uppercase tracking-tighter mb-4">Tribus</h1>
            <p className="text-xl font-medium max-w-md">
              {mode === 'login' 
                ? "Inicia sessió" 
                : "Uneix-te a Tribus."}
            </p>
          </motion.div>
        </div>

        <div className="text-sm font-mono opacity-50">
          © {new Date().getFullYear()} Tribus
        </div>
      </div>

      {/* Form Section */}
      <div className="md:w-1/2 bg-white dark:bg-zinc-900 flex items-center justify-center p-8 overflow-hidden">
        <div className="w-full max-w-md">
          <AnimatePresence mode="wait">
             <motion.div
                key={mode === 'login' ? 'title-login' : 'title-register'}
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 10 }}
             >
                <h2 className="text-4xl font-black uppercase mb-8 text-[#008DCD]">
                  {mode === 'login' ? 'Benvingut' : 'Uneix-te a Tribus'}
                </h2>
             </motion.div>
          </AnimatePresence>

          {error && (
            <motion.div 
               initial={{ opacity: 0, height: 0 }}
               animate={{ opacity: 1, height: 'auto' }}
               className="mb-6 p-4 bg-red-100 dark:bg-red-900/30 border-l-4 border-red-500 text-red-700 dark:text-red-300 text-sm font-bold"
            >
              {error}
            </motion.div>
          )}

          <form onSubmit={mode === 'login' ? handleLogin : handleRegister} className="flex flex-col gap-6 relative">
            <AnimatePresence mode="wait" initial={false}>
             <motion.div
                key={mode === 'register' ? 'register-form' : 'login-form'}
                variants={formVariants}
                initial="initial"
                animate="animate"
                exit="exit"
                className="flex flex-col gap-6 w-full"
             >
                {mode === 'register' && (
                  <div className="flex flex-col gap-2">
                    <label className="text-xs font-bold uppercase tracking-widest opacity-70">Nom</label>
                    <input 
                      type="text" 
                      value={name}
                      onChange={e => setName(e.target.value)}
                      className="border-2 border-black dark:border-white p-4 bg-transparent focus:outline-none focus:border-[#008DCD] transition-colors font-medium"
                      placeholder="Juan Pérez"
                    />
                  </div>
                )}

                <div className="flex flex-col gap-2">
                  <label className="text-xs font-bold uppercase tracking-widest opacity-70">Usuari</label>
                  <input 
                    type="text" 
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    className="border-2 border-black dark:border-white p-4 bg-transparent focus:outline-none focus:border-[#008DCD] transition-colors font-medium"
                    placeholder="juanperez"
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label className="text-xs font-bold uppercase tracking-widest opacity-70">Contrasenya</label>
                  <input 
                    type="password" 
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    className="border-2 border-black dark:border-white p-4 bg-transparent focus:outline-none focus:border-[#008DCD] transition-colors font-medium"
                    placeholder="******"
                  />
                </div>

                <button 
                  type="submit"
                  className="mt-4 bg-black text-white dark:bg-white dark:text-black py-4 font-bold uppercase hover:bg-[#008DCD] dark:hover:bg-[#008DCD] hover:text-white dark:hover:text-white transition-colors border-2 border-transparent text-lg tracking-wide"
                >
                  {mode === 'login' ? 'Iniciar Sessió' : 'Registrar-se'}
                </button>
             </motion.div>
            </AnimatePresence>
          </form>

          <div className="mt-8 text-center">
            {mode === 'login' ? (
              <p className="text-sm opacity-70">
               No tens cap compte?{' '}
                <button onClick={() => { setMode('register'); setError(''); }} className="font-bold underline hover:text-[#008DCD] ml-1">
                  Enregistra't ara
                </button>
              </p>
            ) : (
              <p className="text-sm opacity-70">
                Ja tens un compte?{' '}
                <button onClick={() => { setMode('login'); setError(''); }} className="font-bold underline hover:text-[#008DCD] ml-1">
                  Iniciar sessió
                </button>
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
