FROM babashka/babashka:1.12.194

ENV WORKDIR=/app
WORKDIR ${WORKDIR}

COPY bb.edn LICENSE ${WORKDIR}/
COPY src ${WORKDIR}/src

ENTRYPOINT ["bb", "-o", "--main", "map-tile-metrics.main"]
CMD ["--help"]