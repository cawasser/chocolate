(ns chocolate.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [chocolate.core-test]))

(doo-tests 'chocolate.core-test)

