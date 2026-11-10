import { QRCodeSVG } from 'qrcode.react';
import Modal from './Modal.jsx';

/** Shown by the borrower. The owner scans this (or types the code) to confirm the handover / return. */
export default function QrModal({ title, token, hint, onClose }) {
  return (
    <Modal title={title} onClose={onClose}>
      <div className="qr-box">
        <div className="qr-frame"><QRCodeSVG value={token} size={220} level="M" /></div>
        <p className="muted center">{hint}</p>
        <label className="token-label">Backup code (if the owner can't scan)</label>
        <code className="token">{token}</code>
        <button className="btn btn-ghost" onClick={() => navigator.clipboard?.writeText(token)}>Copy code</button>
      </div>
    </Modal>
  );
}
