import { prettyCategory } from '../api.js';
import { TrustBadge } from './Badges.jsx';

const EMOJI = { BOOKS: '📚', CALCULATORS: '🧮', LAB_EQUIPMENT: '🥼', STATIONERY: '✏️', ELECTRONICS: '🔌', SPORTS: '🏸', OTHER: '📦' };

export function ItemImage({ item }) {
  return item.imageUrl ? (
    <img className="card-img" src={item.imageUrl} alt={item.title} loading="lazy"
         onError={(e) => { e.currentTarget.style.display = 'none'; }} />
  ) : (
    <div className="card-img placeholder" aria-hidden="true">{EMOJI[item.category] ?? '📦'}</div>
  );
}

export default function ItemCard({ item, onRequest, isMine }) {
  return (
    <article className={`card item-card ${item.available ? '' : 'unavailable'}`}>
      <div className="card-media">
        <ItemImage item={item} />
        <span className={`avail-pill ${item.available ? 'yes' : 'no'}`}>
          {item.available ? 'Available' : 'Not available'}
        </span>
      </div>
      <div className="card-body">
        <div className="chips">
          <span className="chip">{prettyCategory(item.category)}</span>
          <span className="chip chip-soft">{item.condition.toLowerCase()}</span>
        </div>
        <h3>{item.title}</h3>
        {item.description && <p className="muted clamp">{item.description}</p>}
        <div className="card-meta">
          <span>Up to <b>{item.maxBorrowDays}</b> days</span>
          {item.estimatedValue > 0 && <span>Worth ₹{Math.round(item.estimatedValue)}</span>}
        </div>
        <div className="card-foot">
          <div className="owner">
            <span className="muted small">Shared by {item.owner.name}</span>
            <TrustBadge score={item.owner.trustScore} level={item.owner.trustLevel} />
          </div>
          {onRequest && !isMine && (
            <button className="btn btn-primary" disabled={!item.available} onClick={() => onRequest(item)}>
              Borrow
            </button>
          )}
          {isMine && <span className="chip chip-soft">Yours</span>}
        </div>
      </div>
    </article>
  );
}
