import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, tokenStore } from './api.js';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(!!tokenStore.get());

  const logout = useCallback(() => {
    tokenStore.clear();
    setUser(null);
  }, []);

  const refresh = useCallback(async () => {
    try {
      setUser(await api.me());
    } catch {
      logout();
    }
  }, [logout]);

  useEffect(() => {
    if (tokenStore.get()) refresh().finally(() => setLoading(false));
    window.addEventListener('uniloop:logout', logout);
    return () => window.removeEventListener('uniloop:logout', logout);
  }, [refresh, logout]);

  const handleAuth = ({ token, user }) => {
    tokenStore.set(token);
    setUser(user);
  };

  const login = async (email, password) => handleAuth(await api.login({ email, password }));
  const register = async (form) => handleAuth(await api.register(form));

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}
