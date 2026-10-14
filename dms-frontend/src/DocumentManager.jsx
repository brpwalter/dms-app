import React, { useState, useEffect } from 'react';
import axios from 'axios';

function DocumentManager() {
  // Wichtig: Initialisiere immer mit einem leeren Array
  const [categories, setCategories] = useState([]);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [file, setFile] = useState(null);
  const [documents, setDocuments] = useState([]);

    // States für Suche und Filter
  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('');

  useEffect(() => {
    fetchCategories();
    fetchDocuments();
  }, []);

  // Wird aufgerufen, wenn das Suchformular abgeschickt wird
  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchDocuments(searchQuery); // Suche im Backend ausführen
  };

  
 const fetchDocuments = async (query = "") => {
    try {
      // Wenn ein Suchbegriff existiert, nutzen wir den neuen /search Endpunkt
      const url = query 
        ? `http://localhost:8080/api/documents/search?q=${encodeURIComponent(query)}`
        : 'http://localhost:8080/api/documents';
        
      const response = await fetch(url);
      const data = await response.json();
      setDocuments(data);
    } catch (error) {
      console.error("Fehler beim Laden/Suchen:", error);
    }
  };

   // Datei-Upload (Erstellt ein Hauptdokument)
  const handleFileUpload = async (event) => {
    // WICHTIG: [0] hinzugefügt, um die eigentliche Datei zu greifen!
    const file = event.target.files[0]; 
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file); 
    formData.append("title", file.name);
    if (selectedCategory) formData.append("categoryId", selectedCategory); 

    try {
      await axios.post("http://localhost:8080/api/documents", formData);
      alert("Hauptdokument hochgeladen!");
      fetchDocuments(searchQuery); // Behalte aktuellen Suchfilter bei
      event.target.value = null; // Input zurücksetzen
    } catch (error) {
      console.error("Fehler beim Upload", error);
    }
  };

   const fetchCategories = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/categories');
      if (Array.isArray(response.data)) setCategories(response.data);
    } catch (error) {
      console.error("Fehler beim Laden der Kategorien", error);
    }
  };

   // NEUE VERSION HOCHLADEN (Kopplung an ein existierendes Dokument)
  const handleUploadNewVersion = async (event, parentId, categoryId) => {
    // WICHTIG: [0] hinzugefügt, um die eigentliche Datei zu greifen!
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);
    formData.append("title", file.name);
    formData.append("parentDocumentId", parentId); 
    if (categoryId) formData.append("categoryId", categoryId);

    try {
      await axios.post("http://localhost:8080/api/documents/version", formData);
      alert("Neue Version erfolgreich hinzugefügt!");
      fetchDocuments(searchQuery);
      event.target.value = null;
    } catch (error) {
      console.error("Fehler beim Hochladen der neuen Version", error);
    }
  };
  // AKTIONEN: Preview, Download, Delete, Move
  const handlePreview = (docId) => {
    window.open(`http://localhost:8080/api/documents/preview/${docId}`, '_blank');
  };

  const handleDownload = (docId, fileName) => {
    // Öffnet den Download-Link direkt im Browser
    window.location.href = `http://localhost:8080/api/documents/download/${docId}`;
  };

    const handleDelete = async (docId) => {
    if (window.confirm("Möchtest du dieses Dokument (und ggf. alle Unterversionen) wirklich löschen?")) {
      try {
        await axios.delete(`http://localhost:8080/api/documents/${docId}`);
        fetchDocuments();
      } catch (error) {
        console.error("Fehler beim Löschen", error);
      }
    }
  };

  const handleMove = async (docId) => {
    const targetCatId = prompt("Gib die Ziel-Kategorie-ID ein (oder leer lassen für keine):");
    if (targetCatId === null) return; // Abbrechen gedrückt
    
    try {
      await axios.put(`http://localhost:8080/api/documents/${docId}/move`, null, {
        params: { categoryId: targetCatId || null }
      });
      fetchDocuments();
    } catch (error) {
      console.error("Fehler beim Verschieben", error);
    }
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();
    if (!newCategoryName.trim()) return;
    try {
      const response = await axios.post('http://localhost:8080/api/categories', { name: newCategoryName });
      setCategories((prev) => [...prev, response.data]);
      setNewCategoryName('');
    } catch (error) {
      console.error("Fehler beim Erstellen der Kategorie", error);
    }
  };

      const handleDeleteCategory = async (categoryId) => {
    if (window.confirm("Möchtest du diese Kategorie wirklich löschen?")) {
      try {
        await axios.delete(`http://localhost:8080/api/categories/${categoryId}`);
        setCategories((prev) => prev.filter((cat) => cat.id !== categoryId));
      } catch (error) {
        console.error("Fehler beim Löschen", error);
      }
    }
  };


    // HIERARCHIE-LOGIK: Dokumente filtern und sortieren
  // 1. Filtern nach Suche und ausgewählter Kategorie im Suchbereich
  const filteredDocs = documents.filter(doc => {
    const matchesSearch = doc.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = filterCategory === '' || String(doc.categoryId) === String(filterCategory);
    return matchesSearch && matchesCategory;
  });

  // 2. Hauptdokumente (ohne Parent) trennen von den Versionen (mit Parent)
  const rootDocuments = filteredDocs.filter(doc => !doc.parentDocumentId);
  const versionDocuments = documents; // Alle Versionen im System vorhalten für die Zuordnung

  // Rekursive Render-Funktion für die Hierarchie (Einrücken der Versionen)
  const renderDocumentRow = (doc, isVersion = false) => {
    // Finde alle Unterversionen, die dieses Dokument als Parent haben
    const childVersions = versionDocuments.filter(v => v.parentDocumentId === doc.id);


 return (
      <div key={doc.id} style={{ marginLeft: isVersion ? '30px' : '0px', marginTop: '5px' }}>
        <div style={{
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          padding: '10px', 
          backgroundColor: isVersion ? '#f9f9f9' : '#fff',
          border: '1px solid #eee',
          borderRadius: '4px',
          borderLeft: isVersion ? '4px solid #007bff' : '1px solid #eee'
        }}>
          <div>
            {isVersion ? '↳ ' : '📄 '} 
            <strong>{doc.title || doc.fileName}</strong> 
            <span style={{ fontSize: '11px', color: '#777', marginLeft: '8px' }}>
              (Ver. {doc.version || '1'})
            </span>
          </div>

          {/* BUTTON-LEISTE */}
          <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
            <button id="preview" onClick={() => handlePreview(doc.id)} style={{ fontSize: '12px' }}>Preview</button>
            <button id="download" onClick={() => handleDownload(doc.id, doc.title)} style={{ fontSize: '12px' }}>Download</button>
            <button id="move" onClick={() => handleMove(doc.id)} style={{ fontSize: '12px' }}>Move</button>
            <button id="delete" onClick={() => handleDelete(doc.id)} style={{ fontSize: '12px', color: 'red' }}>Delete</button>

            {/* Nur bei Hauptdokumenten: Button um eine neue Version hochzuladen */}
            {!isVersion && (
              <label style={{
                fontSize: '12px', backgroundColor: '#e2e8f0', padding: '2px 6px', 
                borderRadius: '3px', cursor: 'pointer', border: '1px solid #cbd5e1'
              }}>
                + Neue Version
                    <input 
                  type="file" 
                  style={{ display: 'none' }} 
                  onChange={(e) => handleUploadNewVersion(e, doc.id, doc.categoryId)} 
                />
              </label>
            )}
          </div>
        </div>

        {/* Wenn Unterversionen existieren, diese rekursiv rendern */}
        {childVersions.map(child => renderDocumentRow(child, true))}
      </div>
    );
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '900px', margin: '0 auto' }}>
       <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h1>Dokumentenmanagement mit Volltextsuche</h1>
      
    
      {/* NEU: Suchformular für die Volltextsuche */}
      <form onSubmit={handleSearchSubmit} style={{ marginBottom: '20px' }}>
        <input 
          type="text" 
          placeholder="Inhalt oder Titel suchen..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{ width: '300px', padding: '8px', marginRight: '10px' }}
        />
        <button type="submit" style={{ padding: '8px 15px' }}>Suchen</button>
        {searchQuery && (
          <button 
            type="button" 
            onClick={() => { setSearchQuery(""); fetchDocuments(""); }} 
            style={{ marginLeft: '5px', padding: '8px' }}
          >
            Zurücksetzen
          </button>
        )}
      </form>

        {/* Dokumentenliste */}
      <h2>Suchergebnisse ({documents.length})</h2>
      <table border="1" cellPadding="10" style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f2f2f2' }}>
            <th>ID</th>
            <th>Name</th>
            <th>Typ</th>
            <th>Größe (Bytes)</th>
            <th>Hochgeladen am</th>
          </tr>
        </thead>
        <tbody>
          {documents.length > 0 ? (
            documents.map((doc) => (
              <tr key={doc.id}>
                <td>{doc.id}</td>
                <td>{doc.fileName}</td>
                <td>{doc.fileType}</td>
                <td>{doc.fileSize}</td>
                <td>{new Date(doc.uploadTime).toLocaleString()}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="5" style={{ textAlign: 'center' }}>Keine Dokumente gefunden, die zum Suchbegriff passen.</td>
            </tr>
          )}
        </tbody>
      </table>
      <br/>
  <br/>
    <br/>

      {/* 1. DOKUMENT-SUCHE & FILTER (OBEN) */}
      <div style={{ padding: '15px', border: '1px solid #ccc', borderRadius: '6px', marginBottom: '20px', backgroundColor: '#f8fafc' }}>
        <h4>🔍 Dokumenten-Schnellsuche</h4>
        <div style={{ display: 'flex', gap: '15px' }}>
          <input 
            type="text" 
            placeholder="Nach Dateiname / Titel suchen..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ flex: 2, padding: '6px' }}
          />
          <select 
            value={filterCategory} 
            onChange={(e) => setFilterCategory(e.target.value)}
            style={{ flex: 1, padding: '6px' }}
             />
          <select 
            value={filterCategory} 
            onChange={(e) => setFilterCategory(e.target.value)}
            style={{ flex: 1, padding: '6px' }}
          >
            <option value="">Alle Kategorien</option>
            {categories.map(cat => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </div>
      </div>


        {/* RECHTS: KATEGORIEN-ANSICHT MIT DOKUMENTEN-HIERARCHIE */}
        <div style={{ flex: 2, border: '1px solid #ccc', borderRadius: '6px', padding: '15px' }}>
          <h3 style={{ marginTop: 0 }}>📂 Dokumenten-Explorer (nach Kategorien sortiert)</h3>
          {categories.map(category => {
            // Filtert alle Dokumente, die zu dieser spezifischen Kategorie gehören
            const docsInCat = rootDocuments.filter(d => String(d.categoryId) === String(category.id));

            return (
              <div key={category.id} style={{ marginBottom: '25px', borderBottom: '2px dashed #eee', paddingBottom: '15px' }}>
                <h4 style={{ color: '#1e293b', margin: '5px 0', backgroundColor: '#e2e8f0', padding: '6px 12px', borderRadius: '4px' }}>
                  📁 {category.name} <span style={{ fontSize: '12px', fontWeight: 'normal', color: '#64748b' }}>(ID: {category.id})</span>
                </h4>
                 <button onClick={() => handleDeleteCategory(category.id)} style={{ fontSize: '12px', color: 'red' }}>Delete</button>
                <div style={{ marginTop: '10px' }}>
                  {docsInCat.length === 0 ? (
                    <p style={{ fontStyle: 'italic', color: '#94a3b8', fontSize: '13px', marginLeft: '10px' }}>Keine Hauptdokumente in dieser Kategorie.</p>
                  ) : (
                    docsInCat.map(doc => renderDocumentRow(doc, false))
                  )}
                </div>
              </div>
            );
          })}

          {/* Sektion für Dokumente komplett ohne Kategorie */}
          <div style={{ marginTop: '20px' }}>
            <h4 style={{ color: '#475569', margin: '5px 0', backgroundColor: '#f1f5f9', padding: '6px 12px', borderRadius: '4px' }}>
              📂 Ohne Kategorie assigned
            </h4>

            <div style={{ marginTop: '10px' }}>
              {rootDocuments.filter(d => !d.categoryId).length === 0 ? (
                <p style={{ fontStyle: 'italic', color: '#94a3b8', fontSize: '13px', marginLeft: '10px' }}>Alles einsortiert!</p>
              ) : (
                rootDocuments.filter(d => !d.categoryId).map(doc => renderDocumentRow(doc, false))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
    
  );
};

export default DocumentManager;