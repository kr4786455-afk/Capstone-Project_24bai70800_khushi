import { useEffect, useState } from 'react';
import { api, CATEGORIES, CONDITIONS, prettyCategory } from '../api.js';
import ItemCard from '../components/ItemCard.jsx';

const EMPTY = { title: '', description: '', category: 'BOOKS', condition: 'GOOD', imageUrl: '', estimatedValue: '', maxBorrowDays: 7 };

export default function MyItems() {
  const [items, setItems] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const load = () => api.myItems().then(setItems).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await api.createItem({
        ...form,
        estimatedValue: form.estimatedValue === '' ? 0 : Number(form.estimatedValue),
        maxBorrowDays: Number(form.maxBorrowDays),
      });
      setForm(EMPTY);
      setOpen(false);
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const act = (fn) => async () => {
    setError('');
    try { await fn(); await load(); } catch (err) { setError(err.message); }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>My items</h1>
          <p className="muted">Things you're sharing with the campus.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setOpen(!open)}>{open ? 'Close' : '+ Share an item'}</button>
      </div>

      {open && (
        <form className="card form form-grid" onSubmit={submit}>
          <label className="span-2">Title<input value={form.title} onChange={set('title')} required placeholder="e.g. Casio fx-991EX Calculator" /></label>
          <label>Category
            <select value={form.category} onChange={set('category')}>
              {CATEGORIES.map((c) => <option key={c} value={c}>{prettyCategory(c)}</option>)}
            </select>
          </label>
          <label>Condition
            <select value={form.condition} onChange={set('condition')}>
              {CONDITIONS.map((c) => <option key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</option>)}
            </select>
          </label>
          <label>Price if bought new (₹)<input type="number" min="0" value={form.estimatedValue} onChange={set('estimatedValue')} /></label>
          <label>Max borrow days<input type="number" min="1" max="60" value={form.maxBorrowDays} onChange={set('maxBorrowDays')} required /></label>
          <label className="span-2">Image URL (optional)<input type="url" value={form.imageUrl} onChange={set('imageUrl')} placeholder="https://…" /></label>
          <label className="span-2">Description<textarea rows="3" value={form.description} onChange={set('description')} /></label>
          {error && <div className="alert alert-error span-2">{error}</div>}
          <div className="span-2 modal-actions"><button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Share item'}</button></div>
        </form>
      )}

      {!open && error && <div className="alert alert-error">{error}</div>}
      {items === null && <p className="muted center pad">Loading…</p>}
      {items?.length === 0 && <p className="muted center pad">You haven't shared anything yet. Got a spare textbook or calculator?</p>}

      <section className="grid">
        {items?.map((item) => (
          <div key={item.id} className="item-wrap">
            <ItemCard item={item} isMine />
            <div className="item-actions">
              <button className="btn btn-ghost" onClick={act(() => api.setAvailability(item.id, !item.available))}>
                {item.available ? 'Pause sharing' : 'Make available'}
              </button>
              <button className="btn btn-danger"
                onClick={() => window.confirm(`Delete “${item.title}”?`) && act(() => api.deleteItem(item.id))()}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </section>
    </>
  );
}
