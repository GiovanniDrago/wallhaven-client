---
description: Speckit implement orchestrato - un task-worker per task, poi review finale
agent: speckit-orchestrator
---

Orchestra l'implementazione della feature $ARGUMENTS (default: la feature corrente in
`.specify/feature.json`) senza implementare codice tu stesso.

## Hook di estensione (pre/post implement)

Gli hook registrati in `.specify/extensions.yml` vengono eseguiti dai template core: `/impl-parallel`
li bypasserebbe, quindi li gestisci qui con le stesse regole di `speckit.implement`.

- Se il file manca, salta in silenzio. Se non è parsabile, riporta l'errore e continua dichiarando
  che nessun hook è stato controllato, inclusi quelli `optional: false`.
- Esegui `hooks.before_implement` prima dell'orchestrazione e `hooks.after_implement` dopo il
  `reviewer`, prima del report finale.
- Scarta gli hook con `enabled: false` (campo assente = abilitato).
- Non valutare `condition`: se è non vuota, salta l'hook (la valutazione spetta a `HookExecutor`).
- `optional: true` → mostra il prompt e **attendi l'approvazione esplicita dell'utente** in questa
  conversazione; senza approvazione l'hook non si esegue e lo annoti nel report. In questo repo vale
  per commit, push e `gh workflow run` (vincolo in `AGENTS.md`).
- `optional: false` → invoca l'hook e attendi il risultato prima di proseguire.
- Invoca davvero il comando nella sintassi dell'harness (`/speckit.git.commit`,
  `/speckit.apk-delivery.deliver`, ...): emettere il blocco non basta.

## Orchestrazione

1. Leggi `specs/<feature>/tasks.md`, `spec.md` e `plan.md`.
2. Prendi i task non completati in ordine e prepara per ciascuno un brief con: id task, testo,
   **scope di file esplicito**, acceptance criteria (FR/SC) e test da mantenere verdi.
3. Lancia un `task-worker` per task. Parallelizza solo task con scope di file disgiunti; altrimenti
   serializza. Non lanciare altri subagent oltre a `task-worker`, `reviewer`, `coverage-analyst`.
4. Al termine raccogli il diff (`git diff`) e lancia `reviewer` con diff + acceptance criteria.
5. Esegui gli hook `after_implement` (sezione sopra).
6. Riporta in italiano: task completati, finding del reviewer, file toccati, esito degli hook e cosa
   richiede un run CI. Nessun commit, nessun push, nessun `gh workflow run` senza approvazione
   esplicita.
