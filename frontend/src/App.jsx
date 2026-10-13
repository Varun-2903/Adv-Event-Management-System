import React, { useEffect, useState } from "react";
import { QRCodeCanvas } from "qrcode.react";
import { api, getPublicEvents, login, register } from "./api";

function Nav({ user, logout }) {
  return (
    <header className="nav">
      <div className="brand">Event<span>Hub</span></div>
      <div className="nav-right">
        {user && <span className="role">{user.role}</span>}
        {user && <button className="ghost" onClick={logout}>Logout</button>}
      </div>
    </header>
  );
}

function Auth({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({name:"",email:"",password:"",role:"PARTICIPANT"});
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const result = mode === "login"
        ? await login({email:form.email,password:form.password})
        : await register(form);
      localStorage.setItem("token", result.token);
      localStorage.setItem("user", JSON.stringify(result));
      onLogin(result);
    } catch (err) { setError(err.message); }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="logo">EH</div>
        <h1>{mode === "login" ? "Welcome back" : "Create account"}</h1>
        <p className="muted">Event Management System</p>
        {error && <div className="error">{error}</div>}
        <form onSubmit={submit}>
          {mode === "register" && <>
            <label>Name<input value={form.name} onChange={e=>setForm({...form,name:e.target.value})} required /></label>
            <label>Role
              <select value={form.role} onChange={e=>setForm({...form,role:e.target.value})}>
                <option>PARTICIPANT</option>
                <option>ORGANIZER</option>
              </select>
            </label>
          </>}
          <label>Email<input type="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} required /></label>
          <label>Password<input type="password" value={form.password} onChange={e=>setForm({...form,password:e.target.value})} required /></label>
          <button className="primary wide">{mode === "login" ? "Login" : "Register"}</button>
        </form>
        <button className="link" onClick={()=>{setMode(mode==="login"?"register":"login");setError("")}}>
          {mode === "login" ? "Create new account" : "Already have an account? Login"}
        </button>
      </div>
    </div>
  );
}

function EventCard({event, user, onRegister}) {
  return (
    <div className="event-card">
      <div className="event-top">
        <span className="pill">{event.category || "General"}</span>
        <span>{event.status}</span>
      </div>
      <h3>{event.title}</h3>
      <p>{event.description || "No description provided."}</p>
      <div className="meta">📅 {event.eventDate} {event.startTime ? `• ${event.startTime}` : ""}</div>
      <div className="meta">📍 {event.venue}</div>
      <div className="meta">👥 {event.registrations}/{event.capacity} registered</div>
      <div className="meta"><b>{Number(event.price || 0) === 0 ? "Free" : `₹${Number(event.price).toFixed(2)}`}</b></div>
      {user?.role === "PARTICIPANT" && (
        <button className="primary" onClick={()=>onRegister(event.id, Number(event.price || 0))}>{Number(event.price || 0) > 0 ? "Pay & Register" : "Register Free"}</button>
      )}
    </div>
  );
}

function ParticipantDashboard() {
  const [events,setEvents] = useState([]);
  const [regs,setRegs] = useState([]);
  const [error,setError] = useState("");

  async function load() {
    try { setEvents(await getPublicEvents()); setRegs(await api("/registrations/mine")); }
    catch(e){ setError(e.message); }
  }
  useEffect(()=>{load()},[]);

  async function reg(id, price = 0) {
    setError("");
    try {
      if (price <= 0) {
        await api(`/registrations/event/${id}`,{method:"POST"});
        await load();
        return;
      }
      const order = await api(`/payments/create-order/${id}`, {method:"POST"});
      if (!window.Razorpay) {
        await new Promise((resolve,reject) => {
          const script=document.createElement("script");
          script.src="https://checkout.razorpay.com/v1/checkout.js";
          script.onload=resolve; script.onerror=()=>reject(new Error("Could not load Razorpay checkout. Check your internet connection."));
          document.body.appendChild(script);
        });
      }
      const checkout = new window.Razorpay({
        key: order.keyId, order_id: order.orderId, amount: order.amount, currency: order.currency,
        name: "EventHub", description: order.eventTitle,
        prefill: {name: order.participantName, email: order.participantEmail},
        theme: {color: "#4f46e5"},
        handler: async (response) => {
          try {
            const verified = await api("/payments/verify", {method:"POST", body:JSON.stringify(response)});
            alert(`Payment successful! Your unique booking code is: ${verified.bookingCode || "available in My Registrations"}. Your QR ticket is ready.`);
            await load();
          } catch (err) { setError(err.message || "Payment verification failed. Contact the organizer before paying again."); }
        },
        modal: {ondismiss: () => setError("Payment cancelled. You have not been registered.")}
      });
      checkout.on("payment.failed", (response) => setError(response.error?.description || "Payment failed. Please try again."));
      checkout.open();
    } catch(e){ setError(e.message); }
  }

  async function feedback(eventId) {
    const rating = Number(prompt("Rating (1-5):", "5"));
    if (!rating) return;
    const comment = prompt("Comment:", "") || "";
    try { await api("/feedback",{method:"POST",body:JSON.stringify({eventId,rating,comment})}); alert("Feedback submitted"); }
    catch(e){ alert(e.message); }
  }

  return <main className="container">
    <div className="page-head"><div><h1>Discover Events</h1><p>Find upcoming events and manage your registrations.</p></div></div>
    {error && <div className="error">{error}</div>}
    <section className="grid">{events.map(e=><EventCard key={e.id} event={e} user={{role:"PARTICIPANT"}} onRegister={reg}/>)}</section>
    <h2>My Registrations</h2>
    <section className="registration-list">
      {regs.length === 0 ? <div className="empty">No registrations yet.</div> : regs.map(r =>
        <div className="reg-card" key={r.id}>
          <div><b>{r.eventTitle}</b><small>Booking code: <strong>{r.bookingCode}</strong></small><small>{r.checkedIn ? "✓ Checked in" : "Not checked in"}</small></div>
          <QRCodeCanvas value={r.qrToken} size={110}/>
          <button className="secondary" onClick={()=>feedback(r.eventId)}>Feedback</button>
        </div>
      )}
    </section>
  </main>
}

function OrganizerDashboard() {
  const [events,setEvents] = useState([]);
  const [form,setForm] = useState({title:"",description:"",category:"Seminar",eventDate:"",startTime:"",endTime:"",venue:"",capacity:100,price:0});
  const [scan,setScan] = useState("");
  const [message,setMessage] = useState("");

  async function load(){ setEvents(await api("/events/mine")); }
  useEffect(()=>{load()},[]);

  async function create(e) {
    e.preventDefault();
    try { await api("/events",{method:"POST",body:JSON.stringify({...form,capacity:Number(form.capacity),price:Number(form.price)})}); setMessage("Event submitted for admin approval."); setForm({title:"",description:"",category:"Seminar",eventDate:"",startTime:"",endTime:"",venue:"",capacity:100,price:0}); load(); }
    catch(e){ setMessage(e.message); }
  }

  async function checkIn(e) {
    e.preventDefault();
    try { const r=await api("/registrations/checkin",{method:"POST",body:JSON.stringify({qrToken:scan})}); setMessage(`${r.participant} checked in successfully.`); setScan(""); }
    catch(e){setMessage(e.message)}
  }

  return <main className="container">
    <div className="page-head"><div><h1>Organizer Dashboard</h1><p>Create events and manage attendance.</p></div></div>
    {message && <div className="notice">{message}</div>}
    <div className="two-col">
      <form className="panel" onSubmit={create}>
        <h2>Create Event</h2>
        {["title","description","category","eventDate","startTime","endTime","venue","capacity"].map(k=>
          <label key={k}>{k.replace(/([A-Z])/g," $1")}
            {k==="description" ? <textarea value={form[k]} onChange={e=>setForm({...form,[k]:e.target.value})}/> :
            <input type={k==="eventDate"?"date":k==="startTime"||k==="endTime"?"time":k==="capacity"?"number":"text"} value={form[k]} onChange={e=>setForm({...form,[k]:e.target.value})} required={["title","eventDate","venue","capacity"].includes(k)}/>}
          </label>
        )}
        <label>Ticket price (₹; enter 0 for free)<input type="number" min="0" step="0.01" value={form.price} onChange={e=>setForm({...form,price:e.target.value})} required /></label>
        <button className="primary wide">Submit for Approval</button>
      </form>
      <div className="panel">
        <h2>QR Check-in</h2>
        <p className="muted">Paste the participant QR token here. A camera scanner can be connected later.</p>
        <form onSubmit={checkIn}><input value={scan} onChange={e=>setScan(e.target.value)} placeholder="QR token"/><button className="primary wide">Mark Attendance</button></form>
        <h3>Your Events</h3>
        {events.map(e=><div className="mini-row" key={e.id}><b>{e.title}</b><span>{e.status} • {e.attendance}/{e.registrations}</span></div>)}
      </div>
    </div>
  </main>
}

function AdminDashboard() {
  const [events,setEvents]=useState([]);
  const [msg,setMsg]=useState("");
  async function load(){setEvents(await api("/events/pending"))}
  useEffect(()=>{load()},[]);
  async function action(id,type){try{await api(`/events/${id}/${type}`,{method:"PUT"});setMsg(`Event ${type}d successfully.`);load()}catch(e){setMsg(e.message)}}
  return <main className="container">
    <div className="page-head"><div><h1>Admin Approval</h1><p>Review organizer submissions before publishing.</p></div></div>
    {msg && <div className="notice">{msg}</div>}
    <section className="grid">
      {events.length===0 ? <div className="empty">No pending events.</div> : events.map(e=>
        <div className="event-card" key={e.id}>
          <span className="pill">{e.category}</span><h3>{e.title}</h3><p>{e.description}</p>
          <div className="meta">Organizer: {e.organizer}</div><div className="meta">📅 {e.eventDate} • 📍 {e.venue}</div><div className="meta">Ticket: {Number(e.price || 0) === 0 ? "Free" : `₹${Number(e.price).toFixed(2)}`}</div>
          <div className="actions"><button className="primary" onClick={()=>action(e.id,"approve")}>Approve & Publish</button><button className="danger" onClick={()=>action(e.id,"reject")}>Reject</button></div>
        </div>
      )}
    </section>
  </main>
}

function App() {
  const [user,setUser]=useState(()=>JSON.parse(localStorage.getItem("user")||"null"));
  const logout=()=>{localStorage.clear();setUser(null)};
  return <>
    {user && <Nav user={user} logout={logout}/>}
    {!user ? <Auth onLogin={setUser}/> :
      user.role==="ADMIN" ? <AdminDashboard/> :
      user.role==="ORGANIZER" ? <OrganizerDashboard/> :
      <ParticipantDashboard/>}
  </>
}

export default App;
