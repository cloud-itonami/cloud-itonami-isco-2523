# cloud-itonami-isco-2523

Open Business Blueprint for **ISCO-08 2523**: Computer Network Professionals — an ISCO
**Wave 0 (cognitive substrate)** occupation per ADR-2607121000:
pure-cognitive work, the LLM-first wave, **no robotics gate** —
eligible for actor implementation now.

**Maturity: `:implemented`** — NetworkProfessionalsAdvisor ⊣
NetworkProfessionalsGovernor as a langgraph StateGraph
(`intake → advise → govern → decide → commit/hold`, human-approval
interrupt), modeled on cloud-itonami-isco-4311's bookkeeping actor.
25 tests / 73 assertions green.

Run them with `clojure -M:test`, which runs `run_tests.kotoba`. That
count is a **floor**, not a note: the runner reads it back out of this
sentence and refuses (exit 2) if the run comes in under it, if the
sentence goes missing, or if it ever publishes zero. Between
2026-09-10 and 2026-09-11 this suite exited 0 having run zero tests —
the rename to `.kotoba` had made every test invisible to
`cognitect.test-runner`, and a suite that ran nothing returned the
same value as a suite that ran everything.

The network HARD invariant — shadow detection by set containment:

1. **Shadowed rule** — a proposed rule whose src/dst zone sets are
   contained by an earlier active rule's is dead config, whether the
   shadow is a redundancy (same action) or a contradiction (different
   action — the earlier rule always wins). Dead config is set
   containment, not opinion.
2. **Zone basis** — rules may only cite this client's registered
   zones (no invented or foreign zones).
3. **Empty basis** — a rule naming no zone on either side matches
   nothing; dead by construction rather than by containment.

**1–3 read the rule body, whatever operation carries it.** Until
2026-09-11 they sat behind `(= :add-rule op)`, so the identical body
under `:draft-change` or `:apply-to-production` was never examined —
and `:apply-to-production` with an invented zone reached human
sign-off described as having no violations. Stopping and giving a
reason are different acts.

Also HARD: unregistered organization, non-`:propose` effect, and an
**undeclared operation** — `:op` is a closed allowlist
(`:add-rule`, `:draft-change`, `:apply-to-production`), so anything
else holds instead of passing through unchecked.
Escalations (always human sign-off): `:apply-to-production` (live
network change), low confidence (< 0.6).

AGPL-3.0-or-later, forkable by any qualified operator. Part of the
[cloud-itonami](https://itonami.cloud) open business fleet.
