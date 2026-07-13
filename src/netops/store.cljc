(ns netops.store
  "SSoT for the ISCO-08 2523 community network-professionals actor
  (itonami actor pattern, ADR-2607011000 / CLAUDE.md Actors section).
  Modeled on cloud-itonami-isco-4311's bookkeeping.store.

  Domain:

    client — a registered organization (:client-id, :name)
    zone   — a registered network zone {:zone-id :client-id :name}.
             Rules may only reference registered zones.
    rule   — an ACTIVE ordered firewall rule {:rule-id :client-id
             :order int :action :allow|:deny :src-zones #{zone-id}
             :dst-zones #{zone-id}}. Ordering is total per client;
             an earlier rule whose match sets contain a later rule's
             match sets SHADOWS it — dead config is set containment,
             not opinion.
    record — a committed operating record (added rule, config change)
             — written ONLY via commit-record!.
    ledger — append-only audit trail, commit or hold."
  )

(defprotocol Store
  (client [s client-id])
  (zone [s zone-id])
  (rules-of [s client-id])
  (records-of [s client-id])
  (ledger [s])
  (register-client! [s client])
  (register-zone! [s z])
  (register-rule! [s r])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (client [_ client-id] (get-in @a [:clients client-id]))
  (zone [_ zone-id] (get-in @a [:zones zone-id]))
  (rules-of [_ client-id]
    (sort-by :order (filter #(= client-id (:client-id %)) (vals (:rules @a)))))
  (records-of [_ client-id] (filter #(= client-id (:client-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-client! [s client]
    (swap! a assoc-in [:clients (:client-id client)] client) s)
  (register-zone! [s z]
    (swap! a assoc-in [:zones (:zone-id z)] z) s)
  (register-rule! [s r]
    (swap! a assoc-in [:rules (:rule-id r)] r) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:clients {} :zones {} :rules {}
                                    :records [] :ledger []}
                                   seed)))))
