from django.http import HttpResponse
from gravebook.models import Article, Category
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response
from django.http import HttpResponse

#from incunabula.models import Article as IArticle
#from incunabula.views import get_master_alist
import settings

import gravebook.graphConnect as gcon
from spring.timeser import prep_time_series_chart

import re, md5, math, urllib2

WIKI_ED = 1000

def index(request):
    articles = []
    searched = False
    if request.method == 'POST':
        searched = True
        title_words = filter(lambda x: len(x) > 0, 
                             request.POST['title_inp'].split(' '))

        if len(title_words) > 0:
            articles = Article.objects.filter(name__icontains=title_words[0], art_ed=WIKI_ED)
            for k in title_words[1:]:
                articles = articles.filter(name__icontains=k)
            
        #get all articles
        elif settings.USER_LOCALHOST:
            articles = Article.objects.filter(art_ed=WIKI_ED)

    #optimize history results by caching foregin keys
    articles =  articles.select_related().order_by('-match_count').iterator()

    return render_to_response('gravebook/index.html',
                              {'sarticles':articles, 
                               'searched':searched},
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
        headers={'User-Agent':"Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11"}
        try:
            r = urllib2.Request(url, headers=headers)
            f = urllib2.urlopen(r)
            f.close()
            return url
        except:
            #print url
            pass

        url = base+'en/'+img
        try:
            r = urllib2.Request(url, headers=headers)
            f = urllib2.urlopen(r)
            f.close()
            return url
        except:
            #print url
            pass


def article_detail(request, article_name):
    art = Article.objects.get(name=article_name)
    img = prep_img_url(art)
                
    chart = None
    matches = Article.objects.filter(match_master=article_name).order_by('art_ed')
    chart = prep_time_series_chart(matches)


    '''art_to = art.people.all()[0]
    score = Article.objects.filter(name__in=art_to.linked_by.iterator).filter(name__in=art.linked_by.iterator).count()
    print 'scorew', score
    '''

    return render_to_response('gravebook/article_detail.html',
                              { 'article':     art, 
                                'image':       img,
                                'evo_chart':   chart,
                                'matches':     matches,
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

def get_wiki_text(article_name):
    txt = 'Oops some error occured'
    article_name = '_'.join(article_name.split(' '))

    base = 'http://en.wikipedia.org/w/api.php'
    url = base+'?action=query&titles='+article_name+'&format=xml&prop=revisions&rvprop=content&redirects'
    
    try:
        f = urllib2.urlopen(url)
        txt = f.read()
        f.close()
        
        txt = re.sub('\{\{.+?\}\}(?s)', '', txt)
        txt = re.sub('^<.+?\'\'\'(?s)|<.+?>', '', txt)
        txt = re.sub('\|.*?\]\]', ']]', txt)
        txt = re.sub('[\[\]]', '', txt)
        txt = txt[:2000]+'... '

    except:
        raise
    
    return txt


def load_article_data(request, article_name, id):
    art = Article.objects.get(name=article_name) 
    items = []
    istext = False
    prefix = ''
    ed = None
    if id == 'peers_div':
        items = art.peers.iterator()

    elif id == 'influenced_by_div':
        for person in art.people.iterator():
            if  person.death == -1:
                continue
            elif art.birth > person.death:
                items.append(person)

    elif id == 'influences_div':
        for person in art.people.iterator():
            if  person.birth == -1:
                continue
            elif art.death < person.birth:
                items.append(person)

    elif id == 'categories_div':
        prefix = 'Category:'
        items = art.categories.iterator()

    elif 'text_' in id:
        ed = int(id.split('_')[2])
        istext = True

        if ed == WIKI_ED:
            txt = get_wiki_text(article_name)
            items.append(txt)

        elif ed == 15:
            items.append('Sorry, edition 15 text is not available for viewing')

        else:
            match = art.article_set.get(art_ed=ed)
            items.append(match.text)
    
    return render_to_response('gravebook/article_data.html',
                              {'items':items,
                               'prefix':prefix,
                               'istext':istext,
                               'texted':ed},
                              RequestContext(request))
    


def category_detail(request, category_name):
    cat = Category.objects.get(name=category_name)
    articles = cat.article_set.all()
    
    return render_to_response('gravebook/category_detail.html',
                              {'carticles':articles,
                               'category_name': category_name},
                              RequestContext(request))

    
