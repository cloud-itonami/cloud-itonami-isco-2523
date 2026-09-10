(ns netops.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [netops.store :as store]
            [netops.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-client! st {:client-id "client-1" :name "Kobo Trade"})
    (doseq [z ["dmz" "lan" "db" "mgmt"]]
      (store/register-zone! st {:zone-id z :client-id "client-1" :name z}))
    st))

(defn- add-rule [rule]
  {:op :add-rule :effect :propose :rule rule
   :confidence 0.9 :stake :low})

(def ^:private req {:client-id "client-1"})

(deftest ok-non-shadowed-rule
  (let [st (fresh-store)
        v (governor/check req {} (add-rule {:action :allow
                                            :src-zones #{"lan"}
                                            :dst-zones #{"db"}}) st)]
    (is (:ok? v))))

(deftest ok-when-existing-rule-is-narrower
  (testing "an earlier narrower rule does not shadow a broader addition"
    (let [st (fresh-store)]
      (store/register-rule! st {:rule-id "R-1" :client-id "client-1" :order 1
                                :action :allow :src-zones #{"lan"}
                                :dst-zones #{"db"}})
      (let [v (governor/check req {} (add-rule {:action :allow
                                                :src-zones #{"lan" "dmz"}
                                                :dst-zones #{"db"}}) st)]
        (is (:ok? v))))))

(deftest hard-on-redundant-shadow
  (testing "same action + containing match sets = redundant dead config"
    (let [st (fresh-store)]
      (store/register-rule! st {:rule-id "R-1" :client-id "client-1" :order 1
                                :action :allow :src-zones #{"lan" "dmz"}
                                :dst-zones #{"db" "mgmt"}})
      (let [v (governor/check req {} (assoc (add-rule {:action :allow
                                                       :src-zones #{"lan"}
                                                       :dst-zones #{"db"}})
                                            :confidence 0.99) st)]
        (is (:hard? v))
        (is (some #(= :shadowed-rule (:rule %)) (:violations v)))))))

(deftest hard-on-contradicting-shadow
  (testing "different action + containing match sets = the earlier rule always wins"
    (let [st (fresh-store)]
      (store/register-rule! st {:rule-id "R-1" :client-id "client-1" :order 1
                                :action :deny :src-zones #{"lan" "dmz"}
                                :dst-zones #{"db"}})
      (let [v (governor/check req {} (add-rule {:action :allow
                                                :src-zones #{"lan"}
                                                :dst-zones #{"db"}}) st)]
        (is (:hard? v))
        (is (some #(= :shadowed-rule (:rule %)) (:violations v)))))))

(deftest hard-on-unknown-zone
  (let [st (fresh-store)
        v (governor/check req {} (add-rule {:action :allow
                                            :src-zones #{"lan" "ghost"}
                                            :dst-zones #{"db"}}) st)]
    (is (:hard? v))
    (is (some #(= :unknown-zone (:rule %)) (:violations v)))))

(deftest hard-on-foreign-zone
  (let [st (fresh-store)]
    (store/register-client! st {:client-id "client-2" :name "Other"})
    (store/register-zone! st {:zone-id "other-lan" :client-id "client-2"
                              :name "other-lan"})
    (let [v (governor/check req {} (add-rule {:action :allow
                                              :src-zones #{"other-lan"}
                                              :dst-zones #{"db"}}) st)]
      (is (:hard? v))
      (is (some #(= :zone-wrong-client (:rule %)) (:violations v))))))

(deftest hard-on-missing-rule-body
  (let [st (fresh-store)
        v (governor/check req {} {:op :add-rule :effect :propose
                                  :confidence 0.9 :stake :low} st)]
    (is (:hard? v))
    (is (some #(= :no-rule (:rule %)) (:violations v)))))

(deftest hard-on-unregistered-client
  (let [st (fresh-store)
        v (governor/check {:client-id "nobody"} {}
                          (add-rule {:action :allow :src-zones #{"lan"}
                                     :dst-zones #{"db"}}) st)]
    (is (:hard? v))
    (is (some #(= :no-client (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        v (governor/check req {} (assoc (add-rule {:action :allow
                                                   :src-zones #{"lan"}
                                                   :dst-zones #{"db"}})
                                        :effect :direct-write) st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-production-apply
  (let [st (fresh-store)
        v (governor/check req {} {:op :apply-to-production :effect :propose
                                  :confidence 0.9 :stake :high} st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))

(deftest escalates-low-confidence
  (let [st (fresh-store)
        v (governor/check req {} (assoc (add-rule {:action :allow
                                                   :src-zones #{"lan"}
                                                   :dst-zones #{"db"}})
                                        :confidence 0.3) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))
