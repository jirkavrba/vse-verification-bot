FROM mozilla/sbt

RUN mkdir /app
COPY . /app
WORKDIR /app

CMD ["sbt", "run"]
