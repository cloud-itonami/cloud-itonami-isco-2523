(ns netops.advisor
  "NetworkProfessionalsAdvisor — proposes a network operation (add a
  firewall rule, draft a config change, apply to production) for a
  registered organization. Swappable mock/llm; the advisor ONLY
  proposes — `netops.governor` performs shadow detection against the
  active ruleset independently. Modeled on cloud-itonami-isco-4311's
  advisor.

  A proposal: {:op :add-rule|:draft-change|:apply-to-production
               :effect :propose
               :rule {:action kw :src-zones #{} :dst-zones #{}}
               :stake kw :confidence n :rationale str}")

(defprotocol Advisor
  (-advise [advisor store request] "request -> proposal map"))

(defn- infer [_store {:keys [op stake rule] :as request}]
  {:op op
   :effect :propose
   :rule rule
   :stake (or stake :low)
   :confidence (case (or stake :low) :high 0.7 :medium 0.85 :low 0.95)
   :rationale (str "proposed " (name op) " for client " (:client-id request))})

(defn mock-advisor []
  (reify Advisor
    (-advise [_ store request] (infer store request))))

(def ^:private system-prompt
  "You are a network operations advisor. Given a request, propose an
   :op and, for rule additions, the :rule body, an honest :confidence
   and a :stake. Never add config an earlier rule already covers — the
   governor runs shadow detection against the active ruleset.")

(defn- parse-proposal [content]
  (try
    (let [p (read-string content)]
      (if (map? p)
        (assoc p :effect :propose)
        {:op :unknown :effect :propose :confidence 0.0 :stake :high
         :rationale "unparseable LLM response"}))
    (catch #?(:clj Exception :cljs js/Error) _
      {:op :unknown :effect :propose :confidence 0.0 :stake :high
       :rationale "LLM response parse failure"})))

(defn llm-advisor
  [chat-model model-generate-fn gen-opts]
  (reify Advisor
    (-advise [_ _store request]
      (let [msgs [{:role :system :content system-prompt}
                  {:role :user :content (str "operation request: " (pr-str request))}]
            resp (model-generate-fn chat-model msgs gen-opts)]
        (parse-proposal (:content resp))))))
