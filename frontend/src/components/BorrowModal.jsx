import { useState } from 'react';
import { api } from '../api.js';
import Modal from './Modal.jsx';

export default function BorrowModal({ item, onClose, onDone }) {
  const [days, setDays] = useState(Math.min(3, item.maxBorrowDays));
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await api.requestItem({ itemId: item.id, days: Number(days), message });
      onDone();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <Modal title={`Borrow “${item.title}”`} onClose={onClose}>
      <form onSubmit={submit} className="form">
        <p className="muted">
          {item.owner.name} will review your request. Once approved you'll get a QR code to show at pickup.
        </p>
        <label>
          How many days? (max {item.maxBorrowDays})
          <input type="number" min="1" max={item.maxBorrowDays} value={days} onChange={(e) => setDays(e.target.value)} required />
        </label>
        <label>
          Message (optional)
          <textarea rows="3" value={message} onChange={(e) => setMessage(e.target.value)}
                    placeholder="e.g. Need it for the DSA exam next week" />
        </label>
        {error && <div className="alert alert-error">{error}</div>}
        <div className="modal-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Sending…' : 'Send request'}</button>
        </div>
      </form>
    </Modal>
  );
}
