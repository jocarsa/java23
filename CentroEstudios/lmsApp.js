// lmsApp.js

let wsLms;
let lmsLog;
let currentUser = null;  // { role: "student"|"teacher", username, name }

// We'll store the user's courses/asignaturas in arrays, so we can build the nav
let myCourses   = []; // array of { nombre, duracion } for a student
let myAsigSt    = []; // array of { asigName, cursoName } for a student
let myAsigTeach = []; // array of { asigName, cursoName } for a teacher

window.onload = function() {
  lmsLog = document.getElementById("lmsLog");
  initWebSocketLms();

  // Show login panel, hide dashboard
  document.getElementById("loginPanel").classList.remove("hidden");
  document.getElementById("dashboardPanel").classList.add("hidden");
};

function initWebSocketLms() {
  wsLms = new WebSocket("ws://localhost:8080");

  wsLms.onopen = () => {
    logLms("LMS connected to server.");
  };

  wsLms.onmessage = (event) => {
    logLms("Server: " + event.data);
    handleLmsResponse(event.data);
  };

  wsLms.onclose = () => {
    logLms("LMS disconnected from server.");
  };
}

function logLms(msg) {
  lmsLog.textContent += msg + "\n";
  lmsLog.scrollTop = lmsLog.scrollHeight;
}

function attemptLogin() {
  let role = document.getElementById("loginRole").value;
  let username = document.getElementById("loginUsername").value.trim();
  let password = document.getElementById("loginPassword").value.trim();

  if (!username || !password) {
    alert("Enter username/password");
    return;
  }

  if (role === "student") {
    wsLms.send(`loginStudent: ${username},${password}`);
  } else {
    wsLms.send(`loginTeacher: ${username},${password}`);
  }
}

function handleLmsResponse(data) {
  // Check login
  if (data.startsWith("OK: Student ")) {
    let name = data.substring("OK: Student ".length).trim();
    currentUser = { role: "student", username: document.getElementById("loginUsername").value, name };
    showDashboard();
    // After login, fetch my courses & asignaturas
    wsLms.send(`myCoursesStudent: ${currentUser.username}`);
    wsLms.send(`myAsignaturasStudent: ${currentUser.username}`);
  }
  else if (data.startsWith("OK: Teacher ")) {
    let name = data.substring("OK: Teacher ".length).trim();
    currentUser = { role: "teacher", username: document.getElementById("loginUsername").value, name };
    showDashboard();
    // After login, fetch my asignaturas
    wsLms.send(`myAsignaturasTeacher: ${currentUser.username}`);
  }
  else if (data.startsWith("ERROR: Invalid student credentials") ||
           data.startsWith("ERROR: Invalid teacher credentials")) {
    alert("Invalid credentials");
  }
  // My courses (student)
  else if (data.startsWith("MyCourses:")) {
    parseMyCourses(data);
    renderLeftNav();
  }
  else if (data.startsWith("No courses found for student")) {
    myCourses = [];
    renderLeftNav();
  }
  // My Asignaturas (Student)
  else if (data.startsWith("MyAsignaturas (Student):")) {
    parseMyAsignaturasStudent(data);
    renderLeftNav();
  }
  else if (data.startsWith("No asignaturas found for student")) {
    myAsigSt = [];
    renderLeftNav();
  }
  // My Asignaturas (Teacher)
  else if (data.startsWith("MyAsignaturas (Teacher):")) {
    parseMyAsignaturasTeacher(data);
    renderLeftNav();
  }
  else if (data.startsWith("No asignaturas found for teacher")) {
    myAsigTeach = [];
    renderLeftNav();
  }
  // ListTemas response
  else if (data.startsWith("Temas in Asignatura")) {
    renderTemasAndContenidos(data);
  }
  // Possibly "No temas found" or "No contenidos found" but let's handle in `renderTemasAndContenidos()`.
}

/**
 * Show the dashboard panel, hide login
 */
function showDashboard() {
  document.getElementById("loginPanel").classList.add("hidden");
  document.getElementById("dashboardPanel").classList.remove("hidden");

  document.getElementById("dashboardUserName").textContent = currentUser.name;
  document.getElementById("dashboardUserRole").textContent = "Role: " + currentUser.role;
}

/**
 * Logout
 */
function logout() {
  currentUser = null;
  myCourses   = [];
  myAsigSt    = [];
  myAsigTeach = [];
  document.getElementById("loginPanel").classList.remove("hidden");
  document.getElementById("dashboardPanel").classList.add("hidden");
  document.getElementById("loginUsername").value = "";
  document.getElementById("loginPassword").value = "";
  document.getElementById("lmsNav").innerHTML    = "";
  document.getElementById("lmsDisplayArea").innerHTML = "";
}

/**
 * Parse "MyCourses:\n - CourseName (Dur: X)\n..."
 */
function parseMyCourses(rawText) {
  // e.g.
  // MyCourses:
  //  - Java101 (Dur: 40)
  myCourses = [];
  let lines = rawText.split("\n");
  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim();
      let match = line.match(/(.+)\(Dur:\s*(\d+)\)/);
      if (match) {
        let nombre = match[1].trim();
        let dur    = match[2].trim();
        myCourses.push({ nombre, dur });
      }
    }
  }
}

/**
 * Parse "MyAsignaturas (Student):\n - Algebra1 (Curso: Java101)\n..."
 */
function parseMyAsignaturasStudent(rawText) {
  myAsigSt = [];
  let lines = rawText.split("\n");
  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim(); 
      let match = line.match(/(.+)\(Curso:\s*(.+)\)/);
      if (match) {
        let asigName  = match[1].trim();
        let cursoName = match[2].replace(")","").trim();
        myAsigSt.push({ asigName, cursoName });
      }
    }
  }
}

/**
 * Parse "MyAsignaturas (Teacher):\n - Algebra1 (Curso: Java101)\n..."
 */
function parseMyAsignaturasTeacher(rawText) {
  myAsigTeach = [];
  let lines = rawText.split("\n");
  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      line = line.substring(2).trim();
      let match = line.match(/(.+)\(Curso:\s*(.+)\)/);
      if (match) {
        let asigName  = match[1].trim();
        let cursoName = match[2].replace(")","").trim();
        myAsigTeach.push({ asigName, cursoName });
      }
    }
  }
}

/**
 * Render the left nav with MyCourses (for students) or MyAsignaturas
 */
function renderLeftNav() {
  let nav = document.getElementById("lmsNav");
  nav.innerHTML = "";

  // If no user, or role unknown
  if (!currentUser) return;

  // Student
  if (currentUser.role === "student") {
    // Show My Courses
    if (myCourses.length > 0) {
      let h3 = document.createElement("h3");
      h3.textContent = "My Courses";
      nav.appendChild(h3);

      let ul = document.createElement("ul");
      myCourses.forEach(c => {
        let li = document.createElement("li");
        li.textContent = `${c.nombre} (Dur: ${c.dur})`;
        ul.appendChild(li);
      });
      nav.appendChild(ul);
    }

    // Show Asignaturas
    if (myAsigSt.length > 0) {
      let h3a = document.createElement("h3");
      h3a.textContent = "Mis Asignaturas";
      nav.appendChild(h3a);

      let ulA = document.createElement("ul");
      myAsigSt.forEach(a => {
        let li = document.createElement("li");
        // On click, fetch/listTemas
        let link = document.createElement("a");
        link.textContent = `${a.asigName} (${a.cursoName})`;
        link.onclick = () => loadTemas(a.asigName);
        li.appendChild(link);
        ulA.appendChild(li);
      });
      nav.appendChild(ulA);
    }

  } 
  // Teacher
  else if (currentUser.role === "teacher") {
    if (myAsigTeach.length > 0) {
      let h3a = document.createElement("h3");
      h3a.textContent = "Mis Asignaturas (Teacher)";
      nav.appendChild(h3a);

      let ulA = document.createElement("ul");
      myAsigTeach.forEach(a => {
        let li = document.createElement("li");
        let link = document.createElement("a");
        link.textContent = `${a.asigName} (${a.cursoName})`;
        link.onclick = () => loadTemas(a.asigName);
        li.appendChild(link);
        ulA.appendChild(li);
      });
      nav.appendChild(ulA);
    }
  }
}

/**
 * Load (listTemas) for a given Asignatura
 */
function loadTemas(asigName) {
  wsLms.send(`listTemas: ${asigName}`);
}

/**
 * Render Temas and Contenidos in the main area
 * We'll parse the server data: "Temas in Asignatura 'X':\n - Tema1..."
 * Then for each tema, do "listContenidos: X, temaName" and render them
 */
function renderTemasAndContenidos(rawText) {
  let display = document.getElementById("lmsDisplayArea");
  display.innerHTML = "";

  let lines = rawText.split("\n");
  let m = lines[0].match(/Temas in Asignatura '(.+)'/);
  let asigName = (m && m[1]) ? m[1] : null;
  if (!asigName) {
    display.innerHTML = "<p>No Temas found.</p>";
    return;
  }

  let temas = [];
  for (let i=1; i<lines.length; i++) {
    let line = lines[i].trim();
    if (line.startsWith("- ")) {
      let temaName = line.substring(2).trim();
      temas.push(temaName);
    }
  }

  if (temas.length === 0) {
    display.innerHTML = `<p>No Temas found for Asignatura '${asigName}'.</p>`;
    return;
  }

  // For each tema, we want to get Contenidos
  // We'll do them sequentially for simplicity
  showTemasWithContenidos(asigName, temas, 0, display);
}

/**
 * Show each Tema with its Contenidos as articles.
 * We'll do a small function that calls "listContenidos: asig,tema"
 * and appends them to the page, then move to next tema.
 */
function showTemasWithContenidos(asigName, temas, index, display) {
  if (index >= temas.length) return; // done

  let temaName = temas[index];
  // create a section for this tema
  let temaSection = document.createElement("div");
  temaSection.classList.add("lms-section");
  let h4 = document.createElement("h4");
  h4.textContent = "Tema: " + temaName;
  temaSection.appendChild(h4);

  display.appendChild(temaSection);

  // Now fetch contenidos from server
  fetchContenidos(asigName, temaName, temaSection, () => {
    // once done, go to the next tema
    showTemasWithContenidos(asigName, temas, index+1, display);
  });
}

/**
 * Helper to call "listContenidos: asigName,temaName" 
 * Then append articles to the given temaSection.
 */
function fetchContenidos(asigName, temaName, temaSection, callback) {
  // We'll do a temporary listener approach to handle a single response
  let tempHandler = function(event) {
    let data = event.data;
    if (data.startsWith("Contenidos in Tema '" + temaName + "' of Asignatura '" + asigName + "':")) {
      // parse them
      wsLms.removeEventListener("message", tempHandler);

      let lines = data.split("\n");
      for (let i=1; i<lines.length; i++) {
        let line = lines[i].trim();
        if (line.startsWith("- [")) {
          // e.g. "- [Titulo] Texto"
          line = line.substring(2).trim(); 
          let bracketPos = line.indexOf("]");
          if (bracketPos > 0) {
            let titulo = line.substring(1, bracketPos);
            let texto  = line.substring(bracketPos+1).trim();

            let art = document.createElement("div");
            art.classList.add("lms-article");
            art.innerHTML = `<strong>${titulo}:</strong> ${texto}`;
            temaSection.appendChild(art);
          }
        }
      }
      callback();
    } 
    else if (data.startsWith("No contenidos found for Tema '" + temaName + "'")) {
      // no contenidos 
      wsLms.removeEventListener("message", tempHandler);

      let art = document.createElement("div");
      art.classList.add("lms-article");
      art.textContent = "No contenidos for this Tema.";
      temaSection.appendChild(art);

      callback();
    }
    // otherwise ignore
  };

  wsLms.addEventListener("message", tempHandler);
  wsLms.send(`listContenidos: ${asigName},${temaName}`);
}
