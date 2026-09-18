import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API = import.meta.env.VITE_API_BASE_URL || '/api';

const DEMO_REQUESTS = [
  ['Password lockout', 'I tried my password 6 times and now my account is locked.'],
  ['VPN expired', 'My VPN credentials expired and I cannot connect.'],
  ['Phishing', 'I received a phishing email.'],
  ['Expense access', 'Please give me access to the expense management system.'],
  ['Vague laptop', 'My laptop is having problems.']
];

const WORKFLOW_STEPS = [
  'Employee Request',
  'Knowledge Retrieval',
  'Policy Matching',
  'Agent Decision',
  'Ticket Creation',
  'Audit Logging',
  'Response'
];

function App() {
  const [name, setName] = useState('Aditi Sharma');
  const [email, setEmail] = useState('aditi.sharma@veridian-corp.example');
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function submitRequest(event) {
    event.preventDefault();
    setError('');
    setResponse(null);
    setLoading(true);

    try {
      const result = await fetch(`${API}/agent/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          employeeName: name,
          employeeEmail: email,
          message
        })
      });

      if (!result.ok) {
        throw new Error('Support request failed');
      }

      setResponse(await result.json());
    } catch {
      setError('Unable to reach support service');
    } finally {
      setLoading(false);
    }
  }

  function startNewRequest() {
    setMessage('');
    setResponse(null);
    setError('');
  }

  return (
    <main>
      <Header />
      <Workflow responseReady={Boolean(response)} />
      <section className="grid">
        <RequestForm
          name={name}
          email={email}
          message={message}
          loading={loading}
          error={error}
          onNameChange={setName}
          onEmailChange={setEmail}
          onMessageChange={setMessage}
          onSubmit={submitRequest}
        />
        <ResponsePanel
          loading={loading}
          response={response}
          onReset={startNewRequest}
        />
      </section>
      <footer>Veridian IT · Helpdesk operations</footer>
    </main>
  );
}

function Header() {
  return (
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
  );
}

function Workflow({ responseReady }) {
  return (
    <nav className="workflow" aria-label="Support workflow">
      {WORKFLOW_STEPS.map((step, index) => (
        <React.Fragment key={step}>
          <div className={`workflow-step ${index === 0 || responseReady && index === WORKFLOW_STEPS.length - 1 ? 'active' : ''}`}>
            <span>{String(index + 1).padStart(2, '0')}</span>
            <strong>{step}</strong>
          </div>
          {index < WORKFLOW_STEPS.length - 1 && <i className="workflow-arrow">→</i>}
        </React.Fragment>
      ))}
    </nav>
  );
}

function RequestForm({
  name,
  email,
  message,
  loading,
  error,
  onNameChange,
  onEmailChange,
  onMessageChange,
  onSubmit
}) {
  return (
    <form className="card request-card" onSubmit={onSubmit}>
      <div className="card-heading">
        <div><span className="eyebrow">01 / REQUEST</span><h2>Tell us what you need</h2></div>
        <span className="stamp">PRIVATE</span>
      </div>
      <p className="helper">Share a few details and the support agent will route your request.</p>

      <label htmlFor="name">Employee name</label>
      <input id="name" value={name} onChange={event => onNameChange(event.target.value)} required maxLength="120" />

      <label htmlFor="email">Work email</label>
      <input id="email" type="email" value={email} onChange={event => onEmailChange(event.target.value)} required maxLength="254" />

      <label htmlFor="issue">What can we help with?</label>
      <textarea
        id="issue"
        rows="7"
        value={message}
        onChange={event => onMessageChange(event.target.value)}
        placeholder="Describe the IT issue..."
        required
        maxLength="4000"
      />

      <div className="chips">
        {DEMO_REQUESTS.map(([label, value]) => (
          <button type="button" onClick={() => onMessageChange(value)} key={label}>
            {label}
          </button>
        ))}
      </div>

      <button className="ask" disabled={loading}>
        {loading ? <><span className="spinner" />Analyzing request</> : <>Ask the agent <span>→</span></>}
      </button>

      {error && (
        <div className="error">
          <strong>Unable to reach support service</strong>
          <span>Check that the support service is running and try again.</span>
        </div>
      )}
    </form>
  );
}

function ResponsePanel({ loading, response, onReset }) {
  return (
    <section className="card response-card" aria-live="polite">
      <div className="card-heading">
        <div><span className="eyebrow">02 / OUTCOME</span><h2>Agent response</h2></div>
        {response && <button className="reset" type="button" onClick={onReset}>↻ <span>New request</span></button>}
      </div>

      {loading && <LoadingState />}
      {!loading && !response && <EmptyState />}
      {!loading && response && <ResponseContent response={response} />}
    </section>
  );
}

function LoadingState() {
  return (
    <div className="state loading-state">
      <span className="loader" />
      <strong>Agent is analyzing your request...</strong>
      <p>Checking internal guidance and finding the right team.</p>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="state empty">
      <span className="empty-icon">✦</span>
      <strong>Your support outcome will appear here</strong>
      <p>Submit a request to see the decision, guidance, and ticket details.</p>
    </div>
  );
}

function ResponseContent({ response }) {
  return (
    <>
      <div className="success">
        <span>✓</span>
        <div>
          <strong>Request processed successfully</strong>
          <small>{response.ticketId ? `Ticket TK-${response.ticketId} created.` : 'No IT ticket is required for this policy.'}</small>
        </div>
      </div>

      <div className="facts">
        <Fact label="Decision" value={response.decision} variant="decision" />
        <Fact label="Reason" value={response.response} />
        <Fact label="Knowledge Source" value={response.source} />
        <Fact label="Department" value={response.assignedTo} />
        <Fact label="Ticket ID" value={response.ticketId ? `TK-${response.ticketId}` : 'Not required'} />
        <Fact label="Next Action" value={response.nextAction} />
      </div>

      {response.historyContext && (
        <div className="history">
          <small>Ticket / History Context</small>
          <strong>{response.historyContext}</strong>
        </div>
      )}

      <AuditTrail entries={response.audit || []} />
    </>
  );
}

function Fact({ label, value, variant = '' }) {
  return (
    <div className={`fact ${variant}`}>
      <small>{label}</small>
      <strong>{value}</strong>
    </div>
  );
}

function AuditTrail({ entries }) {
  return (
    <div className="audit">
      <h3>Activity log</h3>
      {entries.map((entry, index) => (
        <div className="auditrow" key={`${entry.action}-${index}`}>
          <i />
          <span>
            <strong>{entry.action}</strong><br />
            {entry.details}
            <small>{entry.timestamp.replace('T', ' ')}</small>
          </span>
        </div>
      ))}
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);
