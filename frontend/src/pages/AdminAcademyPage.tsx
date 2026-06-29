import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";

interface Course {
  id: string;
  title: string;
  description: string;
  category: string;
  level: string;
  xpReward: number;
  published: boolean;
}

interface Lesson {
  id: string;
  title: string;
  summary: string;
  published: boolean;
  estimatedMinutes: number;
  sortOrder: number;
}

const CATEGORIES = ["CS2", "LOL", "F1"];
const LEVELS = ["BEGINNER", "ADVANCED", "EXPERT"];

export default function AdminAcademyPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const [newCourse, setNewCourse] = useState({
    title: "", description: "", category: "CS2", level: "BEGINNER", xpReward: 200, published: false
  });

  const [generateTopic, setGenerateTopic] = useState("");
  const [generating, setGenerating] = useState(false);

  useEffect(() => { fetchCourses(); }, []);

  const fetchCourses = async () => {
    const res = await fetch("/api/admin/academy/courses", { credentials: "include" });
    const data = await res.json();
    setCourses(data);
  };

  const fetchLessons = async (courseId: string) => {
    const res = await fetch(`/api/admin/academy/courses/${courseId}/lessons`, { credentials: "include" });
    const data = await res.json();
    setLessons(data);
  };

  const createCourse = async () => {
    setLoading(true);
    const res = await fetch("/api/admin/academy/courses", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(newCourse)
    });
    if (res.ok) {
      setMessage("Kurz vytvorený!");
      setNewCourse({ title: "", description: "", category: "CS2", level: "BEGINNER", xpReward: 200, published: false });
      fetchCourses();
    }
    setLoading(false);
  };

  const selectCourse = (course: Course) => {
    setSelectedCourse(course);
    fetchLessons(course.id);
  };

  const generateLesson = async () => {
    if (!selectedCourse || !generateTopic.trim()) return;
    setGenerating(true);
    setMessage("Generujem lekciu pomocou AI...");
    const res = await fetch(`/api/admin/academy/courses/${selectedCourse.id}/lessons/generate`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ topic: generateTopic, sortOrder: lessons.length })
    });
    if (res.ok) {
      setMessage("Lekcia vygenerovaná! Skontroluj a publikuj ju.");
      setGenerateTopic("");
      fetchLessons(selectedCourse.id);
    } else {
      setMessage("Chyba pri generovaní lekcie.");
    }
    setGenerating(false);
  };

  const publishLesson = async (lessonId: string) => {
    await fetch(`/api/admin/academy/lessons/${lessonId}/publish`, {
      method: "PATCH",
      credentials: "include"
    });
    if (selectedCourse) fetchLessons(selectedCourse.id);
  };

  const deleteLesson = async (lessonId: string) => {
    await fetch(`/api/admin/academy/lessons/${lessonId}`, {
      method: "DELETE",
      credentials: "include"
    });
    if (selectedCourse) fetchLessons(selectedCourse.id);
  };

  const deleteCourse = async (courseId: string) => {
    await fetch(`/api/admin/academy/courses/${courseId}`, {
      method: "DELETE",
      credentials: "include"
    });
    setSelectedCourse(null);
    setLessons([]);
    fetchCourses();
  };

  return (
    <div style={{ padding: "2rem", color: "white" }}>
      <h1 style={{ fontSize: "1.5rem", marginBottom: "1.5rem" }}>🎓 Predicto Academy — Admin</h1>

      {message && (
        <div style={{ background: "#1a3a2a", border: "1px solid #4ade80", borderRadius: 8, padding: "0.75rem 1rem", marginBottom: "1rem", color: "#4ade80" }}>
          {message}
        </div>
      )}

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "2rem" }}>
        {/* Left: Courses */}
        <div>
          <h2 style={{ fontSize: "1.1rem", marginBottom: "1rem", color: "#a78bfa" }}>Kurzy</h2>

          {/* Create course form */}
          <div style={{ background: "#1e1e2e", borderRadius: 8, padding: "1rem", marginBottom: "1rem" }}>
            <h3 style={{ marginBottom: "0.75rem", fontSize: "0.9rem", color: "#94a3b8" }}>Nový kurz</h3>
            <input
              placeholder="Názov kurzu"
              value={newCourse.title}
              onChange={e => setNewCourse({ ...newCourse, title: e.target.value })}
              style={{ width: "100%", marginBottom: "0.5rem", padding: "0.5rem", borderRadius: 6, background: "#0f0f1a", border: "1px solid #334155", color: "white" }}
            />
            <input
              placeholder="Popis"
              value={newCourse.description}
              onChange={e => setNewCourse({ ...newCourse, description: e.target.value })}
              style={{ width: "100%", marginBottom: "0.5rem", padding: "0.5rem", borderRadius: 6, background: "#0f0f1a", border: "1px solid #334155", color: "white" }}
            />
            <div style={{ display: "flex", gap: "0.5rem", marginBottom: "0.5rem" }}>
              <select value={newCourse.category} onChange={e => setNewCourse({ ...newCourse, category: e.target.value })}
                style={{ flex: 1, padding: "0.5rem", borderRadius: 6, background: "#0f0f1a", border: "1px solid #334155", color: "white" }}>
                {CATEGORIES.map(c => <option key={c}>{c}</option>)}
              </select>
              <select value={newCourse.level} onChange={e => setNewCourse({ ...newCourse, level: e.target.value })}
                style={{ flex: 1, padding: "0.5rem", borderRadius: 6, background: "#0f0f1a", border: "1px solid #334155", color: "white" }}>
                {LEVELS.map(l => <option key={l}>{l}</option>)}
              </select>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.75rem" }}>
              <input type="checkbox" checked={newCourse.published}
                onChange={e => setNewCourse({ ...newCourse, published: e.target.checked })} />
              <label style={{ fontSize: "0.85rem", color: "#94a3b8" }}>Publikovať ihneď</label>
            </div>
            <button onClick={createCourse} disabled={loading || !newCourse.title}
              style={{ width: "100%", padding: "0.5rem", borderRadius: 6, background: "#7c3aed", border: "none", color: "white", cursor: "pointer" }}>
              Vytvoriť kurz
            </button>
          </div>

          {/* Course list */}
          {courses.map(course => (
            <div key={course.id} onClick={() => selectCourse(course)}
              style={{
                background: selectedCourse?.id === course.id ? "#2d1b69" : "#1e1e2e",
                border: `1px solid ${selectedCourse?.id === course.id ? "#7c3aed" : "#334155"}`,
                borderRadius: 8, padding: "0.75rem 1rem", marginBottom: "0.5rem", cursor: "pointer"
              }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <div style={{ fontWeight: 600 }}>{course.title}</div>
                  <div style={{ fontSize: "0.75rem", color: "#94a3b8" }}>{course.category} · {course.level} · {course.xpReward} XP</div>
                </div>
                <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                  <span style={{ fontSize: "0.7rem", padding: "2px 8px", borderRadius: 4, background: course.published ? "#14532d" : "#1e293b", color: course.published ? "#4ade80" : "#94a3b8" }}>
                    {course.published ? "Publikovaný" : "Draft"}
                  </span>
                  <button onClick={e => { e.stopPropagation(); deleteCourse(course.id); }}
                    style={{ background: "#7f1d1d", border: "none", color: "white", borderRadius: 4, padding: "2px 8px", cursor: "pointer", fontSize: "0.75rem" }}>
                    Zmazať
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Right: Lessons */}
        <div>
          <h2 style={{ fontSize: "1.1rem", marginBottom: "1rem", color: "#a78bfa" }}>
            {selectedCourse ? `Lekcie — ${selectedCourse.title}` : "Vyber kurz"}
          </h2>

          {selectedCourse && (
            <>
              {/* Generate lesson */}
              <div style={{ background: "#1e1e2e", borderRadius: 8, padding: "1rem", marginBottom: "1rem" }}>
                <h3 style={{ marginBottom: "0.75rem", fontSize: "0.9rem", color: "#94a3b8" }}>🤖 Generovať lekciu pomocou AI</h3>
                <input
                  placeholder="Téma lekcie (napr. 'Čo je economy v CS2')"
                  value={generateTopic}
                  onChange={e => setGenerateTopic(e.target.value)}
                  style={{ width: "100%", marginBottom: "0.75rem", padding: "0.5rem", borderRadius: 6, background: "#0f0f1a", border: "1px solid #334155", color: "white" }}
                />
                <button onClick={generateLesson} disabled={generating || !generateTopic.trim()}
                  style={{ width: "100%", padding: "0.5rem", borderRadius: 6, background: generating ? "#334155" : "#059669", border: "none", color: "white", cursor: "pointer" }}>
                  {generating ? "Generujem..." : "Generovať lekciu"}
                </button>
              </div>

              {/* Lessons list */}
              {lessons.length === 0 ? (
                <div style={{ color: "#94a3b8", textAlign: "center", padding: "2rem" }}>Žiadne lekcie</div>
              ) : (
                lessons.map(lesson => (
                  <div key={lesson.id} style={{ background: "#1e1e2e", border: "1px solid #334155", borderRadius: 8, padding: "0.75rem 1rem", marginBottom: "0.5rem" }}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <div>
                        <div style={{ fontWeight: 600, marginBottom: "0.25rem" }}>{lesson.title}</div>
                        <div style={{ fontSize: "0.75rem", color: "#94a3b8" }}>{lesson.estimatedMinutes} min · poradie {lesson.sortOrder}</div>
                      </div>
                      <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                        <span style={{ fontSize: "0.7rem", padding: "2px 8px", borderRadius: 4, background: lesson.published ? "#14532d" : "#1e293b", color: lesson.published ? "#4ade80" : "#94a3b8" }}>
                          {lesson.published ? "Publikovaná" : "Draft"}
                        </span>
                        {!lesson.published && (
                          <button onClick={() => publishLesson(lesson.id)}
                            style={{ background: "#1d4ed8", border: "none", color: "white", borderRadius: 4, padding: "2px 8px", cursor: "pointer", fontSize: "0.75rem" }}>
                            Publikovať
                          </button>
                        )}
                        <button onClick={() => deleteLesson(lesson.id)}
                          style={{ background: "#7f1d1d", border: "none", color: "white", borderRadius: 4, padding: "2px 8px", cursor: "pointer", fontSize: "0.75rem" }}>
                          Zmazať
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
