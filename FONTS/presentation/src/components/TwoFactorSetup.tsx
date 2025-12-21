// TwoFactorSetup deshabilitat: exportem un stub per evitar que la UI intenti usar 2FA.
import React from 'react';

interface TwoFactorSetupProps {
  onClose: () => void;
}

export const TwoFactorSetup: React.FC<TwoFactorSetupProps> = (_props) => {
  // 2FA está deshabilitado — no renderizamos nada.
  return null;
};
