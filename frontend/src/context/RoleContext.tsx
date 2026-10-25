import React, { createContext, useContext, useState } from 'react';

// DEMO-ONLY: Role toggle context for switching between Admin and Student flows in prototype demo
export type DemoRole = 'Admin' | 'Student';

interface RoleContextType {
  role: DemoRole;
  setRole: (role: DemoRole) => void;
}

const RoleContext = createContext<RoleContextType | undefined>(undefined);

export const RoleProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // DEMO-ONLY: Defaults to Admin for administrative setup
  const [role, setRole] = useState<DemoRole>('Admin');

  return (
    <RoleContext.Provider value={{ role, setRole }}>
      {children}
    </RoleContext.Provider>
  );
};

export const useRole = (): RoleContextType => {
  const context = useContext(RoleContext);
  if (!context) {
    throw new Error('useRole must be used within a RoleProvider');
  }
  return context;
};
