---
description: Speckit implement orchestrato - un task-worker per task, poi review finale
agent: speckit-orchestrator
---

Orchestra l'implementazione della feature $ARGUMENTS (default: la feature corrente in
`.specify/feature.json`) senza implementare codice tu stesso.

1. Leggi `specs/<feature>/tasks.md`, `spec.md` e `plan.md`.
2. Prendi i task non completati in ordine e prepara per ciascuno un brief con: id task, testo,
   **scope di file esplicito**, acceptance criteria (FR/SC) e test da mantenere verdi.
3. Lancia un `task-worker` per task. Parallelizza solo task con scope di file disgiunti; altrimenti
   serializza. Non lanciare altri subagent oltre a `task-worker`, `reviewer`, `coverage-analyst`.
4. Al termine raccogli il diff (`git diff`) e lancia `reviewer` con diff + acceptance criteria.
5. Riporta in italiano: task completati, finding del reviewer, file toccati e cosa richiede un run
   CI. Nessun commit, nessun push, nessun `gh workflow run` senza approvazione esplicita.
