import { createContext, useContext, useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        const role = localStorage.getItem('role');

        if (token && username && role) {
            setUser({ username, role, token });
        }
        setLoading(false);
    }, []);

    const login = async (usernameInput, password) => {
        const response = await axiosInstance.post('/auth/login', {
            username: usernameInput,
            password,
        });

        const { token, username, role } = response.data;

        localStorage.setItem('token', token);
        localStorage.setItem('username', username);
        localStorage.setItem('role', role);

        setUser({ username, role, token });
        return response.data;
    };

    const register = async (usernameInput, password, role) => {
        const response = await axiosInstance.post('/auth/register', {
            username: usernameInput,
            password,
            role,
        });

        const { token, username: returnedUsername, role: returnedRole } = response.data;

        localStorage.setItem('token', token);
        localStorage.setItem('username', returnedUsername);
        localStorage.setItem('role', returnedRole);

        setUser({ username: returnedUsername, role: returnedRole, token });
        return response.data;
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}