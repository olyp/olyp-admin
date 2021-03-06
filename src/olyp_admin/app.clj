(ns olyp-admin.app
  (:require [com.stuartsierra.component :as component]
            [olyp-admin.components.web :as web]
            [olyp-app-utils.components.olyp-central-api-client-ctx :as olyp-central-api-client-ctx]))

(defn create-system [{:keys [olyp-central-api web env cookie-secret]}]
  (component/system-map
   :env env
   :cookie-secret cookie-secret
   :olyp-central-api-client-ctx (olyp-central-api-client-ctx/create-ctx olyp-central-api)
   :web (component/using
         (web/create-web web)
         [:env :olyp-central-api-client-ctx :cookie-secret])))
