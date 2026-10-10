const API = "http://localhost:8080/api";

export async function api(path, options = {}) {
  const token = localStorage.getItem("token");
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(API + path, { ...options, headers });
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }

  if (!response.ok) {
    throw new Error(data?.message || "Request failed");
  }
  return data;
}

export const getPublicEvents = () => api("/events/public");
export const login = (data) => api("/auth/login", { method:"POST", body:JSON.stringify(data) });
export const register = (data) => api("/auth/register", { method:"POST", body:JSON.stringify(data) });
