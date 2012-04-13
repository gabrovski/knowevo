from django.http import HttpResponse
from gravebook.models import Article, Category
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response

from spring.force import SpringBox

import re, md5, math


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

    OVERLAP = 15

    influences = []
    influenced = []
    peers = []
    people = art.people.all()
    for person in people:
        if person.birth == -1 or person.death == -1:
            continue
        
        if art.birth > person.death:
            influences.append(person)
        elif art.death < person.birth:
            influenced.append(person)
        elif (math.fabs(person.birth-art.birth) > OVERLAP or 
              math.fabs(person.death-art.death) > OVERLAP):
            peers.append(person)
        
    print 'beginning spring box'
    sb = springbox(art)
    names = set(map(lambda x: x.name, sb.objects))
    for o in sb.objects:
        o.save()
               
    chart = SpringBox.get_chart(Article.objects.filter(name__in=names))

    return render_to_response('gravebook/article_detail.html',
                              { 'article':     art, 
                                'image':       img,
                                'categories':  art.categories.all(),
                                'peers':       peers,
                                'influences':  influences,
                                'influenced':  influenced,
                                'springchart': chart,
                                },
                              RequestContext(request))

def category_detail(request, category_name):
    cat = Category.objects.get(name=category_name)
    articles = cat.article_set.all()
    
    return render_to_response('gravebook/category_detail.html',
                              {'articles':articles,
                               'category_name': category_name},
                              RequestContext(request))

def _get_cat_score(u,v):
    common_cats = u.categories.filter(name__in=(
            list(v.categories.values_list('name'))))

    score = 0
    for cat in common_cats:
        score += 1.0 / cat.size
    return score

def _get_neighbors(art, max_depth):
    res = []
    queue = [(art,0)]
    seen = set([art.name])
    ndict = dict()
    
    while len(queue) > 0:
        curr, d = queue.pop(0)
        if d > max_depth:
            break
        
        res.append(curr)
    
        for p in curr.people.all():
            if curr.name not in ndict or p.name not in ndict[curr.name]:
                SpringBox.update_ndict(ndict, curr, p, 1)
                #_get_cat_score(curr, p))

            if p.name not in seen:
                seen.add(p.name)
                queue.append((p, d+1))

    return res, ndict
        

def springbox(art, max_depth=1):
    objects, ndict = _get_neighbors(art, max_depth)
    print 'retrieved neighbors', len(objects)
    
    sb = SpringBox(objects=objects, width=300, height=300, 
                   charge=2, mass=1, time_step=0.05, 
                   kfn=lambda x, y: SpringBox.kfn(ndict, x, y))

    sb.move_to_equillibrium( len(objects) )
    print 'acheived equillibrium'

    #sb.print_objects()
    return sb
    
