from django.conf.urls.defaults import patterns, include, url
from django.conf.urls.static import static
from django.conf import settings

from django.contrib import admin
admin.autodiscover()

if settings.USER_LOCALHOST:
	urlpatterns = patterns('',
			       url(r'^$', 'incunabula.views.index'), #todo portal
			       url(r'^knowevo/$', 'incunabula.views.index'), #todo portal

			       url(r'^knowevo/incunabula/$', 'incunabula.views.index'),
			       url(r'^knowevo/incunabula/(?P<article_id>\d+)/$', 'incunabula.views.article_detail'),
			       url(r'^knowevo/incunabula/(?P<master_name>.+?)/$', 'incunabula.views.master_detail'),
			       
			       #gravebook links
			       url(r'^knowevo/gravebook/$', 'gravebook.views.index'),
			       url(r'^knowevo/gravebook/Category:(?P<article_name>.+?)/_(?P<id>.+?)/$', 'gravebook.views.load_article_data'), 
			       url(r'^knowevo/gravebook/Category:(?P<category_name>.+?)/$', 'gravebook.views.category_detail'), 
			       url(r'^knowevo/gravebook/(?P<article_name>.+?)/_(?P<id>.+?)/$', 'gravebook.views.load_article_data'), 
			       url(r'^knowevo/gravebook/(?P<article_name>.+?)/_spring_box/$', 'gravebook.views.load_spring_box'),
			       url(r'^knowevo/gravebook/(?P<article_name>.+?)/$', 'gravebook.views.article_detail'),

			       
			       # Uncomment the admin/doc line below to enable admin documentation
			       url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
			       url(r'^admin/', include(admin.site.urls)),
)+(
		static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)+
		static(settings.STATIC_URL, document_root=settings.STATIC_ROOT))


#for Apache deployment
else:
    urlpatterns = patterns('',
                           url(r'^$', 'incunabula.views.index'), #todo make general portal page

			   #gravebook links
			   url(r'^gravebook/$', 'gravebook.views.index'),
			   url(r'^gravebook/Category:(?P<article_name>.+?)/_(?P<id>.+?)/$', 'gravebook.views.load_article_data'), 
			   url(r'^gravebook/Category:(?P<category_name>.+?)/$', 'gravebook.views.category_detail'), 
			   url(r'^gravebook/(?P<article_name>.+?)/_(?P<id>.+?)/$', 'gravebook.views.load_article_data'), 
			   url(r'^gravebook/(?P<article_name>.+?)/_spring_box/$', 'gravebook.views.load_spring_box'),
			   url(r'^gravebook/(?P<article_name>.+?)/$', 'gravebook.views.article_detail'),

                           url(r'^incunabula/$', 'incunabula.views.index'),
                           url(r'^incunabula/(?P<article_id>\d+)/$', 'incunabula.views.article_detail'),
                           url(r'^incunabula/(?P<master_name>.+?)/$', 'incunabula.views.master_detail'),
			       
                           url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
                           url(r'^admin/', include(admin.site.urls)),
) + (
	    static(settings.MEDIA_URL.split('knowevo')[1], document_root=settings.MEDIA_ROOT)+
	    static(settings.STATIC_URL.split('knowevo')[1], document_root=settings.STATIC_ROOT))
