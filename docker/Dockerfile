FROM debian
MAINTAINER Andrew Williams <andrew@headsupdev.com>

RUN apt-get update
RUN apt-get upgrade -y

#java
RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" > /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" >> /etc/apt/sources.list.d/webupd8team-java.list
RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
RUN apt-get update
RUN apt-get install -y oracle-java8-installer

#HeadsUp Agile
RUN apt-get install -y curl
RUN curl https://s3-eu-west-1.amazonaws.com/agile-releases/agile-2.1.tar.gz > agile-2.1.tar.gz
RUN tar xf agile-2.1.tar.gz

#Setup DB
RUN sed -i 's/${user.home}\/.headsupagile\/data/\/agile-data\/db/g' /agile-2.1/conf/config.properties

VOLUME /agile-data/repository/
EXPOSE 8069

CMD /agile-2.1/bin/agile.sh

