(ns com.duelinmarkers.datomic-schema
  (:require [datomic.api :refer (tempid)]))

(defn- key-for-value [value]
  (if-let [[_ key-name] (re-find #"^db\.(\w+)$" (namespace value))]
    (keyword "db" key-name)
    (throw (IllegalArgumentException. (str "Can't use " value)))))

(defn attribute
  "Builds a transaction map for defining an attribute named by ident with
  :db/valueType given by type and a generated temporary db id.

  Additional optional args may appear in any order and are
    :db.cardinality/many or :db.cardinality/one (the default),
    :db.unique/identity or :db.unique/value,
  and any combination of
    :db/index, :db/fullText, :db/isComponent, :db/noHistory,
  and a doc string.

  See http://docs.datomic.com/schema.html for allowed values of type and
  meanings of other arguments."
  [ident type & more]
  (reduce #(cond
             (keyword? %2) (if (= (namespace %2) "db")
                             (assoc %1 %2 true)
                             (assoc %1 (key-for-value %2) %2))
             (instance? String %2) (assoc %1 :db/doc %2)
             :else (throw (IllegalArgumentException. (str "Can't use " %2))))
          {:db/id (tempid :db.part/db)
           :db/ident ident
           :db/valueType type
           :db/cardinality :db.cardinality/one
           :db.install/_attribute :db.part/db}
          more))

(defn enum-value
  ([value]
   (enum-value :db.part/user value))
  ([db-part value]
   [:db/add (tempid db-part) :db/ident value]))
