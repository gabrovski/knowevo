#!/bin/sh

echo "getting jars" ;
wget http://caligari.dartmouth.edu/~agabrovs/libs.tar.gz ;
tar xfvz libs.tar.gz ;
mv lib jar/lib ;

echo "getting required js files" ;
wget http://caligari.dartmouth.edu/~agabrovs/js.tar.gz ;
tar xfvz js.tar.gz ;
mv js static/js ;

