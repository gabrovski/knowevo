#!/bin/sh

echo "getting jars" ;
wget http://hacktown.cs.dartmouth.edu/~sasho/libs.tar.gz ;
tar xfvz libs.tar.gz ;
mv lib jar/lib ;

echo "getting required js files" ;
wget http://hacktown.cs.dartmouth.edu/~sasho/js.tar.gz ;
tar xfvz js.tar.gz ;
mv js static/js ;

echo "ensure that you have the proper python loaded "
echo "collecting static files into stroot. make sure that settings is configure properly in terms of absolute paths " ;
python manage.py collectstatic ;

