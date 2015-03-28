#######################################################################
# Copyright © 2015 The CoDoLa developer team
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
# USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#######################################################################

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
