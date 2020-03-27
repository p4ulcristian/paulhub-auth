FROM openfaas/of-watchdog:0.7.6 as watchdog

FROM clojure:openjdk-14-lein-2.9.1-alpine as preparebuild
WORKDIR /root/
COPY project.clj project.clj
RUN lein deps

FROM clojure:openjdk-14-lein-2.9.1-alpine as clojure-builder
WORKDIR /root/
COPY --from=preparebuild /root/.m2 /root/.m2
COPY src src
COPY project.clj project.clj
#RUN apk add --update nodejs npm
#RUN npm install --only=production
RUN lein uberjar && mv target/paulhub-auth-latest-standalone.jar ./
RUN apk add bash

FROM oracle/graalvm-ce:latest as graal-builder
WORKDIR /root
COPY --from=clojure-builder /root/paulhub-auth-latest-standalone.jar /root/
RUN gu install native-image
RUN native-image \
    -H:EnableURLProtocols=http \
    -H:EnableURLProtocols=https \
    --report-unsupported-elements-at-runtime \
    --initialize-at-build-time \
    --no-server \
    --verbose \
    --static \
    -cp build/libs/complete-*-all.jar \
    -jar ./paulhub-auth-latest-standalone.jar

FROM frolvlad/alpine-glibc
RUN apk update && apk add libstdc++
COPY --from=graal-builder /root/paulhub-auth-latest-standalone /root/
COPY --from=watchdog /fwatchdog /usr/bin/fwatchdog
#RUN apk add bash
WORKDIR /root/
RUN mkdir -p /home/app
RUN chmod +x /usr/bin/fwatchdog
RUN apk add bash
ENV read_timeout="1800"
ENV write_timeout="1800"
ENV upstream_url="http://127.0.0.1:3000"
ENV mode="http"
ENV fprocess="./paulhub-auth-latest-standalone"
ENV write_debug="true"
EXPOSE 8080
HEALTHCHECK --interval=5s CMD [ -e /tmp/.lock ] || exit 1
CMD ["fwatchdog"]