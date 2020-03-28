(ns auth.core
  (:require
   [org.httpkit.server :as server]
   [buddy.sign.jwt :as jwt]
   [buddy.core.keys :as keys]
   [hiccup.page :refer [include-js include-css html5]]
   [reitit.ring :as reitit-ring]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.reload :refer [wrap-reload]])
  ;(:import (sun.util.logging PlatformLogger PlatformLogger$Level))
  (:gen-class))

(defonce users
         [{:user "paul"
           :pass "paul"}
          {:user "bela"
           :pass "bela"}
          {:user "admin"
           :pass "bela"}])

;; Create keys instances
(def auth-private-key (keys/private-key "keys/auth-private-key.pem"))
(def auth-public-key (keys/public-key "keys/auth-public-key.pem"))
(def refresh-private-key (keys/private-key "keys/refresh-private-key.pem"))
(def refresh-public-key (keys/public-key "keys/refresh-public-key.pem"))
;; Use them like plain secret password with hmac algorithms for sign


(defn sign-data [private-key payload]
      (jwt/sign payload private-key {:alg :es256}))

(defn unsign-data [public-key signed-data]
      (jwt/unsign signed-data public-key {:alg :es256}))



(defn make-tokens [payload]
      {:auth-token (sign-data auth-private-key payload)
       :refresh-token (sign-data refresh-private-key payload)})


(defn html-head [req]
      [:head
       [:title "(paulhub-auth)"]
       ;[:link {:rel "icon" :href "/favicon.png" :type "image/png"}]
       [:meta {:charset "utf-8"}]
       [:meta {:name "viewport"
               :content "width=device-width, initial-scale=1"}]])
(defn html-page [req]
        "The client's page"
        (html5
          (html-head req)
          [:body {:class "body-container"}
           [:h2 "paulhub-auth"]
           [:h3 "Authentication api."]]))
(defn request-wrap [status content-type body]
       "wrap request with status and headers"
       {:status status
        :headers {"Content-Type" content-type}
        :body body})
(defn html-wrap [content]
        "Wrap Html"
        (request-wrap 200 "text/html" content))
(defn text-wrap [content]
      "Wrap Html"
      (request-wrap 200 "text/plain" content))
(defn edn-wrap [content]
      "Wrap Html"
      (request-wrap 200 "application/edn" content))


(defonce all-refresh-tokens (atom {}))

(defn add-token [token]
      (reset! all-refresh-tokens (assoc @all-refresh-tokens token {:random "data"})))

(defn remove-token [token]
      (reset! all-refresh-tokens (dissoc @all-refresh-tokens token))
      "successful logout")

(def app
    (reitit-ring/ring-handler
      (reitit-ring/router
        [["/" {:get {:handler (fn [req] (html-wrap (html-page req)))}}]
         ["/api"
           ["/public-key" {:get {:handler (fn [req] (text-wrap (str auth-public-key)))}}]
           ["/login" {:post {:handler (fn [req]
                                          (text-wrap
                                            (str (let [tokens (make-tokens
                                                                {:user "Paul" :permissions ["every" "thing"]})]
                                                    (add-token (:refresh-token tokens))
                                                    tokens))))}}]
;
           ["/logout" {:get {:handler (fn [req]
                                          (text-wrap
                                            (remove-token (:refresh-token (:params req)))))}}]]])
      (reitit-ring/routes
        (reitit-ring/create-resource-handler {:path "/" :root "/public"})
        (reitit-ring/create-default-handler))
      {:middleware
       [wrap-keyword-params
        wrap-params]}))
        ;wrap-reload]}))


(defn -main [& args]
  (println "Hello, Web!")
  (server/run-server app {:port 3000}))
