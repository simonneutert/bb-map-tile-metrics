FROM babashka/babashka:1.4.192

ENV WORKDIR=/app
WORKDIR ${WORKDIR}

COPY bb.edn LICENSE ${WORKDIR}/
COPY src ${WORKDIR}/src

ENTRYPOINT ["bb", "-o", "--main", "map-tile-metrics.main"]
CMD ["--help"]