#
# codola Dockerfile
#
# https://github.com/phil-pona/codola
#
FROM dockerfile/java:openjdk-7-jdk
ADD src/main/resources/shell/texlive.profile /tmp/
MAINTAINER "Thomas Philipona" <philipona@puzzle.ch>

# Prerequisites
RUN apt-get -qqy install build-essential wget

# Set Installation directory
ENV latexFolder /usr/local/texlive
RUN sed -i "s|\\\${latexFolder}|"$latexFolder"|g" /tmp/texlive.profile

# TeXLive
RUN wget http://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz \
&& tar -xvf install-tl-unx.tar.gz \ 
&& cd install-tl-* \
&& ./install-tl -profile /tmp/texlive.profile
ENV PATH /usr/local/texlive/bin/i386:/usr/local/texlive/bin/x86_64-linux:$PATH
RUN tlmgr install latexmk

# Uncomment as soon as the application is added
#EXPOSE 8080
