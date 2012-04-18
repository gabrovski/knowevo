from django.http import HttpResponse
from incunabula.models import MasterArticle, Article
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response

import re

EMPTY_ART = {'name':'#NA'}

def get_master_alist(master, keywords=[]):
    res_m = [EMPTY_ART for x in xrange(4)]
    matches = Article.objects.filter(match_master=master)
    
    kword_match = (len(keywords) == 0)
    pats = [re.compile(keyword+'(?i)') for keyword in keywords]        

    for art in matches:
        
        #filter by anding keywords
        if not kword_match:
            curr = True
            for pat in pats:
                curr = curr and (pat.search(art.text)!=None)
            kword_match = kword_match or curr

        if art.art_ed == 3:  res_m[0] = art
        if art.art_ed == 9:  res_m[1] = art
        if art.art_ed == 11: res_m[2] = art
        if art.art_ed == 15: res_m[3] = art
        
    if kword_match:
        return res_m, len(matches)
    else:
        return None

def index(request):
    if request.method == 'POST':
        keywords = filter(lambda x: len(x) > 0, 
                          str(request.POST['keyword_inp']).split(' '))
        master_words = filter(lambda x: len(x) > 0, 
                              str(request.POST['title_inp']).split(' '))

        if master_words != []:
            #get masters by specified name, and title words
            masters = MasterArticle.objects.filter(
                name__icontains=master_words[0])

            for mw in master_words[1:]:
                masters = masters.filter(name__icontains=mw)

        else: #empty title, will take a while
            masters = MasterArticle.objects.all()

        res = []
        for master in masters:
            res_m, num = get_master_alist(master, keywords)
            if res_m != None: res.append((master.name, res_m, num))
        
        #print res
        res.sort(key=lambda x: x[2], reverse=True)
        return render_to_response('incunabula/index.html', 
                                  {'master_arts':res}, 
                                  context_instance=RequestContext(request))

    else: #nothing submitted
        return render_to_response('incunabula/index.html', 
                                  {},
                                  RequestContext(request))

def master_detail(request, master_name):
    master = MasterArticle.objects.get(name=master_name)
    res, num = get_master_alist(master)
    return render_to_response('incunabula/master_detail.html',
                       {'master_name':master_name, 'arts':res},
                       RequestContext(request))

def article_detail(request, article_id):
    art = Article.objects.get(id=article_id)
    return render_to_response('incunabula/article_detail.html',
                              {'article':art},
                              RequestContext(request))
