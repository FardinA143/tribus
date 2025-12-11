// TwoFactorSetup deshabilitado: exportar un stub para evitar que la UI intente usar 2FA.
import React from 'react';

interface TwoFactorSetupProps {
  onClose: () => void;
}

export const TwoFactorSetup: React.FC<TwoFactorSetupProps> = (_props) => {
  // 2FA está deshabilitado — no renderizamos nada.
  return null;
};
