from django.http import HttpResponse
from gravebook.models import Article
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response

import re, md5


def index(request):
    articles = []
    if request.method == 'POST':
        title_words = filter(lambda x: len(x) > 0, 
                             request.POST['title_inp'].split(' '))
        if len(title_words) > 0:
            articles = Article.objects.filter(name__icontains=title_words[0])
            for k in title_words[1:]:
                articles = articles.filter(name__icontains=k)
    
    return render_to_response('gravebook/index.html',
                              {'articles':articles},
                              RequestContext(request))

def article_detail(request, article_name):
    art = Article.objects.get(name=article_name)
    img = art.image.replace(' ', '_')
    digest = md5.new(img).hexdigest()
    img = digest[0]+'/'+digest[:2]+'/'+img
    return render_to_response('gravebook/article_detail.html',
                              { 'article':art, 'image':img,
                                'peers':art.peers.all(),
                                'influences':art.influences.all(),
                                'influenced':art.influenced.all() },
                              RequestContext(request))
            
        
        
