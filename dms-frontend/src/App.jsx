import React, { useState, useEffect } from 'react';
import axios from 'axios';


const API_URL = 'http://localhost:8080/api';

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoggedIn, setIsLoggedIn] = useState(!!token);

  // DMS States
  const [documents, setDocuments] = useState([]);
  const [categories, setCategories] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Upload States
  const [title, setTitle] = useState('');
  const [file, setFile] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [activeDocForVersion, setActiveDocForVersion] = useState(null);
  const [versionFile, setVersionFile] = useState(null);
  const [isRegistering, setIsRegistering] = useState(false);

  useEffect(() => {
    if (isLoggedIn) {
      loadWorkspace();
    }
  }, [isLoggedIn, searchQuery]);

  const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${token}` } });

  const loadWorkspace = async () => {
    try {
      const docRes = await axios.get(`${API_URL}/documents/search?query=${searchQuery}`, getAuthHeaders());
      const catRes = await axios.get(`${API_URL}/categories`, getAuthHeaders());
      setDocuments(docRes.data || []);
      setCategories(catRes.data || []);
    } catch (err) {
      console.error("Fehler beim Laden der Daten", err);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_URL}/auth/login`, { username, password });
      localStorage.setItem('token', res.data.token);
      setToken(res.data.token);
      setIsLoggedIn(true);
    } catch (err) {
      alert('Login fehlgeschlagen. Bitte überprüfen Sie Ihre Anmeldedaten.');
    }
  };

  const [email, setEmail] = useState('');
  
  const handleRegister = async (e) => {
  e.preventDefault();
  try {
    await axios.post(`${API_URL}/auth/register`, { username, password, email }); // <-- email hinzugefügt
    alert('Registrierung erfolgreich! Sie können sich jetzt einloggen.');
    setIsRegistering(false);
    setPassword('');
    setEmail('');
  } catch (err) {
    alert(err.response?.data?.message || 'Registrierung fehlgeschlagen.');
  }
};
  const handleUploadDocument = async (e) => {
    e.preventDefault();
    if (!file || !selectedCategory) return;

    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    formData.append('categoryId', selectedCategory);

    try {
      await axios.post(`${API_URL}/documents`, formData, {
        headers: { 'Content-Type': 'multipart/form-data', Authorization: `Bearer ${token}` }
      });
      setTitle('');
      setFile(null);
      loadWorkspace();
    } catch (err) {
      alert('Fehler beim Upload des Dokuments.');
    }
  };

  const handleUploadNewVersion = async (e) => {
    e.preventDefault();
    if (!versionFile || !activeDocForVersion) return;

    const formData = new FormData();
    formData.append('file', versionFile);

    try {
      await axios.post(`${API_URL}/documents/${activeDocForVersion}/versions`, formData, {
        headers: { 'Content-Type': 'multipart/form-data', Authorization: `Bearer ${token}` }
      });
      setVersionFile(null);
      setActiveDocForVersion(null);
      loadWorkspace();
    } catch (err) {
      alert('Fehler beim Erstellen der neuen Version.');
    }
  };

  // 1. ANMELDE- UND REGISTRIERUNGS-MASKE (Liegt korrekt INNERHALB der App-Funktion)
  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center p-4">
        <div className="bg-slate-800 p-8 rounded-2xl shadow-2xl max-w-md w-full border border-slate-700">
          <h2 className="text-3xl font-bold text-white text-center mb-2 tracking-tight">
            {isRegistering ? 'Account erstellen' : 'DMS Enterprise'}
          </h2>
          <p className="text-slate-400 text-center text-sm mb-6">
            {isRegistering ? 'Registrieren Sie sich für das DMS-Netzwerk.' : 'Bitte melden Sie sich an.'}
          </p>

          <form onSubmit={isRegistering ? handleRegister : handleLogin} className="space-y-4">
            <div>
              <label className="text-slate-300 block mb-1 text-sm font-medium">Benutzername</label>
              <input type="text" className="w-full bg-slate-700 text-white rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500 transition" value={username} onChange={e => setUsername(e.target.value)} required />
            </div>
            <div>
              <label className="text-slate-300 block mb-1 text-sm font-medium">Passwort</label>
              <input type="password" className="w-full bg-slate-700 text-white rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500 transition" value={password} onChange={e => setPassword(e.target.value)} required />
            </div>
            <button type="submit" className={`w-full text-white font-bold p-3 rounded-lg transition duration-200 mt-2 shadow-lg ${isRegistering ? 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-600/20' : 'bg-blue-600 hover:bg-blue-700 shadow-blue-600/20'}`}>
              {isRegistering ? 'Registrieren' : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-center border-t border-slate-700 pt-4">
            <button onClick={() => { setIsRegistering(!isRegistering); setPassword(''); }} className="text-sm font-medium text-blue-400 hover:text-blue-300 transition underline decoration-dotted">
              {isRegistering ? 'Bereits ein Konto? Hier einloggen' : 'Noch kein Konto? Jetzt registrieren'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 2. MAIN APPLICATION WORKSPACE (Ebenfalls korrekt innerhalb der App-Funktion)
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans antialiased text-slate-800">
      <header className="bg-white border-b border-slate-200 sticky top-0 z-40 px-8 py-4 flex justify-between items-center shadow-sm">
        <div className="flex items-center space-x-3">
          <div className="bg-blue-600 text-white px-3 py-1.5 rounded-lg font-black text-xl tracking-wider">DMS</div>
          <h1 className="text-xl font-bold text-slate-900">Cloud Storage & Archive</h1>
        </div>
        <button onClick={() => { localStorage.removeItem('token'); setIsLoggedIn(false); }} className="bg-slate-100 hover:bg-red-50 hover:text-red-600 text-slate-600 px-4 py-2 rounded-lg font-semibold transition text-sm">Abmelden</button>
      </header>

      <div className="flex-1 max-w-7xl w-full mx-auto p-6 grid grid-cols-1 lg:grid-cols-4 gap-6">
        <aside className="space-y-6 lg:col-span-1">
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
            <h3 className="text-base font-bold text-slate-900 mb-4 flex items-center"><span className="w-2.5 h-2.5 bg-blue-600 rounded-full mr-2"></span> Neues Dokument</h3>
            <form onSubmit={handleUploadDocument} className="space-y-3">
              <input type="text" placeholder="Dokumententitel..." className="w-full bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500 transition" value={title} onChange={e => setTitle(e.target.value)} required />
              <select className="w-full bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500 transition" value={selectedCategory} onChange={e => setSelectedCategory(e.target.value)} required>
                <option value="">Kategorie wählen...</option>
                {categories && categories.length > 0 && categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              <input type="file" className="w-full text-xs text-slate-500 file:mr-2 file:py-2 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 cursor-pointer" onChange={e => setFile(e.target.files)} required />
              <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-lg text-sm transition shadow-sm">Indexieren & Speichern</button>
            </form>
          </div>

          {activeDocForVersion && (
            <div className="bg-amber-50/60 p-5 rounded-xl border border-amber-200 shadow-sm">
              <h3 className="text-sm font-bold text-amber-900 mb-1">Neue Version einspielen</h3>
              <p className="text-xs text-amber-700 mb-3">Erstellt eine inkrementelle Dateiversion.</p>
              <form onSubmit={handleUploadNewVersion} className="space-y-3">
                <input type="file" className="w-full text-xs text-slate-500 file:mr-2 file:py-2 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-amber-100 file:text-amber-700 hover:file:bg-amber-200 cursor-pointer" onChange={e => setVersionFile(e.target.files)} required />
                <div className="flex space-x-2">
                  <button type="submit" className="flex-1 bg-amber-600 hover:bg-amber-700 text-white font-semibold py-2 rounded-lg text-xs transition">Upload vNext</button>
                  <button type="button" onClick={() => setActiveDocForVersion(null)} className="bg-white border border-slate-300 text-slate-700 px-3 py-2 rounded-lg text-xs font-medium hover:bg-slate-50 transition">Abbrechen</button>
                </div>
              </form>
            </div>
          )}
        </aside>

        <main className="lg:col-span-3 space-y-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center space-x-3">
            <input type="text" placeholder="Dokumente durchsuchen..." className="flex-1 bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500 transition" value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
            <button onClick={loadWorkspace} className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-4 py-2 rounded-lg text-sm transition shadow-sm">Suchen</button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {documents && documents.length > 0 ? documents.map(doc => (
              <div key={doc.id} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                <div>
                  <h3 className="text-base font-bold text-slate-900 mb-1">{doc.title}</h3>
                  <p className="text-xs text-slate-500 mb-2">Kategorie: {doc.category?.name || 'Unbekannt'}</p>
                  <p className="text-xs text-slate-400 mb-3
                  ">Versionen: {doc.versions.length}</p>

                  <div className="flex space-x-2">
                    <button onClick={() => setActiveDocForVersion(doc.id)} className="flex-1 bg-amber-600 hover:bg-amber-700 text-white font-semibold py-2 rounded-lg text-xs transition">Neue Version</button>
                    <a href={`${API_URL}/documents/${doc.id}/download`} target="_blank" rel="noopener noreferrer" className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-3 py-2 rounded-lg text-xs transition">Download</a>
                  </div>
                </div>
              </div>
            )) : (
              <p className="text-slate-500 col-span-full text-center">Keine Dokumente gefunden.</p>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}

