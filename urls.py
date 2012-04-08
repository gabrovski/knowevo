from django.conf.urls.defaults import patterns, include, url

from django.contrib import admin
admin.autodiscover()

USER_LOCALHOST = True

if USER_LOCALHOST:
	urlpatterns = patterns('',
			       url(r'^$', 'incunabula.views.index'),
			       url(r'^incunabula/$', 'incunabula.views.index'),
			       url(r'^incunabula/(?P<article_id>\d+)/$', 'incunabula.views.article_detail'),
			       url(r'^incunabula/(?P<master_name>.+?)/$', 'incunabula.views.master_detail'),
			       
			       #gravebook links
			       url(r'^gravebook/$', 'gravebook.views.index'),
			       url(r'^gravebook/(?P<article_name>.+?)/$', 'gravebook.views.article_detail'),
			       
			   
			       # Uncomment the admin/doc line below to enable admin documentation
			       url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
			       url(r'^admin/', include(admin.site.urls)),
)

#for Apache deployment
else:
    urlpatterns = patterns('',
                           url(r'^$', 'incunabula.views.index'),

			   #gravebook links
			   url(r'^gravebook/$', 'gravebook.views.index'),
			   url(r'^gravebook/(?P<article_name>.+?)/$', 'gravebook.views.article_detail'),

                           url(r'^(?P<article_id>\d+)/$', 'incunabula.views.article_detail'),
                           url(r'^(?P<master_name>.+?)/$', 'incunabula.views.master_detail'),
			       
                           url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
                           url(r'^admin/', include(admin.site.urls)),
)
