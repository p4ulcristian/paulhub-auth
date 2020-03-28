FROM openfaas/of-watchdog:0.7.6 as watchdog

FROM clojure:openjdk-14-lein-2.9.1-alpine as preparebuild
WORKDIR /root/
COPY project.clj project.clj
RUN lein deps

FROM oracle/graalvm-ce:latest as graal-builder
WORKDIR /root
COPY --from=preparebuild /root/.m2 /root/.m2
COPY src src
RUN gu install native-image
COPY project.clj project.clj
COPY keys keys
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/bin/lein
RUN chmod +x /usr/bin/lein
RUN lein self-install
RUN lein native-image


FROM ubuntu
WORKDIR /root/
COPY --from=graal-builder /root/target/server /root/
COPY --from=watchdog /fwatchdog /usr/bin/fwatchdog
COPY keys keys
RUN mkdir -p /home/app
RUN chmod +x /usr/bin/fwatchdog
ENV read_timeout="1800"
ENV write_timeout="1800"
ENV upstream_url="http://127.0.0.1:3000"
ENV mode="http"
ENV fprocess="./server"
ENV write_debug="true"
EXPOSE 8080
HEALTHCHECK --interval=5s CMD [ -e /tmp/.lock ] || exit 1
CMD ["fwatchdog"]