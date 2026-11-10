import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { StatusTracker, StatusBadge } from '../components/Badges.jsx';
import QrModal from '../components/QrModal.jsx';

const fmt = (d) => (d ? new Date(d).toLocaleDateString(undefined, { day: 'numeric', month: 'short' }) : '—');

/** Owner types/pastes the code from the borrower's QR to confirm a handover or return. */
function VerifyBox({ label, onSubmit }) {
  const [token, setToken] = useState('');
  const [busy, setBusy] = useState(false);
  return (
    <form className="verify" onSubmit={async (e) => {
      e.preventDefault();
      setBusy(true);
      try { await onSubmit(token.trim()); setToken(''); } finally { setBusy(false); }
    }}>
      <input value={token} onChange={(e) => setToken(e.target.value)} placeholder="Paste the code from the borrower's QR" required />
      <button className="btn btn-primary" disabled={busy}>{label}</button>
    </form>
  );
}

function TransactionRow({ t, meId, run, showQr }) {
  const iAmBorrower = t.borrower.id === meId;
  const item = t.item;

  return (
    <article className="card tx">
      <div className="tx-head">
        <div>
          <h3>{item.title}</h3>
          <p className="muted small">
            {iAmBorrower ? `Borrowing from ${item.owner.name}` : `Lending to ${t.borrower.name}`}
            {' · '}requested {fmt(t.requestedAt)}
            {t.dueDate && ['REQUESTED', 'APPROVED', 'HANDED_OVER'].includes(t.status) && <> · due {fmt(t.dueDate)}</>}
          </p>
        </div>
        {t.overdue && <StatusBadge status="HANDED_OVER" overdue />}
      </div>

      <StatusTracker status={t.status} />
      {t.message && <p className="quote">“{t.message}”</p>}

      <div className="tx-actions">
        {/* owner actions */}
        {!iAmBorrower && t.status === 'REQUESTED' && (
          <>
            <button className="btn btn-primary" onClick={() => run(() => api.approve(t.id))}>Approve</button>
            <button className="btn btn-ghost" onClick={() => run(() => api.reject(t.id))}>Decline</button>
          </>
        )}
        {!iAmBorrower && t.status === 'APPROVED' && (
          <VerifyBox label="Confirm handover" onSubmit={(tok) => run(() => api.verifyHandover(t.id, tok))} />
        )}
        {!iAmBorrower && t.status === 'HANDED_OVER' && (
          <VerifyBox label="Confirm return" onSubmit={(tok) => run(() => api.verifyReturn(t.id, tok))} />
        )}

        {/* borrower actions */}
        {iAmBorrower && t.status === 'APPROVED' && t.handoverToken && (
          <button className="btn btn-primary" onClick={() => showQr({
            title: 'Pickup QR', token: t.handoverToken,
            hint: `Show this to ${item.owner.name} when you collect the item.` })}>
            Show pickup QR
          </button>
        )}
        {iAmBorrower && t.status === 'HANDED_OVER' && t.returnToken && (
          <button className="btn btn-primary" onClick={() => showQr({
            title: 'Return QR', token: t.returnToken,
            hint: `Show this to ${item.owner.name} when you give the item back.` })}>
            Show return QR
          </button>
        )}
        {iAmBorrower && ['REQUESTED', 'APPROVED'].includes(t.status) && (
          <button className="btn btn-ghost" onClick={() => run(() => api.cancel(t.id))}>Cancel</button>
        )}
      </div>
    </article>
  );
}

export default function Transactions() {
  const { user, refresh } = useAuth();
  const [txs, setTxs] = useState(null);
  const [tab, setTab] = useState('all');
  const [error, setError] = useState('');
  const [qr, setQr] = useState(null);

  const load = () => api.transactions().then(setTxs).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  // run an action, then reload (trust scores may have changed after a return)
  const run = async (fn) => {
    setError('');
    try { await fn(); await Promise.all([load(), refresh()]); } catch (err) { setError(err.message); }
  };

  const filtered = (txs ?? []).filter((t) =>
    tab === 'all' ? true : tab === 'borrowing' ? t.borrower.id === user.id : t.borrower.id !== user.id);
  const pending = (txs ?? []).filter((t) => t.borrower.id !== user.id && t.status === 'REQUESTED').length;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Transactions</h1>
          <p className="muted">Track every request from first message to returned item.</p>
        </div>
      </div>

      <div className="tabs">
        {[['all', 'All'], ['borrowing', 'Borrowing'], ['lending', `Lending${pending ? ` (${pending} new)` : ''}`]].map(([k, label]) => (
          <button key={k} className={tab === k ? 'tab active' : 'tab'} onClick={() => setTab(k)}>{label}</button>
        ))}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {txs === null && !error && <p className="muted center pad">Loading…</p>}
      {txs && filtered.length === 0 && <p className="muted center pad">No transactions here yet.</p>}

      <div className="stack">
        {filtered.map((t) => <TransactionRow key={t.id} t={t} meId={user.id} run={run} showQr={setQr} />)}
      </div>

      {qr && <QrModal {...qr} onClose={() => setQr(null)} />}
    </>
  );
}
