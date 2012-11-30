(ns com.duelinmarkers.datomic-schema-test
  (:use clojure.test)
  (:require [com.duelinmarkers.datomic-schema :as dschema]))

(deftest attribute
  (testing "given minimal arguments"
    (let [result0 (dschema/attribute :widget/name :db.type/string)
          result1 (dschema/attribute :widget/weight :db.type/float)]

      (testing "returns a map with the required entries to create a new datomic attribute"
        (is (= #{:db/id :db/ident :db/valueType :db/cardinality :db.install/_attribute}
               (set (keys result0))
               (set (keys result1)))))

      (testing "sets ident and valueType as specified"
        (is (= [:widget/name :widget/weight] (map :db/ident [result0 result1])))
        (is (= [:db.type/string :db.type/float] (map :db/valueType [result0 result1]))))

      (testing "defaults cardinality to one"
        (is (= :db.cardinality/one (:db/cardinality result0))))

      (testing "wires up the attribute to the db partition"
        (is (= :db.part/db (:db.install/_attribute result0))))

      (testing "generates a new tempid for each attribute"
        (is (instance? datomic.db.DbId (:db/id result0)))
        (is (not= (:db/id result0) (:db/id result1))))))

  (testing "overriding cardinality"
    (let [result (dschema/attribute :widget/tags :db.type/string :db.cardinality/many)]
      (is (= :db.cardinality/many (:db/cardinality result)))))

  (testing "specifying a doc string"
    (let [result (dschema/attribute :widget/name :db.type/string "Widget name")]
      (is (= "Widget name" (:db/doc result)))))

  (testing "specifying uniqueness"
    (let [result (dschema/attribute :widget/name :db.type/string :db.unique/value)]
      (is (= :db.unique/value (:db/unique result)))))

  (testing "specifying boolean options"
    (let [result (dschema/attribute :widget/name :db.type/string :db/index :db/fullText :db/isComponent :db/noHistory)]
      (is (= true (:db/index result)))
      (is (= true (:db/fullText result)))
      (is (= true (:db/isComponent result)))
      (is (= true (:db/noHistory result)))))

  (testing "after the first two, arguments can appear in any order"
    (dotimes [n 50]
      (let [extra-args [:db.cardinality/many :db.unique/identity :db/index :db/fullText :db/isComponent :db/noHistory "doc string"]
            shuffled-args (shuffle extra-args)
            result0 (apply dschema/attribute :widget/name :db.type/string extra-args)
            result1 (apply dschema/attribute :widget/name :db.type/string shuffled-args)]
        (is (= (dissoc result0 :db/id) (dissoc result1 :db/id)) (str "Shuffled args were " shuffled-args))))))

(deftest enum-value
  (testing "given just a keyword"
    (let [result (dschema/enum-value :widget.type/up)]
      (testing "generates an add command vector"
        (is (= [:db/add :ignored-db-id :db/ident :widget.type/up] (assoc result 1 :ignored-db-id))))
      (testing "generates a tempid in the user partition"
        (let [db-id (result 1)]
        (is (= :db.part/user (:part db-id)))
        (is (neg? (:idx db-id)))))))

  (testing "given a partition and keyword"
    (let [result (dschema/enum-value :db.part/widgetry :widget.type/up)]
      (testing "generates an add command vector"
        (is (= [:db/add :ignored-db-id :db/ident :widget.type/up] (assoc result 1 :ignored-db-id))))
      (testing "generates a tempid in the given partition"
        (let [db-id (result 1)]
        (is (= :db.part/widgetry (:part db-id)))
        (is (neg? (:idx db-id))))))))
