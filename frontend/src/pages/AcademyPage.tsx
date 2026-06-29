import { useState } from "react";
import { useNavigate } from "react-router-dom";

const sports = [
  {
    key: "CS2",
    name: "Counter-Strike 2",
    logo: "https://cdn.fastly.steamstatic.com/apps/csgo/images/csgo_react/social/cs2.jpg",
    description: "Od základných pravidiel až po taktické princípy profesionálneho CS2.",
    color: "#f59e0b"
  },
  {
    key: "LOL",
    name: "League of Legends",
    logo: "https://i0.wp.com/highschool.latimes.com/wp-content/uploads/2021/09/league-of-legends.jpeg?fit=1200%2C668&ssl=1",
    description: "Kompletné základy aj pokročilé témy o najväčšej MOBA hre na svete.",
    color: "#c89b3c"
  },
  {
    key: "F1",
    name: "Formula 1",
    logo: "https://images.ctfassets.net/gy95mqeyjg28/4DCWEw7FYzoky73roJSLpZ/16228392768ba87c63147fee8d390e5b/launch_2026_mcl40_desktop-2.png?w=3840&q=75&fm=webp&fit=fill",
    description: "Pravidlá, stratégie a fungovanie sveta Formuly 1.",
    color: "#ef4444"
  }
];

export default function AcademyPage() {
  const navigate = useNavigate();

  return (
    <div style={{ minHeight: "100vh", background: "#0a0a14", color: "white", padding: "2rem" }}>
      <button onClick={() => navigate("/")}
        style={{ background: "none", border: "none", color: "#94a3b8", cursor: "pointer", marginBottom: "1rem", fontSize: "0.9rem", display: "block", padding: "0.5rem 0" }}>
        ← Späť
      </button>
      <div style={{ textAlign: "center", marginBottom: "3rem", maxWidth: 700, margin: "0 auto 3rem" }}>
        <h1 style={{ fontSize: "2.5rem", fontWeight: 800, marginBottom: "0.75rem" }}>
          🎓 Predicto Academy
        </h1>
        <p style={{ color: "#94a3b8", fontSize: "1.1rem" }}>
          Staň sa informovanejším fanúšikom. Krátke, praktické lekcie o esporte a motorsporte.
        </p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "2rem", maxWidth: 1000, margin: "0 auto" }}>
        {sports.map(sport => (
          <div key={sport.key}
            onClick={() => navigate(`/academy/${sport.key.toLowerCase()}`)}
            style={{
              background: "#1e1e2e",
              borderRadius: 16,
              padding: "2.5rem 2rem",
              border: `2px solid #334155`,
              cursor: "pointer",
              textAlign: "center",
              transition: "border-color 0.2s, transform 0.2s",
            }}
            onMouseEnter={e => {
              e.currentTarget.style.borderColor = sport.color;
              e.currentTarget.style.transform = "translateY(-4px)";
            }}
            onMouseLeave={e => {
              e.currentTarget.style.borderColor = "#334155";
              e.currentTarget.style.transform = "translateY(0)";
            }}>
            <img src={sport.logo} alt={sport.name} style={{ width: "100%", height: 160, objectFit: "cover", borderRadius: 8, marginBottom: "1rem" }} />
            <h2 style={{ fontSize: "1.3rem", fontWeight: 700, marginBottom: "0.75rem", color: sport.color }}>{sport.name}</h2>
            <p style={{ color: "#94a3b8", fontSize: "0.9rem", lineHeight: 1.6 }}>{sport.description}</p>
            <div style={{ marginTop: "1.5rem", padding: "0.5rem 1.5rem", borderRadius: 20, background: sport.color + "22", color: sport.color, fontWeight: 600, display: "inline-block", fontSize: "0.85rem" }}>
              Začať →
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
