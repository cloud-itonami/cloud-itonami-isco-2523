# cloud-itonami-isco-2523

Open Business Blueprint for **ISCO-08 2523**: Computer Network Professionals — an ISCO
**Wave 0 (cognitive substrate)** occupation per ADR-2607121000:
pure-cognitive work, the LLM-first wave, **no robotics gate** —
eligible for actor implementation now.

**Maturity: `:implemented`** — NetworkProfessionalsAdvisor ⊣
NetworkProfessionalsGovernor as a langgraph StateGraph
(`intake → advise → govern → decide → commit/hold`, human-approval
interrupt), modeled on cloud-itonami-isco-4311's bookkeeping actor.
14 tests / 29 assertions green.

The network HARD invariant — shadow detection by set containment:

1. **Shadowed rule** — a proposed rule whose src/dst zone sets are
   contained by an earlier active rule's is dead config, whether the
   shadow is a redundancy (same action) or a contradiction (different
   action — the earlier rule always wins). Dead config is set
   containment, not opinion.
2. **Zone basis** — rules may only cite this client's registered
   zones (no invented or foreign zones).

Also HARD: unregistered organization, non-`:propose` effect.
Escalations (always human sign-off): `:apply-to-production` (live
network change), low confidence (< 0.6).

AGPL-3.0-or-later, forkable by any qualified operator. Part of the
[cloud-itonami](https://itonami.cloud) open business fleet.
