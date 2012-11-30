# datomic-schema

A Clojure library providing convenient fns for building up a datomic schema.

## Usage

    (require '[com.duelinmarkers.datomic-schema :as dschema]
             '[datomic.api :as d])
    
    (d/transact [
      (dschema/attribute :widget/name :db.type/string :db/fullText :db.unique/identity "The name")
      (dschema/attribute :widget/price :db.type/bigdec "The unit price")
      (dschema/attribute :widget/type :db.type/ref)
      (dschema/enum-value :widget.type/up)
      (dschema/enum-value :widget.type/down)])

## License

Copyright © 2012 John D. Hume

Distributed under the Eclipse Public License, the same as Clojure.
