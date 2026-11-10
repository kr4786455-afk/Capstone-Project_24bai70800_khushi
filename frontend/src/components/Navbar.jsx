import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const link = ({ isActive }) => (isActive ? 'nav-link active' : 'nav-link');

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <NavLink to="/" className="brand">
          <svg width="28" height="28" viewBox="0 0 64 64" aria-hidden="true">
            <rect width="64" height="64" rx="14" fill="#14532d" />
            <path d="M20 32a12 12 0 0 1 20-9l4 4" fill="none" stroke="#fbbf24" strokeWidth="5" strokeLinecap="round" />
            <path d="M44 32a12 12 0 0 1-20 9l-4-4" fill="none" stroke="#fff" strokeWidth="5" strokeLinecap="round" />
          </svg>
          <span>UniLoop</span>
        </NavLink>
        <nav className="nav-links">
          <NavLink to="/" end className={link}>Browse</NavLink>
          <NavLink to="/dashboard" className={link}>Impact</NavLink>
          {user && <NavLink to="/my-items" className={link}>My items</NavLink>}
          {user && <NavLink to="/transactions" className={link}>Transactions</NavLink>}
        </nav>
        <div className="nav-user">
          {user ? (
            <>
              <span className="user-chip" title={`Trust score ${user.trustScore}/100`}>
                {user.name.split(' ')[0]} · <b>{user.trustScore}</b>
              </span>
              <button className="btn btn-ghost" onClick={() => { logout(); navigate('/'); }}>Sign out</button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="btn btn-ghost">Sign in</NavLink>
              <NavLink to="/register" className="btn btn-primary">Join</NavLink>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
