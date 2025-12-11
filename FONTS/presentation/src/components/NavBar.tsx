  import React from 'react';
import { useApp } from '../store';
import { LogIn, Sun, Moon, User, ChevronDown, List, MessageSquare, LogOut, Trash2 } from 'lucide-react';
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
  onDeleteAccount: () => void;
}

export const NavBar: React.FC<NavBarProps> = ({ 
  onNavigateAuth, 
  onNavigateHome,
  onNavigateMySurveys,
  onNavigateMyResponses,
  onDeleteAccount
}) => {
  const { currentUser, setCurrentUser, theme, toggleTheme } = useApp();

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
                 Mi Cuenta
              </DropdownMenuLabel>
              <DropdownMenuItem onClick={onNavigateMySurveys} className="p-3 cursor-pointer hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors rounded-none flex items-center gap-2 font-medium">
                <List size={16} /> Ver mis encuestas
              </DropdownMenuItem>
              <DropdownMenuItem onClick={onNavigateMyResponses} className="p-3 cursor-pointer hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors rounded-none flex items-center gap-2 font-medium">
                <MessageSquare size={16} /> Ver mis respuestas
              </DropdownMenuItem>
              {/* Seguridad (2FA) deshabilitado */}
              <DropdownMenuSeparator className="bg-black dark:bg-zinc-700 h-0.5 m-0" />
              <DropdownMenuItem onClick={() => setCurrentUser(null)} className="p-3 cursor-pointer hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-colors rounded-none flex items-center gap-2 font-medium">
                <LogOut size={16} /> Cerrar sesión
              </DropdownMenuItem>
              <DropdownMenuItem 
                onClick={() => {
                   if(window.confirm('¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) {
                     onDeleteAccount();
                   }
                }} 
                className="p-3 cursor-pointer hover:bg-red-600 hover:text-white transition-colors rounded-none flex items-center gap-2 font-medium text-red-600"
              >
                <Trash2 size={16} /> Eliminar cuenta
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : (
          <button 
            onClick={onNavigateAuth}
            className="flex items-center gap-2 px-4 py-2 border-2 border-black dark:border-white hover:bg-black hover:text-white dark:hover:bg-white dark:hover:text-black transition-all font-bold text-sm uppercase bg-white text-black"
          >
            <LogIn size={16} />
            Iniciar Sesión / Registrarse
          </button>
        )}
      </div>
    </nav>
  );
};
