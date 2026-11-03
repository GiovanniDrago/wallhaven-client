---
description: Review read-only di una feature spec-kit contro i suoi acceptance criteria
agent: reviewer
subagent: true
---

Rivedi la feature `$ARGUMENTS` (es. `004` o `specs/004-drawer-settings-update`).

Procedi così:

1. Individua la directory `specs/` corrispondente (match esatto o per prefisso numerico) e leggi
   `spec.md`, `plan.md`, `tasks.md`, `data-model.md` e `contracts/` se presenti.
2. Leggi i file applicativi elencati nei task completati e i test unitari corrispondenti in
   `app/src/test/java/`.
3. Confronta l'implementazione con ogni acceptance scenario di `spec.md` e riporta i finding in
   ordine di severità (`blocker`, `major`, `minor`, `nit`) con path e riga.
4. Aggiungi la tabella criterio → esito (`met` / `not met` / `not verifiable without CI`) e i gap
   di test.

Non modificare file e non eseguire comandi: sei read-only. Rispondi in italiano.
