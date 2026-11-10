import { useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api, prettyCategory } from '../api.js';
import { useAuth } from '../auth.jsx';
import { TrustBadge } from '../components/Badges.jsx';

const GREEN = '#15803d';
const PALETTE = ['#14532d', '#15803d', '#65a30d', '#d97706', '#0f766e', '#a16207', '#78716c'];

function Stat({ label, value, sub, accent }) {
  return (
    <div className={`card stat ${accent ? 'stat-accent' : ''}`}>
      <span className="stat-label">{label}</span>
      <span className="stat-value">{value}</span>
      {sub && <span className="stat-sub">{sub}</span>}
    </div>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [mine, setMine] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.platformAnalytics().then(setData).catch((e) => setError(e.message));
  }, []);
  useEffect(() => {
    if (user) api.myImpact().then(setMine).catch(() => {});
    else setMine(null);
  }, [user]);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!data) return <p className="muted center pad">Loading impact…</p>;

  const cats = data.byCategory.map((c) => ({ ...c, label: prettyCategory(c.category) }));

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Campus impact</h1>
          <p className="muted">What our community has shared and saved so far.</p>
        </div>
      </div>

      <section className="stats">
        <Stat accent label="CO₂ avoided" value={`${data.co2SavedKg} kg`} sub={`≈ ${data.treesEquivalent} trees for a year`} />
        <Stat label="Successful shares" value={data.completedShares} sub={`${data.activeLoans} on loan now`} />
        <Stat label="Est. money saved" value={`₹${Math.round(data.moneySaved).toLocaleString('en-IN')}`} sub="vs. buying new" />
        <Stat label="Items listed" value={data.totalItems} sub={`${data.availableItems} available · ${data.totalUsers} members`} />
      </section>

      <section className="charts">
        <div className="card chart">
          <h3>Shares per month</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={data.monthly} margin={{ top: 8, right: 8, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e7e5e4" />
              <XAxis dataKey="month" tickLine={false} axisLine={false} fontSize={12} />
              <YAxis allowDecimals={false} tickLine={false} axisLine={false} fontSize={12} />
              <Tooltip cursor={{ fill: '#f5f5f4' }} />
              <Bar dataKey="shares" name="Shares" fill={GREEN} radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card chart">
          <h3>Most shared categories</h3>
          {cats.length === 0 ? <p className="muted">No completed shares yet.</p> : (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={cats} layout="vertical" margin={{ top: 8, right: 16, left: 20, bottom: 0 }}>
                <XAxis type="number" allowDecimals={false} hide />
                <YAxis type="category" dataKey="label" tickLine={false} axisLine={false} fontSize={12} width={90} />
                <Tooltip cursor={{ fill: '#f5f5f4' }} />
                <Bar dataKey="shares" name="Shares" radius={[0, 6, 6, 0]}>
                  {cats.map((_, i) => <Cell key={i} fill={PALETTE[i % PALETTE.length]} />)}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </section>

      <section className="two-col">
        <div className="card">
          <h3>Top sharers</h3>
          {data.leaderboard.length === 0 ? <p className="muted">Complete a share to get on the board.</p> : (
            <ol className="leaderboard">
              {data.leaderboard.map((e, i) => (
                <li key={e.userId}>
                  <span className="rank">{i + 1}</span>
                  <span className="lb-name">{e.name}</span>
                  <span className="muted small">{e.sharesCompleted} shares · {e.co2SavedKg} kg CO₂</span>
                  <TrustBadge score={e.trustScore} level={e.trustScore >= 85 ? 'Campus Champion' : e.trustScore >= 65 ? 'Trusted' : 'Neutral'} />
                </li>
              ))}
            </ol>
          )}
        </div>

        <div className="card">
          <h3>Your impact</h3>
          {!user && <p className="muted">Sign in to see how much you've contributed.</p>}
          {mine && (
            <>
              <div className="mine-grid">
                <div><b>{mine.itemsLent}</b><span>items lent</span></div>
                <div><b>{mine.itemsBorrowed}</b><span>items borrowed</span></div>
                <div><b>{mine.co2SavedKg} kg</b><span>CO₂ avoided</span></div>
                <div><b>₹{Math.round(mine.moneySaved)}</b><span>saved</span></div>
              </div>
              <div className="trust-meter">
                <div className="trust-meter-head"><span>Trust score</span><b>{mine.trustScore} · {mine.trustLevel}</b></div>
                <div className="meter"><div style={{ width: `${mine.trustScore}%` }} /></div>
                <p className="muted small">Return on time (+5) · lend successfully (+2) · return late (−10)</p>
              </div>
            </>
          )}
        </div>
      </section>
    </>
  );
}
