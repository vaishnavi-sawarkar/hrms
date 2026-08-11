import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { useAuth } from '../context/AuthContext';

export default function MyLeave() {
    const [profile, setProfile] = useState(null);
    const [leaveHistory, setLeaveHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [reason, setReason] = useState('');
    const [applyError, setApplyError] = useState('');
    const [applying, setApplying] = useState(false);

    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const loadData = async () => {
        setLoading(true);
        try {
            const profileRes = await axiosInstance.get('/employees/me');
            setProfile(profileRes.data);

            const historyRes = await axiosInstance.get(`/leave/history/${profileRes.data.id}`);
            setLeaveHistory(historyRes.data);
        } catch (err) {
            console.error('Failed to load leave data', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const handleApply = async (e) => {
        e.preventDefault();
        setApplyError('');
        setApplying(true);

        try {
            await axiosInstance.post(
                `/leave/apply?employeeId=${profile.id}&startDate=${startDate}&endDate=${endDate}&reason=${encodeURIComponent(reason)}`
            );
            setStartDate('');
            setEndDate('');
            setReason('');
            loadData();
        } catch (err) {
            setApplyError(err.response?.data || 'Failed to submit leave request');
        } finally {
            setApplying(false);
        }
    };

    const statusColor = (status) => {
        if (status === 'APPROVED') return 'bg-emerald-100 text-emerald-700';
        if (status === 'REJECTED') return 'bg-red-100 text-red-700';
        return 'bg-amber-100 text-amber-700';
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-slate-50">
                <p className="text-slate-500">Loading...</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-50">
            {/* Topbar */}
            <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
                <div className="flex items-center gap-6">
                    <h1 className="text-xl font-bold text-slate-800">HRMS</h1>
                    <nav className="flex gap-4 text-sm font-medium">
                        <button onClick={() => navigate('/my-space')} className="text-slate-500 hover:text-indigo-600">
                            My Space
                        </button>
                        <button className="text-indigo-600">My Leave</button>
                    </nav>
                </div>
                <div className="flex items-center gap-4">
                    <span className="text-sm text-slate-500">{user?.username}</span>
                    <button
                        onClick={logout}
                        className="text-sm font-medium text-slate-600 hover:text-red-600 transition px-4 py-2 rounded-lg hover:bg-red-50"
                    >
                        Logout
                    </button>
                </div>
            </header>

            <main className="p-6 max-w-4xl mx-auto">
                {/* Apply for leave */}
                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 mb-6">
                    <h2 className="text-lg font-semibold text-slate-800 mb-4">Apply for Leave</h2>
                    <form onSubmit={handleApply} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Start Date</label>
                                <input
                                    type="date"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    required
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">End Date</label>
                                <input
                                    type="date"
                                    value={endDate}
                                    onChange={(e) => setEndDate(e.target.value)}
                                    required
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                />
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Reason</label>
                            <input
                                type="text"
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="e.g. Family function"
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            />
                        </div>
                        {applyError && (
                            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-2.5">
                                {applyError}
                            </div>
                        )}
                        <button
                            type="submit"
                            disabled={applying}
                            className="bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold px-5 py-2.5 rounded-lg hover:from-indigo-700 hover:to-purple-700 transition disabled:opacity-60"
                        >
                            {applying ? 'Submitting...' : 'Submit Request'}
                        </button>
                    </form>
                </div>

                {/* Leave history */}
                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                    <h2 className="text-lg font-semibold text-slate-800 mb-4">My Leave History</h2>
                    {leaveHistory.length === 0 ? (
                        <p className="text-slate-400 text-center py-8">No leave requests yet</p>
                    ) : (
                        <div className="divide-y divide-slate-100">
                            {leaveHistory.map((leave) => (
                                <div key={leave.id} className="py-4 flex items-center justify-between">
                                    <div>
                                        <p className="font-medium text-slate-700">
                                            {leave.startDate} → {leave.endDate}
                                        </p>
                                        <p className="text-sm text-slate-500">{leave.reason || 'No reason given'}</p>
                                    </div>
                                    <span className={`text-xs font-semibold px-3 py-1 rounded-full ${statusColor(leave.status)}`}>
                    {leave.status}
                  </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}