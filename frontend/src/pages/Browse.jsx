import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, CATEGORIES, prettyCategory } from '../api.js';
import { useAuth } from '../auth.jsx';
import ItemCard from '../components/ItemCard.jsx';
import BorrowModal from '../components/BorrowModal.jsx';

export default function Browse() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [q, setQ] = useState('');
  const [category, setCategory] = useState('');
  const [onlyAvailable, setOnlyAvailable] = useState(false);
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [notice, setNotice] = useState('');

  const load = () =>
    api.items({ q, category, onlyAvailable })
      .then((d) => { setItems(d); setError(''); })
      .catch((e) => setError(e.message));

  // debounce the search box
  useEffect(() => {
    const t = setTimeout(load, 250);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [q, category, onlyAvailable]);

  const onRequest = (item) => (user ? setSelected(item) : navigate('/login'));

  return (
    <>
      <section className="hero">
        <h1>Share More. Waste Less. <span>Grow Together.</span></h1>
        <p>Borrow books, calculators and lab gear from students on your campus — and lend what you're not using.</p>
      </section>

      <section className="filters card">
        <input className="search" type="search" placeholder="Search books, calculators, kits…"
               value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">All categories</option>
          {CATEGORIES.map((c) => <option key={c} value={c}>{prettyCategory(c)}</option>)}
        </select>
        <label className="check">
          <input type="checkbox" checked={onlyAvailable} onChange={(e) => setOnlyAvailable(e.target.checked)} />
          Available only
        </label>
      </section>

      {notice && <div className="alert alert-ok">{notice}</div>}
      {error && <div className="alert alert-error">{error}</div>}
      {items === null && !error && <p className="muted center pad">Loading items…</p>}
      {items?.length === 0 && <p className="muted center pad">Nothing matches your search yet. Be the first to share something!</p>}

      <section className="grid">
        {items?.map((item) => (
          <ItemCard key={item.id} item={item} onRequest={onRequest} isMine={user?.id === item.owner.id} />
        ))}
      </section>

      {selected && (
        <BorrowModal item={selected} onClose={() => setSelected(null)}
          onDone={() => { setSelected(null); setNotice('Request sent! Track it under Transactions.'); load(); }} />
      )}
    </>
  );
}
