from django.http import HttpResponse
from gravebook.models import Article, Category
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response
from django.http import HttpResponse

from incunabula.models import Article as IArticle
from incunabula.views import get_master_alist
import settings

import gravebook.graphConnect as gcon
from spring.timeser import prep_time_series_chart

import re, md5, math, urllib2


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

def prep_img_url(art):
    img = None
    if art.image != 'null':
        img = art.image.replace(' ', '_')
        img = img.split('|')[0]
        digest = md5.new(img).hexdigest()
        img = digest[0]+'/'+digest[:2]+'/'+img

        base = 'http://upload.wikimedia.org/wikipedia/'
        url = base+'commons/'+img
        try:
            f = urllib2.urlopen(url)
            f.close()
            return url
        except:
            pass

        url = base+'en/'+img
        try:
            f = urllib2.urlopen(url)
            f.close()
            return url
        except:
            pass


def article_detail(request, article_name):
    art = Article.objects.get(name=article_name)
    img = prep_img_url(art)
    
    OVERLAP = 15

    influences = []
    influenced = []
    peers = []
    people = art.people.all()
    for person in people:
        if person.birth == -1 or person.death == -1:
            continue
        
        if person.death-art.birth > OVERLAP and art.death-person.birth > OVERLAP:
            peers.append(person)

        elif art.birth > person.death:
            influences.append(person)
        elif art.death < person.birth:
            influenced.append(person)
            
    chart = None
    res, res_matches = get_master_alist(
        '_'.join(article_name.split(' ')))
    num = len(res_matches)
    if num > 1:
        chart = prep_time_series_chart(res_matches)


    '''art_to = art.people.all()[0]
    score = Article.objects.filter(name__in=art_to.linked_by.iterator).filter(name__in=art.linked_by.iterator).count()
    print 'scorew', score
    '''

    return render_to_response('gravebook/article_detail.html',
                              { 'article':     art, 
                                'image':       img,
                                'categories':  art.categories.all(),
                                'peers':       peers,
                                'influences':  influences,
                                'influenced':  influenced,                
                                'evo_chart':   chart,
                                },
                              RequestContext(request))

def load_spring_box(request, article_name):
    print 'loading sbox'
    local = 'local'
    if not settings.USER_LOCALHOST:
        local = 'remote'
    return HttpResponse(''+
                        '<applet\n'+
                        'code="knowevo.springbox.vizster.VizsterApplet"\n'+

                        'archive = "/knowevo/static/knowevo.jar,'+
                        '/knowevo/static/prefuse.jar,'+
                        '/knowevo/static/gephi-toolkit.jar,'+
                        '/knowevo/static/vizster.jar"\n'+

                        'width = 900\n'+
                        'height = 659>\n'+
                        '<param name="article_name" value="'+article_name+'" />\n'+
                        '<param name="server_address" value="'+local+'" />\n'+
                        '<param name="port" value="62541" />\n'+
                        '</applet>'
                        )


def category_detail(request, category_name):
    cat = Category.objects.get(name=category_name)
    articles = cat.article_set.all()
    
    return render_to_response('gravebook/category_detail.html',
                              {'articles':articles,
                               'category_name': category_name},
                              RequestContext(request))

    
