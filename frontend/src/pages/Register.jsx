import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', department: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <div className="auth-wrap">
      <form className="card auth-card form" onSubmit={submit}>
        <h1>Join UniLoop</h1>
        <p className="muted">Share what you're not using. Borrow what you need.</p>
        <label>Full name<input value={form.name} onChange={set('name')} required autoFocus /></label>
        <label>College email<input type="email" value={form.email} onChange={set('email')} required /></label>
        <label>Department<input value={form.department} onChange={set('department')} placeholder="e.g. Computer Science" /></label>
        <label>Password<input type="password" minLength={6} value={form.password} onChange={set('password')} required /></label>
        {error && <div className="alert alert-error">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
        <p className="muted small center">Already a member? <Link to="/login">Sign in</Link></p>
      </form>
    </div>
  );
}
