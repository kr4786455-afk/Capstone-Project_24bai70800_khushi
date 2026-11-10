const STATUS_LABEL = {
  REQUESTED: 'Requested',
  APPROVED: 'Approved · awaiting pickup',
  HANDED_OVER: 'On loan',
  RETURNED: 'Returned',
  REJECTED: 'Declined',
  CANCELLED: 'Cancelled',
};

export function StatusBadge({ status, overdue }) {
  return (
    <span className={`badge status-${status.toLowerCase()}`}>
      {overdue ? 'Overdue' : STATUS_LABEL[status] ?? status}
    </span>
  );
}

export function TrustBadge({ score, level }) {
  const tone = score >= 85 ? 'gold' : score >= 65 ? 'green' : score >= 40 ? 'grey' : 'red';
  return (
    <span className={`trust trust-${tone}`} title={`Trust score ${score}/100`}>
      <span className="trust-dot" /> {score} · {level}
    </span>
  );
}

const STEPS = ['REQUESTED', 'APPROVED', 'HANDED_OVER', 'RETURNED'];
const STEP_LABEL = ['Requested', 'Approved', 'Handed over', 'Returned'];

/** Visual progress tracker for a transaction. */
export function StatusTracker({ status }) {
  if (status === 'REJECTED' || status === 'CANCELLED') {
    return <StatusBadge status={status} />;
  }
  const current = STEPS.indexOf(status);
  return (
    <ol className="tracker">
      {STEPS.map((s, i) => (
        <li key={s} className={i < current ? 'done' : i === current ? 'current' : ''}>
          <span className="tracker-dot">{i < current || status === 'RETURNED' ? '✓' : i + 1}</span>
          <span className="tracker-label">{STEP_LABEL[i]}</span>
        </li>
      ))}
    </ol>
  );
}
