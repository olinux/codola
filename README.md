# CoDoLa - Continuous Documentation with LaTeX
This project builds the technical fundaments for a solution towards an enterprise-wide continuous documentation solution based on LaTeX. It is a follow up of my talk at ["Puzzle ITC Tech Talk 2014"](http://www.puzzle.ch/de/blog/articles/2014/11/05/rueckblick-auf-den-puzzle-tech-talk-2014).

## Docker Image; work in progress

TODO: Add Application Server and Application to the Dockerfile

### Create Docker image
To Create the docker image just run 

`sudo ./build-docker-image.sh`

### render PDF

To generate the helloworld Example located in the ./src/test/resources directory you can run the following command

`sudo docker run -v [full_path_to_your_cloned_repo]/src/test/resources:/data phil_pona/codola:0.0.1 pdflatex -interaction=nonstopmode -output-directory output /data/helloworld.tex`

the result is generated into the ./src/test/resources/output directory of your host



