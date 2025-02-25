// app.js (Admin Panel)

// Global references
let ws = null;
let outputArea = null;

// Our data arrays
let estudiantesList = [];
let profesoresList  = [];
let cursosList      = [];
let asignaturasList = [];

// NEW: We'll store the last known "temaList" so we can populate the "contenidoTemaSelect"
let temaList = [];

/**
 * On window load: set up the WebSocket, hide sections except "estudiantes"
 */
window.onload = function() {
  outputArea = document.getElementById("outputArea");
  initWebSocket();

  // Hide all sections except "estudiantes"
  document.querySelectorAll(".admin-section").forEach(sec => sec.style.display = "none");
  showSection("estudiantes");
};

/**
 * Initialize WebSocket connection to the server
 */
function initWebSocket() {
  ws = new WebSocket("ws://localhost:8080");

  ws.onopen = () => {
    logOutput("WebSocket connected.");
    // Load data for dropdowns
    refreshAllDropdowns();
  };

  ws.onmessage = (event) => {
    logOutput("Server says:\n" + event.data);
    handleServerResponse(event.data);
  };

  ws.onclose = () => {
    logOutput("WebSocket disconnected.");
  };
}

/**
 * Toggle the server log panel's visibility
 */
function toggleLog() {
  const panel = document.getElementById("outputPanel");
  panel.classList.toggle("hidden");
}

/**
 * Show only one section (by ID), hide the others
 */
function showSection(name) {
  document.querySelectorAll(".admin-section").forEach(sec => {
    sec.style.display = "none";
  });
  const target = document.getElementById("section-" + name);
  if (target) target.style.display = "block";
}

/**
 * Log messages to the outputArea
 */
function logOutput(msg) {
  if (outputArea) {
    outputArea.textContent += msg + "\n";
    outputArea.scrollTop = outputArea.scrollHeight;
  }
}

/**
 * Parse server responses. If it starts with "Estudiantes:", "Profesores:", etc., we parse them.
 */
function handleServerResponse(data) {
  if (data.startsWith("Estudiantes:")) {
    parseEstudiantesList(data);
  } else if (data.startsWith("Profesores:")) {
    parseProfesoresList(data);
  } else if (data.startsWith("Cursos:")) {
    parseCursosList(data);
  } else if (data.startsWith("Asignaturas:")) {
    parseAsignaturasList(data);
  } else if (data.startsWith("Temas in Asignatura")) {
    parseTemasList(data);  // fill temaList + fix "contenidoTemaSelect"
  } else if (data.startsWith("Contenidos in Tema")) {
    parseContenidosList(data);
  }
  // else, it's likely a response to a create/update/delete or an error.
}

/* ========================================================================
   ESTUDIANTES
========================================================================= */
function createEstudiante() {
  let name = document.getElementById("estudianteName").value.trim();
  let edad = document.getElementById("estudianteEdad").value.trim();

  // Optional username/password
  let user = document.getElementById("estudianteUser").value.trim();
  let pass = document.getElementById("estudiantePass").value.trim();

  if (!name || !edad) {
    alert("Ingresa nombre y edad para el estudiante.");
    return;
  }

  if (user && pass) {
    // 4-arg
    ws.send(`createStudent: ${name},${edad},${user},${pass}`);
  } else {
    // 2-arg
    ws.send(`createStudent: ${name},${edad}`);
  }

  setTimeout(listEstudiantes, 500);
}

function listEstudiantes() {
  ws.send("listStudents");
}

function parseEstudiantesList(rawText) {
  // e.g.
  // Estudiantes:
  //  - John (Edad: 25)
  estudiantesList = [];
  const lines = rawText.split("\n");
  let tableBody = document.querySelector("#estudiantesTable tbody");
  tableBody.innerHTML = "";

  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim(); // e.g. "John (Edad: 25)"
      let match = line.match(/(.+)\(Edad:\s*(\d+)\)/);
      if (match) {
        let nombre = match[1].trim();
        let edad   = match[2].trim();
        estudiantesList.push({ nombre, edad });

        let tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${nombre}</td>
          <td>${edad}</td>
          <td>
            <button class="action-btn" onclick="editEstudiante('${nombre}',${edad})">Edit</button>
            <button class="action-btn" onclick="deleteEstudiante('${nombre}')">Delete</button>
          </td>`;
        tableBody.appendChild(tr);
      }
    }
  }

  populateEstudiantesDropdown(); // For enrollment
}

function editEstudiante(oldName, oldAge) {
  let newName = prompt("Nuevo nombre?", oldName);
  if (!newName) return;
  let newAge = prompt("Nueva edad?", oldAge);
  if (!newAge) return;

  ws.send(`updateEstudiante: ${oldName},${newName},${newAge}`);
  setTimeout(listEstudiantes, 500);
}

function deleteEstudiante(name) {
  if (!confirm("Eliminar estudiante " + name + "?")) return;
  ws.send("deleteEstudiante: " + name);
  setTimeout(listEstudiantes, 500);
}

/* ========================================================================
   PROFESORES
========================================================================= */
function createProfesor() {
  let name = document.getElementById("profesorName").value.trim();
  let esp  = document.getElementById("profesorEsp").value.trim();

  let user = document.getElementById("profesorUser").value.trim();
  let pass = document.getElementById("profesorPass").value.trim();

  if (!name || !esp) {
    alert("Ingresa nombre y especialidad.");
    return;
  }

  if (user && pass) {
    // 4-arg
    ws.send(`createProfesor: ${name},${esp},${user},${pass}`);
  } else {
    // 2-arg
    ws.send(`createProfesor: ${name},${esp}`);
  }

  setTimeout(listProfesores, 500);
}

function listProfesores() {
  ws.send("listProfesores");
}

function parseProfesoresList(rawText) {
  profesoresList = [];
  const lines = rawText.split("\n");
  let tableBody = document.querySelector("#profesoresTable tbody");
  tableBody.innerHTML = "";

  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim(); // e.g. "Alice (Especialidad: Math)"
      let match = line.match(/(.+)\(Especialidad:\s*(.+)\)/);
      if (match) {
        let nombre = match[1].trim();
        let esp    = match[2].replace(")","").trim();
        profesoresList.push({ nombre, esp });

        let tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${nombre}</td>
          <td>${esp}</td>
          <td>
            <button class="action-btn" onclick="editProfesor('${nombre}','${esp}')">Edit</button>
            <button class="action-btn" onclick="deleteProfesor('${nombre}')">Delete</button>
          </td>`;
        tableBody.appendChild(tr);
      }
    }
  }

  populateProfesoresDropdown();
}

function editProfesor(oldName, oldEsp) {
  let newName = prompt("Nuevo nombre?", oldName);
  if (!newName) return;
  let newEsp = prompt("Nueva especialidad?", oldEsp);
  if (!newEsp) return;

  ws.send(`updateProfesor: ${oldName},${newName},${newEsp}`);
  setTimeout(listProfesores, 500);
}

function deleteProfesor(name) {
  if (!confirm("Eliminar profesor " + name + "?")) return;
  ws.send(`deleteProfesor: ${name}`);
  setTimeout(listProfesores, 500);
}

/* ========================================================================
   CURSOS
========================================================================= */
function createCurso() {
  let name = document.getElementById("cursoName").value.trim();
  let dur  = document.getElementById("cursoDuracion").value.trim();
  if (!name || !dur) {
    alert("Ingresa nombre y duracion para el curso");
    return;
  }
  ws.send(`createCurso: ${name},${dur}`);
  setTimeout(listCursos, 500);
}

function listCursos() {
  ws.send("listCursos");
}

function parseCursosList(rawText) {
  cursosList = [];
  const lines = rawText.split("\n");
  let tableBody = document.querySelector("#cursosTable tbody");
  tableBody.innerHTML = "";

  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim();
      // e.g. "Java101 (Duración: 40)"
      let match = line.match(/(.+)\(Duración:\s*(\d+)\)/);
      if (match) {
        let nombre   = match[1].trim();
        let duracion = match[2].trim();
        cursosList.push({ nombre, duracion });

        let tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${nombre}</td>
          <td>${duracion}</td>
          <td>
            <button class="action-btn" onclick="editCurso('${nombre}','${duracion}')">Edit</button>
            <button class="action-btn" onclick="deleteCurso('${nombre}')">Delete</button>
          </td>`;
        tableBody.appendChild(tr);
      }
    }
  }

  populateCursosDropdown();
}

function editCurso(oldName, oldDur) {
  let newName = prompt("Nuevo nombre del curso?", oldName);
  if (!newName) return;
  let newDur = prompt("Nueva duración?", oldDur);
  if (!newDur) return;
  ws.send(`updateCurso: ${oldName},${newName},${newDur}`);
  setTimeout(listCursos, 500);
}

function deleteCurso(name) {
  if (!confirm("Eliminar curso " + name + "?")) return;
  ws.send(`deleteCurso: ${name}`);
  setTimeout(listCursos, 500);
}

/* ========================================================================
   ASIGNATURAS
========================================================================= */
function createAsignatura() {
  let asigName  = document.getElementById("asignaturaName").value.trim();
  let cursoName = document.getElementById("asignaturaCursoSelect").value;
  if (!asigName || !cursoName) {
    alert("Ingresa nombre de asignatura y selecciona un curso");
    return;
  }
  ws.send(`createAsignatura: ${asigName},${cursoName}`);
  setTimeout(listAsignaturas, 500);
}

function listAsignaturas() {
  ws.send("listAsignaturas");
}

function parseAsignaturasList(rawText) {
  asignaturasList = [];
  const lines = rawText.split("\n");
  let tableBody = document.querySelector("#asignaturasTable tbody");
  tableBody.innerHTML = "";

  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim();
      // e.g. "Algebra1 (Curso: Java101)"
      let match = line.match(/(.+)\(Curso:\s*(.+)\)/);
      if (match) {
        let asigName  = match[1].trim();
        let cursoName = match[2].replace(")","").trim();
        asignaturasList.push({ asigName, cursoName });

        let tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${asigName}</td>
          <td>${cursoName}</td>
          <td>
            <button class="action-btn" onclick="editAsignatura('${asigName}','${cursoName}')">Edit</button>
            <button class="action-btn" onclick="deleteAsignatura('${asigName}')">Delete</button>
          </td>`;
        tableBody.appendChild(tr);
      }
    }
  }

  populateAsignaturasDropdown();
}

function editAsignatura(oldAsig, oldCurso) {
  let newAsig = prompt("Nuevo nombre de asignatura?", oldAsig);
  if (!newAsig) return;
  let newCurso = prompt("Nuevo curso para la asignatura?", oldCurso);
  if (!newCurso) return;

  ws.send(`updateAsignatura: ${oldAsig},${newAsig},${newCurso}`);
  setTimeout(listAsignaturas, 500);
}

function deleteAsignatura(name) {
  if (!confirm("Eliminar asignatura " + name + "?")) return;
  ws.send(`deleteAsignatura: ${name}`);
  setTimeout(listAsignaturas, 500);
}

/* ========================================================================
   TEMAS
========================================================================= */
function createTema() {
  let asig = document.getElementById("temaAsignaturaSelect").value;
  let temaName = document.getElementById("temaName").value.trim();
  if (!asig || !temaName) {
    alert("Selecciona asignatura e ingresa nombre del Tema");
    return;
  }
  ws.send(`createTema: ${asig},${temaName}`);
  setTimeout(() => listTemas(asig), 500);
}

function listTemas(asigName) {
  if (!asigName) {
    asigName = document.getElementById("temaAsignaturaSelect").value;
  }
  if (!asigName) {
    alert("Selecciona una Asignatura para listar Temas");
    return;
  }
  ws.send(`listTemas: ${asigName}`);
}

function parseTemasList(rawText) {
  // e.g.
  // Temas in Asignatura 'Algebra1':
  //  - Introduccion
  temaList = []; // important: store so we can populate the "contenidoTemaSelect"

  let lines = rawText.split("\n");
  let match = lines[0].match(/Temas in Asignatura '(.+)'/);
  let asigName = (match && match[1]) ? match[1] : "";

  let tableBody = document.querySelector("#temasTable tbody");
  tableBody.innerHTML = "";

  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      let temaName = line.substring(2).trim();
      temaList.push({ temaName, asigName }); // store it

      let tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${temaName}</td>
        <td>${asigName}</td>
        <td>
          <button class="action-btn" onclick="editTema('${asigName}','${temaName}')">Edit</button>
          <button class="action-btn" onclick="deleteTema('${asigName}','${temaName}')">Delete</button>
        </td>`;
      tableBody.appendChild(tr);
    }
  }

  // FIX: now populate "contenidoTemaSelect"
  populateTemaDropdown();
}

function editTema(asigName, oldTema) {
  let newTema = prompt("Nuevo nombre del tema?", oldTema);
  if (!newTema) return;
  ws.send(`updateTema: ${asigName},${oldTema},${newTema}`);
  setTimeout(() => listTemas(asigName), 500);
}

function deleteTema(asigName, temaName) {
  if (!confirm(`Eliminar tema '${temaName}' en '${asigName}'?`)) return;
  ws.send(`deleteTema: ${asigName},${temaName}`);
  setTimeout(() => listTemas(asigName), 500);
}

/* ========================================================================
   CONTENIDOS
========================================================================= */
function createContenido() {
  let asig = document.getElementById("contenidoAsigSelect").value;
  let tema = document.getElementById("contenidoTemaSelect").value;
  let titulo = document.getElementById("contenidoTitulo").value.trim();
  let texto  = document.getElementById("contenidoTexto").value.trim();

  if (!asig || !tema || !titulo) {
    alert("Selecciona Asignatura y Tema, e ingresa un Título");
    return;
  }
  ws.send(`createContenido: ${asig},${tema},${titulo},${texto}`);
  setTimeout(() => listContenidos(asig, tema), 500);
}

function listContenidos(asigName, temaName) {
  if (!asigName) asigName = document.getElementById("contenidoAsigSelect").value;
  if (!temaName) temaName = document.getElementById("contenidoTemaSelect").value;
  if (!asigName || !temaName) {
    alert("Selecciona asignatura y tema");
    return;
  }
  ws.send(`listContenidos: ${asigName},${temaName}`);
}

function parseContenidosList(rawText) {
  // e.g. 
  // Contenidos in Tema 'Intro' of Asignatura 'Algebra1':
  //  - [Titulo] Texto
  let lines = rawText.split("\n");
  let match = lines[0].match(/Contenidos in Tema '(.+)' of Asignatura '(.+)'/);
  let temaName = "";
  let asigName = "";
  if (match) {
    temaName = match[1];
    asigName = match[2];
  }

  let tableBody = document.querySelector("#contenidosTable tbody");
  tableBody.innerHTML = "";

  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- [")) {
      line = line.substring(2).trim(); // remove "- "
      let bracketPos = line.indexOf("]");
      if (bracketPos < 0) continue;
      let titulo = line.substring(1, bracketPos); 
      let texto  = line.substring(bracketPos+1).trim();

      let tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${titulo}</td>
        <td>${texto}</td>
        <td>${temaName}</td>
        <td>${asigName}</td>
        <td>
          <button class="action-btn" onclick="editContenido('${asigName}','${temaName}','${titulo}','${texto}')">Edit</button>
          <button class="action-btn" onclick="deleteContenido('${asigName}','${temaName}','${titulo}')">Delete</button>
        </td>`;
      tableBody.appendChild(tr);
    }
  }
}

function editContenido(asig, tema, oldTitulo, oldTexto) {
  let newTitulo = prompt("Nuevo título?", oldTitulo);
  if (!newTitulo) return;
  let newTexto = prompt("Nuevo texto?", oldTexto);
  if (newTexto === null) return;

  ws.send(`updateContenido: ${asig},${tema},${oldTitulo},${newTitulo},${newTexto}`);
  setTimeout(() => listContenidos(asig, tema), 500);
}

function deleteContenido(asig, tema, titulo) {
  if (!confirm(`Eliminar contenido '${titulo}' en tema '${tema}'?`)) return;
  ws.send(`deleteContenido: ${asig},${tema},${titulo}`);
  setTimeout(() => listContenidos(asig, tema), 500);
}

/* ========================================================================
   ENROLL (Estudiantes / Profesores)
========================================================================= */
function enrollEstudiante() {
  let asigName = document.getElementById("enrollAsignaturaSelectEst").value;
  let estName  = document.getElementById("enrollEstudianteSelect").value;
  if (!asigName || !estName) {
    alert("Selecciona asignatura y estudiante");
    return;
  }
  ws.send(`enrollEstudiante: ${asigName},${estName}`);
}

function enrollProfesor() {
  let asigName = document.getElementById("enrollAsignaturaSelectProf").value;
  let profName = document.getElementById("enrollProfesorSelect").value;
  if (!asigName || !profName) {
    alert("Selecciona asignatura y profesor");
    return;
  }
  ws.send(`enrollProfesor: ${asigName},${profName}`);
}

/* ========================================================================
   DROPDOWN population
========================================================================= */
function refreshAllDropdowns() {
  // fetch everything
  listEstudiantes();
  listProfesores();
  listCursos();
  listAsignaturas();
}

// after parse
function populateEstudiantesDropdown() {
  let estSel = document.getElementById("enrollEstudianteSelect");
  estSel.innerHTML = "";
  estudiantesList.forEach(e => {
    let opt = document.createElement("option");
    opt.value = e.nombre;
    opt.textContent = e.nombre;
    estSel.appendChild(opt);
  });
}

function populateProfesoresDropdown() {
  let profSel = document.getElementById("enrollProfesorSelect");
  profSel.innerHTML = "";
  profesoresList.forEach(p => {
    let opt = document.createElement("option");
    opt.value = p.nombre;
    opt.textContent = p.nombre;
    profSel.appendChild(opt);
  });
}

function populateCursosDropdown() {
  let sel = document.getElementById("asignaturaCursoSelect");
  sel.innerHTML = "";
  cursosList.forEach(c => {
    let opt = document.createElement("option");
    opt.value = c.nombre;
    opt.textContent = c.nombre;
    sel.appendChild(opt);
  });
}

function populateAsignaturasDropdown() {
  let temaAsigSel = document.getElementById("temaAsignaturaSelect");
  temaAsigSel.innerHTML = "";

  let enrollEstAsigSel = document.getElementById("enrollAsignaturaSelectEst");
  enrollEstAsigSel.innerHTML = "";

  let enrollProfAsigSel = document.getElementById("enrollAsignaturaSelectProf");
  enrollProfAsigSel.innerHTML = "";

  let contAsigSel = document.getElementById("contenidoAsigSelect");
  contAsigSel.innerHTML = "";

  asignaturasList.forEach(a => {
    let optTema = document.createElement("option");
    optTema.value = a.asigName;
    optTema.textContent = a.asigName;
    temaAsigSel.appendChild(optTema.cloneNode(true));

    let optEst = document.createElement("option");
    optEst.value = a.asigName;
    optEst.textContent = a.asigName;
    enrollEstAsigSel.appendChild(optEst.cloneNode(true));

    let optProf = document.createElement("option");
    optProf.value = a.asigName;
    optProf.textContent = a.asigName;
    enrollProfAsigSel.appendChild(optProf.cloneNode(true));

    let optCont = document.createElement("option");
    optCont.value = a.asigName;
    optCont.textContent = a.asigName;
    contAsigSel.appendChild(optCont.cloneNode(true));
  });
}

// Populate "contenidoTemaSelect" from the global "temaList"
function populateTemaDropdown() {
  let sel = document.getElementById("contenidoTemaSelect");
  sel.innerHTML = "";

  temaList.forEach(t => {
    let opt = document.createElement("option");
    opt.value = t.temaName;
    opt.textContent = t.temaName + " (" + t.asigName + ")";
    sel.appendChild(opt);
  });
}
