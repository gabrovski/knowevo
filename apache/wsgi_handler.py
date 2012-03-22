import site
activate_this = '/home/sasho/cs/venv/bin/activate_this.py'
execfile(activate_this, dict(__file__=activate_this))

#site.addsitedir('/home/sasho/venv/lib/python2.7/site-packages')

import os, sys

#sys.path.insert(0, '/home/sasho/cs/venv/bin/python')

sys.path.append('/home/sasho/cs/knowevo')
sys.path.append('/home/sasho/cs')
os.environ['DJANGO_SETTINGS_MODULE'] = 'knowevo.settings'

import django.core.handlers.wsgi
application = django.core.handlers.wsgi.WSGIHandler()
