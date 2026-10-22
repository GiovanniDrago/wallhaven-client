---
description: Coverage unit-test per spec dall'artifact CI (read-only, nessuna build locale)
agent: coverage-analyst
subagent: true
---

Analizza la coverage dei test unitari per le feature $ARGUMENTS (se vuoto: tutte le spec in
`specs/`).

Segui la procedura del tuo prompt:

1. Leggi `AGENTS.md` per la baseline (spec 002: `ZoomPan`/gesture; spec 004: filtri drawer e
   update check; fuori scope: Compose UI e DAO con DB reale).
2. Trova l'ultimo run del workflow `test.yml` e scarica l'artifact
   `wallhaven-client-coverage-*` con `gh run download` in `/tmp/opencode/coverage`. Esegui **un solo
   comando semplice per chiamata**: niente pipe, `&&`, `;`, `head`/`tail`, perché un comando
   composto viene spezzato e ogni segmento fuori dalla allowlist fa fallire l'intera chiamata.
3. Leggi il `summary.txt` che lo step CI scrive accanto a `report.xml` (TSV:
   `package, sourcefile, line_covered, line_total, branch_covered, branch_total`). È la fonte
   autorevole per i numeri per file: `report.xml` è una singola riga XML enorme che i tool di
   lettura troncano, quindi non usarlo e non ri-derivare le percentuali dalle pagine HTML.
   Produci, per ogni spec, la tabella `file | linee coperte/totali | branch coperti/totali`, con
   l'elenco di ciò che è scoperto e di ciò che è scoperto solo in parte.
4. Chiudi **sempre** con: **Coperto**, **Scoperto**, **Gap** (criteri di accettazione senza test) e
   **Fuori scope**.
5. Se l'artifact non esiste, non inventare numeri: riporta la baseline attesa da `AGENTS.md` come
   "non misurato", indica il comando esatto che serve (`gh workflow run test.yml --ref <branch>`)
   come "richiede un run CI" e chiudi comunque con il riepilogo.


Rispondi in italiano, conciso, senza modificare file.
