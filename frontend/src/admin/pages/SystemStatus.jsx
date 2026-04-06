import React, { useState, useEffect } from 'react';
import { getSystemStatus, getAdminStats } from '../../shared/lib/api';

const SystemStatus = () => {
  const [statusData, setStatusData] = useState(null);
  const [statsData, setStatsData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [statusRes, statsRes] = await Promise.all([
        getSystemStatus(),
        getAdminStats()
      ]);
      setStatusData(statusRes.data);
      setStatsData(statsRes.data);
    } catch (err) {
      console.error('Failed to fetch system data', err);
      if (err.response && err.response.data && err.response.status === 503) {
        setStatusData(err.response.data);
      } else {
        setError('Could not connect to the server to verify status.');
      }
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    if (status === 'UP') return 'bg-green-500';
    if (status === 'OUT_OF_SERVICE' || status === 'DOWN') return 'bg-red-500';
    return 'bg-yellow-500';
  };

  if (loading && !statusData) {
    return (
      <div className="p-6 text-white max-w-5xl mx-auto">
        <h2 className="text-2xl font-bold mb-4">System Status</h2>
        <div className="text-gray-400">Checking system health...</div>
      </div>
    );
  }

  return (
    <div className="p-6 text-white max-w-5xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">System Status</h2>
        <button 
          onClick={fetchData}
          disabled={loading}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors font-medium"
        >
          {loading ? 'Refreshing...' : 'Refresh Status'}
        </button>
      </div>

      {error ? (
        <div className="bg-red-500/10 border border-red-500/50 p-4 mb-6 rounded-lg">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-200">{error}</p>
            </div>
          </div>
        </div>
      ) : null}

      <div className="bg-[#1F2023] border border-[#2e303a] rounded-xl shadow-lg overflow-hidden">
        <div className="p-6">
          <div className="flex items-center space-x-4 mb-2">
            <div className={`h-4 w-4 rounded-full ${getStatusColor(statusData?.status)} shadow-[0_0_10px_rgba(0,0,0,0.5)] ${statusData?.status === 'UP' ? 'shadow-green-500/50' : 'shadow-red-500/50'}`}></div>
            <div className="text-xl font-bold text-gray-100">
              Overall Status: {statusData?.status || 'UNKNOWN'}
            </div>
          </div>
          <p className="text-sm text-gray-400 mb-6 pl-8">
            Real-time infrastructure and application health
          </p>

          {statsData && (
            <div className="mb-8 border-t border-[#2e303a] pt-8">
              <h3 className="text-lg font-semibold text-gray-200 mb-4">Platform Overview</h3>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                <div className="bg-[#16171d] border border-[#2e303a] p-4 rounded-lg text-center">
                  <div className="text-sm text-gray-400 uppercase tracking-wider mb-1">Consultants</div>
                  <div className="text-2xl font-bold text-indigo-400">{statsData.totalConsultants}</div>
                </div>
                <div className="bg-[#16171d] border border-[#2e303a] p-4 rounded-lg text-center">
                  <div className="text-sm text-gray-400 uppercase tracking-wider mb-1">Clients</div>
                  <div className="text-2xl font-bold text-indigo-400">{statsData.totalClients}</div>
                </div>
                <div className="bg-[#16171d] border border-[#2e303a] p-4 rounded-lg text-center">
                  <div className="text-sm text-gray-400 uppercase tracking-wider mb-1">Booked Slots</div>
                  <div className="text-2xl font-bold text-indigo-400">{statsData.bookedAppointments}</div>
                </div>
                <div className="bg-[#16171d] border border-[#2e303a] p-4 rounded-lg text-center">
                  <div className="text-sm text-gray-400 uppercase tracking-wider mb-1">Awaiting Appr.</div>
                  <div className="text-2xl font-bold text-yellow-400">{statsData.appointmentsRequested}</div>
                </div>
                <div className="bg-[#16171d] border border-[#2e303a] p-4 rounded-lg text-center">
                  <div className="text-sm text-gray-400 uppercase tracking-wider mb-1">Awaiting Pay.</div>
                  <div className="text-2xl font-bold text-amber-500">{statsData.appointmentsWaitingPayment}</div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SystemStatus;