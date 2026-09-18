import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const demos = [
  ['Password lockout', 'I tried my password 6 times and now my account is locked.'],
  ['VPN expired', 'My VPN credentials expired and I cannot connect.'],
  ['Phishing', 'I received a phishing email.'],
  ['Expense access', 'Please give me access to the expense management system.'],
  ['Vague laptop', 'My laptop is having problems.']
];
const workflow = ['Employee Request', 'Knowledge Retrieval', 'Policy Matching', 'Agent Decision', 'Ticket Creation', 'Audit Logging', 'Response'];

function App() {
  const [name, setName] = useState('Aditi Sharma');
  const [email, setEmail] = useState('aditi.sharma@veridian-corp.example');
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    setResponse(null);
    setLoading(true);
    try {
      const result = await fetch(`${API}/agent/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ employeeName: name, employeeEmail: email, message })
      });
      if (!result.ok) throw new Error();
      setResponse(await result.json());
    } catch {
      setError('Unable to reach support service');
    } finally {
      setLoading(false);
    }
  }

  function reset() {
    setMessage('');
    setResponse(null);
    setError('');
  }

  return (
    <main>
      <header>
        <div className="brand">
          <span className="brand-mark">V</span>
          <div>
            <small>VERIDIAN CORP · INTERNAL SERVICES</small>
            <h1>Veridian IT Support</h1>
            <p>Internal IT Helpdesk Assistant</p>
          </div>
        </div>
        <span className="status"><i /> Online</span>
      </header>

      <nav className="workflow" aria-label="Support workflow">
        {workflow.map((step, index) => (
          <React.Fragment key={step}>
            <div className={`workflow-step ${index === 0 || response && index === workflow.length - 1 ? 'active' : ''}`}>
              <span>{String(index + 1).padStart(2, '0')}</span>
              <strong>{step}</strong>
            </div>
            {index < workflow.length - 1 && <i className="workflow-arrow">→</i>}
          </React.Fragment>
        ))}
      </nav>

      <section className="grid">
        <form className="card request-card" onSubmit={submit}>
          <div className="card-heading">
            <div><span className="eyebrow">01 / REQUEST</span><h2>Tell us what you need</h2></div>
            <span className="stamp">PRIVATE</span>
          </div>
          <p className="helper">Share a few details and the support agent will route your request.</p>
          <label htmlFor="name">Employee name</label>
          <input id="name" value={name} onChange={event => setName(event.target.value)} required maxLength="120" />
          <label htmlFor="email">Work email</label>
          <input id="email" type="email" value={email} onChange={event => setEmail(event.target.value)} required maxLength="254" />
          <label htmlFor="issue">What can we help with?</label>
          <textarea id="issue" rows="7" value={message} onChange={event => setMessage(event.target.value)} placeholder="Describe the IT issue..." required maxLength="4000" />
          <div className="chips">{demos.map(([label, value]) => <button type="button" onClick={() => setMessage(value)} key={label}>{label}</button>)}</div>
          <button className="ask" disabled={loading}>{loading ? <><span className="spinner" />Analyzing request</> : <>Ask the agent <span>→</span></>}</button>
          {error && <div className="error"><strong>Unable to reach support service</strong><span>Check that the support service is running and try again.</span></div>}
        </form>

        <section className="card response-card" aria-live="polite">
          <div className="card-heading">
            <div><span className="eyebrow">02 / OUTCOME</span><h2>Agent response</h2></div>
            {response && <button className="reset" type="button" onClick={reset}>↻ <span>New request</span></button>}
          </div>
          {loading ? <div className="state loading-state"><span className="loader" /><strong>Agent is analyzing your request...</strong><p>Checking internal guidance and finding the right team.</p></div> : !response ? <div className="state empty"><span className="empty-icon">✦</span><strong>Your support outcome will appear here</strong><p>Submit a request to see the decision, guidance, and ticket details.</p></div> : <>
            <div className="success"><span>✓</span><div><strong>Request processed successfully</strong><small>{response.ticketId ? `Ticket TK-${response.ticketId} created.` : 'No IT ticket is required for this policy.'}</small></div></div>
            <div className="facts">
              <div className="fact decision"><small>Decision</small><strong>{response.decision}</strong></div>
              <div className="fact"><small>Reason</small><strong>{response.response}</strong></div>
              <div className="fact"><small>Knowledge Source</small><strong>{response.source}</strong></div>
              <div className="fact"><small>Department</small><strong>{response.assignedTo}</strong></div>
              <div className="fact"><small>Ticket ID</small><strong>{response.ticketId ? `TK-${response.ticketId}` : 'Not required'}</strong></div>
              <div className="fact"><small>Next Action</small><strong>{response.nextAction}</strong></div>
            </div>
            {response.historyContext && <div className="history"><small>Ticket / History Context</small><strong>{response.historyContext}</strong></div>}
            <div className="audit"><h3>Activity log</h3>{(response.audit || []).map((item, index) => <div className="auditrow" key={`${item.action}-${index}`}><i /><span><strong>{item.action}</strong><br />{item.details}<small>{item.timestamp.replace('T', ' ')}</small></span></div>)}</div>
          </>}
        </section>
      </section>
      <footer>Veridian IT · Helpdesk operations</footer>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
