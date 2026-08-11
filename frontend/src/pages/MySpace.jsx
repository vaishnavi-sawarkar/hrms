import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { useAuth } from '../context/AuthContext';

export default function MySpace() {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [checkedIn, setCheckedIn] = useState(false);
    const [attendanceMsg, setAttendanceMsg] = useState('');
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        axiosInstance.get('/employees/me')
            .then((res) => setProfile(res.data))
            .catch((err) => console.error('Failed to load profile', err))
            .finally(() => setLoading(false));
    }, []);

    const handleCheckIn = async () => {
        if (!profile) return;
        try {
            await axiosInstance.post(`/attendance/checkin/${profile.id}`);
            setAttendanceMsg('Checked in successfully!');
            setCheckedIn(true);
        } catch (err) {
            setAttendanceMsg(err.response?.data || 'Check-in failed');
        }
    };

    const handleCheckOut = async () => {
        if (!profile) return;
        try {
            await axiosInstance.post(`/attendance/checkout/${profile.id}`);
            setAttendanceMsg('Checked out successfully!');
        } catch (err) {
            setAttendanceMsg(err.response?.data || 'Check-out failed');
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-slate-50">
                <p className="text-slate-500">Loading your profile...</p>
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
                        <button className="text-indigo-600">My Space</button>
                        <button onClick={() => navigate('/my-leave')} className="text-slate-500 hover:text-indigo-600">
                            My Leave
                        </button>
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
                {/* Profile card */}
                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 mb-6">
                    <div className="flex items-center gap-4 mb-6">
                        <div className="w-16 h-16 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-white flex items-center justify-center text-xl font-bold">
                            {profile?.firstName?.charAt(0)}{profile?.lastName?.charAt(0)}
                        </div>
                        <div>
                            <h2 className="text-xl font-bold text-slate-800">
                                {profile?.firstName} {profile?.lastName}
                            </h2>
                            <p className="text-slate-500">{profile?.designationName || 'No designation set'}</p>
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 text-sm">
                        <InfoItem label="Email" value={profile?.email} />
                        <InfoItem label="Phone" value={profile?.phoneNumber} />
                        <InfoItem label="Department" value={profile?.departmentName} />
                        <InfoItem label="Date of Joining" value={profile?.dateOfJoining} />
                    </div>
                </div>

                {/* Attendance card */}
                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                    <h2 className="text-lg font-semibold text-slate-800 mb-4">Today's Attendance</h2>
                    <div className="flex gap-3">
                        <button
                            onClick={handleCheckIn}
                            className="bg-gradient-to-r from-emerald-500 to-emerald-600 text-white font-semibold px-5 py-2.5 rounded-lg hover:from-emerald-600 hover:to-emerald-700 transition shadow-lg shadow-emerald-500/30"
                        >
                            Check In
                        </button>
                        <button
                            onClick={handleCheckOut}
                            className="bg-gradient-to-r from-amber-500 to-amber-600 text-white font-semibold px-5 py-2.5 rounded-lg hover:from-amber-600 hover:to-amber-700 transition shadow-lg shadow-amber-500/30"
                        >
                            Check Out
                        </button>
                    </div>
                    {attendanceMsg && (
                        <p className="mt-4 text-sm text-slate-600">{attendanceMsg}</p>
                    )}
                </div>
            </main>
        </div>
    );
}

function InfoItem({ label, value }) {
    return (
        <div>
            <p className="text-slate-400 text-xs uppercase tracking-wide mb-1">{label}</p>
            <p className="text-slate-700 font-medium">{value || '—'}</p>
        </div>
    );
}