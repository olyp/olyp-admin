(ns olyp-admin.web-handler
  (:require cheshire.core
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :as strategies]
            bidi.ring
            ring.middleware.anti-forgery
            ring.middleware.session
            ring.middleware.params
            [olyp-admin.web-handlers.users-handler :as users-handler]
            [olyp-admin.web-handlers.central-api-proxy-handler :as central-api-proxy-handler]))

(defn first-step-optimizations [assets options]
  (-> assets
      (optimizations/minify-js-assets options)
      (optimizations/minify-css-assets options)
      (optimizations/inline-css-imports)))

(defn second-step-optimizations [assets options]
  (-> assets
      (optimizations/concatenate-bundles)
      (optimizations/add-cache-busted-expires-headers)
      (optimizations/add-last-modified-headers)))

(defn get-unoptimizable-assets [env]
  (concat
   (if (= :dev env)
     (assets/load-bundle "public" "lib.js" ["/js/lib/react-with-addons-0.12.2.js"])
     (assets/load-bundle "public" "lib.js" ["/js/lib/react-with-addons-0.12.2.min.js"]))))

(defn get-optimizable-assets []
  (concat
   (assets/load-bundle "public" "lib.js" ["/js/lib/when-3.6.4.js"
                                          "/js/olyp_app_utils/http.js"])
   (assets/load-bundle "public" "app.css" ["/bootstrap/css/bootstrap.css"
                                           "/bootstrap/css/bootstrap-theme.css"
                                           "/css/app.css"])
   (assets/load-bundle "public" "users.js" ["/js/users/users_components.js"
                                            "/js/users/users_store.js"
                                            "/js/users/users_actions.js"
                                            "/js/users/users.js"])))

(defn get-assets [env]
  (if (= :dev env)
    (concat
     (get-unoptimizable-assets env)
     (get-optimizable-assets))
    (-> (concat
         (get-unoptimizable-assets env)
         (-> (get-optimizable-assets)
             (first-step-optimizations {})))
        (second-step-optimizations {}))))

(defn wrap-anti-forgery-token-hack [handler]
  (fn [req]
    (handler (assoc req :anti-forgery-token ring.middleware.anti-forgery/*anti-forgery-token*))))

(defn wrap-olyp-env [handler olyp-central-api-client-ctx]
  (fn [req]
    (handler (assoc req
               :olyp-env {:api-ctx olyp-central-api-client-ctx}))))

(def app-handler
  (bidi.ring/make-handler
   [""
    {"/" {:get (fn [req] {:status 302 :headers {"Location" "/users"}})}
     "/users" #'users-handler/users-page
     "/central_api_proxy" {[[#".*" :path] ""] central-api-proxy-handler/central-api-proxy}}]))

(defn create-actual-handler [olyp-central-api-client-ctx]
  (->
   app-handler
   (wrap-olyp-env olyp-central-api-client-ctx)
   wrap-anti-forgery-token-hack
   ring.middleware.anti-forgery/wrap-anti-forgery
   ring.middleware.session/wrap-session
   ring.middleware.params/wrap-params))

(defn create-handler [{:keys [env olyp-central-api-client-ctx]}]
  ((if (= :dev env)
      strategies/serve-live-assets
      strategies/serve-frozen-assets)
   (create-actual-handler olyp-central-api-client-ctx)
   #(get-assets env)
   optimizations/none
   {}))
