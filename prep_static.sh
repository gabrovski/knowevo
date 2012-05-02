#!/bin/sh

echo "obtaining gephi" ;
cd static ;
wget https://launchpad.net/gephi/toolkit/toolkit-0.8.5/+download/gephi-toolkit-0.8.5-all.zip ;
unzip gephi-toolkit-0.8.5-all.zip ;
cp gephi-toolkit-0.8.1-all/gephi-toolkit.jar . ;

echo "gephi's there. use netbeans to add the jar in static to the GraphDrawerProject and build it using ant jar. start the server by running GraphDrawer's jar file" ;

echo "getting js files" ;
mkdir js ;
wget -P js http://code.jquery.com/jquery-1.7.2.min.js ;
wget -P js http://code.jquery.com/jquery-1.7.2.js ;
wget -P js http://www.highcharts.com/downloads/zips/Highcharts-2.2.1.zip

echo "adding necessary categories" ;
mkdir stroot ;
mkdir pngs ;

cd .. ;
echo "ensure that you have the proper python loaded "
echo "collecting static files into stroot. make sure that settings is configure properly in terms of absolute paths " ;
python manage.py collectstatic ;

