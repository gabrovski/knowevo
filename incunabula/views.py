from django.http import HttpResponse
from incunabula.models import MasterArticle, Article
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response

from django.core import serializers
from django.utils import simplejson as json
from django.forms.models import model_to_dict

from spring.timeser import prep_time_series_chart
from gravebook.models import Article as GArticle

import re


class Dummy:
    def __init__(self, name):
        self.name = name
EMPTY_ART = Dummy('#NA')


def get_master_alist(master, keywords=[]):
    res_m = [EMPTY_ART for x in xrange(4)]
    ms = Article.objects.filter(match_master=master)
    map = dict()
    for art in ms:
        key = str(art.art_id) + '_' + str(art.art_ed)
        if key not in map:
            map[key] = art
    
    kword_match = (len(keywords) == 0)
    pats = [re.compile(keyword+'(?i)') for keyword in keywords]        

    #not used
    for kword in keywords:
        matches = matches.filter(text__icontains=kword)

    for art in map.values():        
        if art.art_ed == 3:  res_m[0] = art
        if art.art_ed == 9:  res_m[1] = art
        if art.art_ed == 11: res_m[2] = art
        if art.art_ed == 15: res_m[3] = art
        
    if kword_match:
        return res_m, sorted(map.values(), key=lambda x: x.art_ed)
    else:
        return None

def index(request):
    if request.method == 'POST':
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
            res_m, res_matches = get_master_alist(master)
            num = len(res_matches)
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
    res, res_matches = get_master_alist(master)
    #data = json.dumps( model_to_dict(res_matches))
    print len(res_matches)
    data = serializers.serialize('json', res_matches, ensure_ascii=False, fields=['name', 'volume_score', 'art_ed', 'art_id'])

    return render_to_response('incunabula/master_detail.html',
                       {'master_name':master_name, 
                        'evo_data':data,
                        'arts':res},
                       RequestContext(request))

def article_detail(request, article_id):
    art = Article.objects.get(id=article_id)
    return render_to_response('incunabula/article_detail.html',
                              {'article':art},
                              RequestContext(request))
