import { useEffect, useState } from 'react';
import axiosInstance from '../api/axiosInstance';
import { useAuth } from '../context/AuthContext';
import {
    PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend,
    BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';

const COLORS = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6'];

export default function Dashboard() {
    const [data, setData] = useState(null);
    const [loadingData, setLoadingData] = useState(true);
    const { user, logout } = useAuth();

    useEffect(() => {
        axiosInstance.get('/dashboard')
            .then((res) => setData(res.data))
            .catch((err) => console.error('Failed to load dashboard', err))
            .finally(() => setLoadingData(false));
    }, []);

    if (loadingData) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-slate-50">
                <p className="text-slate-500">Loading dashboard...</p>
            </div>
        );
    }

    const departmentChartData = data?.employeesByDepartment
        ? Object.entries(data.employeesByDepartment).map(([name, value]) => ({ name, value }))
        : [];

    return (
        <div className="min-h-screen bg-slate-50">
            {/* Topbar */}
            <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
                <div>
                    <h1 className="text-xl font-bold text-slate-800">HRMS Dashboard</h1>
                    <p className="text-sm text-slate-500">Welcome back, {user?.username}</p>
                </div>
                <button
                    onClick={logout}
                    className="text-sm font-medium text-slate-600 hover:text-red-600 transition px-4 py-2 rounded-lg hover:bg-red-50"
                >
                    Logout
                </button>
            </header>

            <main className="p-6 max-w-7xl mx-auto">
                {/* Stat Cards */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
                    <StatCard
                        label="Total Employees"
                        value={data?.totalEmployees ?? 0}
                        color="from-indigo-500 to-indigo-600"
                        icon="👥"
                    />
                    <StatCard
                        label="Present Today"
                        value={data?.presentTodayCount ?? 0}
                        color="from-emerald-500 to-emerald-600"
                        icon="✅"
                    />
                    <StatCard
                        label="Pending Leave Requests"
                        value={data?.pendingLeaveRequests ?? 0}
                        color="from-amber-500 to-amber-600"
                        icon="📋"
                    />
                    <StatCard
                        label="Departments"
                        value={departmentChartData.length}
                        color="from-purple-500 to-purple-600"
                        icon="🏢"
                    />
                </div>

                {/* Charts row */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                    {/* Department pie chart */}
                    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                        <h2 className="text-lg font-semibold text-slate-800 mb-4">Employees by Department</h2>
                        {departmentChartData.length > 0 ? (
                            <ResponsiveContainer width="100%" height={280}>
                                <PieChart>
                                    <Pie
                                        data={departmentChartData}
                                        dataKey="value"
                                        nameKey="name"
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={90}
                                        label
                                    >
                                        {departmentChartData.map((_, index) => (
                                            <Cell key={index} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                    <Legend />
                                </PieChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className="text-slate-400 text-center py-16">No department data yet</p>
                        )}
                    </div>

                    {/* Same data as bar chart */}
                    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                        <h2 className="text-lg font-semibold text-slate-800 mb-4">Department Headcount</h2>
                        {departmentChartData.length > 0 ? (
                            <ResponsiveContainer width="100%" height={280}>
                                <BarChart data={departmentChartData}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} />
                                    <Tooltip />
                                    <Bar dataKey="value" fill="#6366f1" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className="text-slate-400 text-center py-16">No department data yet</p>
                        )}
                    </div>
                </div>

                {/* Recent joiners */}
                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                    <h2 className="text-lg font-semibold text-slate-800 mb-4">Recent Joiners</h2>
                    {data?.recentJoiners?.length > 0 ? (
                        <div className="divide-y divide-slate-100">
                            {data.recentJoiners.map((joiner, idx) => (
                                <div key={idx} className="flex items-center justify-between py-3">
                                    <div className="flex items-center gap-3">
                                        <div className="w-9 h-9 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-semibold text-sm">
                                            {joiner.name?.charAt(0)}
                                        </div>
                                        <span className="font-medium text-slate-700">{joiner.name}</span>
                                    </div>
                                    <span className="text-sm text-slate-500">{joiner.dateOfJoining}</span>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="text-slate-400 text-center py-8">No recent joiners</p>
                    )}
                </div>
            </main>
        </div>
    );
}

function StatCard({ label, value, color, icon }) {
    return (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5">
            <div className={`w-11 h-11 rounded-xl bg-gradient-to-br ${color} flex items-center justify-center text-xl mb-3 shadow-lg`}>
                {icon}
            </div>
            <p className="text-2xl font-bold text-slate-800">{value}</p>
            <p className="text-sm text-slate-500 mt-0.5">{label}</p>
        </div>
    );
}