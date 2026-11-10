// Thin fetch wrapper: adds the JWT, parses JSON, and turns API errors into Error(message).
const TOKEN_KEY = 'uniloop_token';

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  const token = tokenStore.get();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 401 && token) {
      tokenStore.clear();
      window.dispatchEvent(new Event('uniloop:logout'));
    }
    throw new Error(data?.error || (res.status === 401 ? 'Please sign in' : `Request failed (${res.status})`));
  }
  return data;
}

const qs = (params) => {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== '' && v !== null && v !== undefined && v !== false) p.set(k, v);
  });
  const s = p.toString();
  return s ? `?${s}` : '';
};

export const api = {
  register: (b) => request('POST', '/auth/register', b),
  login: (b) => request('POST', '/auth/login', b),
  me: () => request('GET', '/users/me'),

  items: (params = {}) => request('GET', `/items${qs(params)}`),
  myItems: () => request('GET', '/items/mine'),
  createItem: (b) => request('POST', '/items', b),
  updateItem: (id, b) => request('PUT', `/items/${id}`, b),
  setAvailability: (id, value) => request('PATCH', `/items/${id}/availability?value=${value}`),
  deleteItem: (id) => request('DELETE', `/items/${id}`),

  transactions: () => request('GET', '/transactions'),
  requestItem: (b) => request('POST', '/transactions', b),
  approve: (id) => request('POST', `/transactions/${id}/approve`),
  reject: (id) => request('POST', `/transactions/${id}/reject`),
  cancel: (id) => request('POST', `/transactions/${id}/cancel`),
  verifyHandover: (id, token) => request('POST', `/transactions/${id}/verify-handover`, { token }),
  verifyReturn: (id, token) => request('POST', `/transactions/${id}/verify-return`, { token }),

  platformAnalytics: () => request('GET', '/analytics/public'),
  myImpact: () => request('GET', '/analytics/me'),
};

export const CATEGORIES = ['BOOKS', 'CALCULATORS', 'LAB_EQUIPMENT', 'STATIONERY', 'ELECTRONICS', 'SPORTS', 'OTHER'];
export const CONDITIONS = ['NEW', 'GOOD', 'FAIR', 'WORN'];
export const prettyCategory = (c) => c.charAt(0) + c.slice(1).toLowerCase().replace('_', ' ');
