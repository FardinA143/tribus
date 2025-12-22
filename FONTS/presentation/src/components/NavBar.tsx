  import React from 'react';
import { useApp } from '../store'; // 
import { LogIn, Sun, Moon, User, ChevronDown, List, MessageSquare, LogOut, Trash2 } from 'lucide-react'; // Iconos
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

interface NavBarProps {
  onNavigateAuth: () => void;
  onNavigateHome: () => void;
  onNavigateMySurveys: () => void;
  onNavigateMyResponses: () => void;
  onDeleteAccount: (opts: { deleteSurveys: boolean }) => void;
}

export const NavBar: React.FC<NavBarProps> = ({ 
  onNavigateAuth, 
  onNavigateHome,
  onNavigateMySurveys,
  onNavigateMyResponses,
  onDeleteAccount
}) => {
  const { currentUser, setCurrentUser, theme, toggleTheme } = useApp();
  const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
  const [deleteMySurveys, setDeleteMySurveys] = React.useState(false);

  return (
    <nav className="border-b-2 border-black dark:border-white px-6 py-4 flex justify-between items-center bg-[#008DCD] transition-colors relative z-50">
      <button onClick={onNavigateHome} className="text-2xl font-bold tracking-tighter hover:opacity-70 transition-opacity text-white">
        <span>Tribus</span>
      </button>

      <div className="flex items-center gap-4">
        <button 
          onClick={toggleTheme}
          className="p-2 border-2 border-transparent hover:border-black dark:hover:border-white rounded-none transition-all text-white"
        >
          {theme === 'light' ? <Sun size={20} /> : <Moon size={20} />}
        </button>

        {currentUser ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="flex items-center gap-2 px-4 py-2 border-2 border-black dark:border-white hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-all font-bold text-sm uppercase bg-white text-black outline-none focus:outline-none">
                <User size={16} />
                <span className="hidden sm:inline">Hola, {currentUser.username}</span>
                <ChevronDown size={14} />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56 border-2 border-black rounded-none p-0 mt-2 bg-white dark:bg-zinc-900 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
              <DropdownMenuLabel className="p-3 bg-gray-100 dark:bg-zinc-800 border-b-2 border-black dark:border-zinc-700">
                 El meu compte
              </DropdownMenuLabel>
              <DropdownMenuItem onClick={onNavigateMySurveys} className="p-3 cursor-pointer hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors rounded-none flex items-center gap-2 font-medium">
                <List size={16} /> Veure les meves enquestes
              </DropdownMenuItem>
              <DropdownMenuItem onClick={onNavigateMyResponses} className="p-3 cursor-pointer hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors rounded-none flex items-center gap-2 font-medium">
                <MessageSquare size={16} /> Veure les meves respostes
              </DropdownMenuItem>
              {/* Seguridad (2FA) deshabilitado */}
              <DropdownMenuSeparator className="bg-black dark:bg-zinc-700 h-0.5 m-0" />
              <DropdownMenuItem onClick={() => setCurrentUser(null)} className="p-3 cursor-pointer hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors rounded-none flex items-center gap-2 font-medium">
                <LogOut size={16} /> Tanca la sessió
              </DropdownMenuItem>
              <DropdownMenuItem
                onSelect={(e) => {
                  e.preventDefault();
                  setDeleteMySurveys(false);
                  setDeleteDialogOpen(true);
                }}
                className="p-3 cursor-pointer hover:bg-red-600 hover:text-white transition-colors rounded-none flex items-center gap-2 font-medium text-red-600"
              >
                <Trash2 size={16} /> Esborra el compte
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : (
          <button 
            onClick={onNavigateAuth}
            className="flex items-center gap-2 px-4 py-2 border-2 border-black dark:border-white hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-all font-bold text-sm uppercase bg-white text-black"
          >
            <LogIn size={16} />
            Inicia sessió / Registra't
          </button>
        )}
      </div>

      {deleteDialogOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4" role="dialog" aria-modal="true">
          <div className="w-full max-w-md bg-white dark:bg-zinc-900 border-2 border-black dark:border-white shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] p-6 flex flex-col gap-4">
            <div className="flex items-start justify-between gap-4">
              <div className="space-y-2">
                <h2 className="text-2xl font-black uppercase">Vols esborrar el teu compte?</h2>
                <p className="text-sm opacity-70">Aquesta acció no es pot desfer.</p>
              </div>
              <button
                onClick={() => setDeleteDialogOpen(false)}
                className="text-lg font-black leading-none hover:opacity-70"
                aria-label="Tancar"
              >
                ×
              </button>
            </div>

            <label className="flex items-center gap-3 select-none cursor-pointer">
              <input
                type="checkbox"
                checked={deleteMySurveys}
                onChange={(e) => setDeleteMySurveys(e.target.checked)}
                className="w-5 h-5 accent-[#008DCD]"
              />
              <span className="font-bold uppercase text-sm">borrar mis encuestas</span>
            </label>

            <div className="flex justify-end gap-3 pt-2">
              <button
                onClick={() => setDeleteDialogOpen(false)}
                className="px-4 py-2 border-2 border-black dark:border-white uppercase font-bold hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black"
              >
                No
              </button>
              <button
                onClick={() => {
                  onDeleteAccount({ deleteSurveys: deleteMySurveys });
                  setDeleteDialogOpen(false);
                }}
                className="px-4 py-2 border-2 border-black dark:border-white uppercase font-bold hover:bg-red-700"
                style={{ backgroundColor: '#dc2626', color: '#ffffff' }}
              >
                Sí
              </button>
            </div>
          </div>
        </div>
      )}
    </nav>
  );
};
