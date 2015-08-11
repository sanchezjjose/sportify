FROM flurdy/activator:1.3.2


COPY . /src


RUN cd /src


EXPOSE 9000


WORKDIR /src


RUN activator clean stage


CMD ["target/universal/stage/bin/sportify"]