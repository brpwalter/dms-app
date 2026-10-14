import React,  { useState, useEffect } from 'react';
import DocumentManager from './DocumentManager'; 
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState(''); // <-- 1. NEUER STATE FÜR EMAIL
  const [isLoggedIn, setIsLoggedIn] = useState(!!token);

  const [selectedFile, setSelectedFile] = useState(null);
const [documentTitle, setDocumentTitle] = useState('');


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

  // Aktualisiert auf den funktionierenden GET-Endpunkt
  const loadWorkspace = async () => {
    try {
      const docRes = await axios.get(`${API_URL}/documents`, getAuthHeaders());
      const catRes = await axios.get(`${API_URL}/categories`, getAuthHeaders());
      
      // Filtere die Dokumente clientseitig, falls eine Suchanfrage eingetippt wurde
      const allDocs = docRes.data || [];
      if (searchQuery.trim() !== '') {
        const filtered = allDocs.filter(doc => 
          doc.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          doc.fileName?.toLowerCase().includes(searchQuery.toLowerCase())
        );
        setDocuments(filtered);
      } else {
        setDocuments(allDocs);
      }
      
      setCategories(catRes.data || []);
    } catch (err) {
      console.error("Fehler beim Laden der Daten", err);
    }
  };

    /*const fetchDocuments = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/documents");
      
      // Absicherung: Nur setzen, wenn das Backend ein Array liefert
      if (Array.isArray(response.data)) {
        setDocuments(response.data);
      } else {
        console.error("Erwartete Dokumenten-Liste, erhielt aber:", response.data);
      }
    } catch (error) {
      console.error("Fehler beim Laden der Dokumente vom Server:", error);
    }
  };*/

  
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedFile(e.target.files[0]);
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

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API_URL}/auth/register`, { username, password, email }); 
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
    if (!selectedFile) {
      alert("Bitte wähle zuerst eine Datei aus!");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);
    formData.append("title", documentTitle || selectedFile.name); 
    if (selectedCategory) {
      formData.append("categoryId", selectedCategory);
    }

    try {
      const response = await axios.post(`${API_URL}/documents`, formData);
      console.log("Upload erfolgreich:", response.data);
      alert("Datei erfolgreich hochgeladen!");
      
  //    setSelectedFile(null);
  //    setDocumentTitle('');
     loadWorkspace(); // Erzwingt das sofortige Neuladen der Tabelle im Mittelteil!
    // HIER DIE LISTE AKTUALISIEREN:
 // fetchDocuments(); 
    } catch (error) {
      console.error("Fehler beim Upload des Dokuments", error);
    }
  };

  const [newCategoryName, setNewCategoryName] = useState('');

// Funktion zum Senden der neuen Kategorie an das Backend:
const handleCreateCategory = async (e) => {
    e.preventDefault();
    if (!newCategoryName.trim()) return;

    try {
      await axios.post(`${API_URL}/categories`, { name: newCategoryName }, getAuthHeaders());
      setNewCategoryName(''); 
      loadWorkspace(); 
      alert('Kategorie erfolgreich erstellt!');
    } catch (err) {
      console.error("Fehler beim Erstellen der Kategorie", err);
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

  // ANMELDE- UND REGISTRIERUNGS-MASKE
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
              <input id="username" type="text" className="w-full bg-slate-700 text-white rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500 transition" value={username} onChange={e => setUsername(e.target.value)} required />
            </div>

            {/* 3. VISUELLES E-MAIL FELD NUR BEI DER REGISTRIERUNG ANZEIGEN */}
            {isRegistering && (
              <div>
                <label className="text-slate-300 block mb-1 text-sm font-medium">E-Mail-Adresse</label>
                <input id="email" type="email" className="w-full bg-slate-700 text-white rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500 transition" value={email} onChange={e => setEmail(e.target.value)} required />
              </div>
            )}

            <div>
              <label className="text-slate-300 block mb-1 text-sm font-medium">Passwort</label>
              <input id="password" type="password" className="w-full bg-slate-700 text-white rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500 transition" value={password} onChange={e => setPassword(e.target.value)} required />
            </div>
            <button id="signin" type="submit" className={`w-full text-white font-bold p-3 rounded-lg transition duration-200 mt-2 shadow-lg ${isRegistering ? 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-600/20' : 'bg-blue-600 hover:bg-blue-700 shadow-blue-600/20'}`}>
              {isRegistering ? 'Registrieren' : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-center border-t border-slate-700 pt-4">
            <button onClick={() => { setIsRegistering(!isRegistering); setPassword(''); setEmail(''); }} className="text-sm font-medium text-blue-400 hover:text-blue-300 transition underline decoration-dotted">
              {isRegistering ? 'Bereits ein Konto? Hier einloggen' : 'Noch kein Konto? Jetzt registrieren'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // MAIN APPLICATION WORKSPACE
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
              <input id="documentTitle" type="text" placeholder="Dokumententitel..." className="w-full bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500 transition" value={documentTitle} onChange={e => setDocumentTitle(e.target.value)} required />
              <select id="selectedCategory" className="w-full bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500 transition" value={selectedCategory} onChange={e => setSelectedCategory(e.target.value)} required>
                <option value="">Kategorie wählen...</option>
                {categories && categories.length > 0 && categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              <input id="documentFile" type="file" className="w-full text-xs text-slate-500 file:mr-2 file:py-2 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 cursor-pointer" onChange={handleFileChange} required />
              <button id="upload" type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold p-2.5 rounded-lg text-sm transition">Hochladen</button>
            </form>
          </div>

           {/* NEU: Formular zum Erstellen einer Kategorie */}
  <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm mt-4">
    <h3 className="text-base font-bold text-slate-900 mb-4 flex items-center">
      <span className="w-2.5 h-2.5 bg-emerald-600 rounded-full mr-2"></span> Neue Kategorie
    </h3>
    <form onSubmit={handleCreateCategory} className="space-y-3">
      <input 
        id="newcategory"
        type="text" 
        placeholder="Kategoriename (z.B. Rechnungen)..." 
        className="w-full bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500 transition" 
        value={newCategoryName} 
        onChange={e => setNewCategoryName(e.target.value)} 
        required 
      />
      <button id="addcategory" type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold p-2.5 rounded-lg text-sm transition">
        Kategorie erstellen
      </button>
    </form>
  </div>
        </aside>

        <main className="lg:col-span-3 space-y-6">
          <DocumentManager/>
               </main>
      </div>
    </div>
  );
}
