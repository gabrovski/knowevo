from django.http import HttpResponse
from incunabula.models import MasterArticle, Match, Article
from django.template import RequestContext, Context, loader
from django.shortcuts import render_to_response

EMPTY_ART = {'name':'#NA'}

def get_master_alist(master):
    res_m = [EMPTY_ART for x in xrange(4)]
    matches = Match.objects.filter(article = master.id)

    for match in matches:
        art = Article.objects.get(id=match.match_id)
        if art.art_ed == 3:  res_m[0] = art
        if art.art_ed == 9:  res_m[1] = art
        if art.art_ed == 11: res_m[2] = art
        if art.art_ed == 15: res_m[3] = art
    return res_m

def index(request):
    if request.method == 'POST':
        masters = MasterArticle.objects.filter(name__icontains=str(request.POST['title_inp']))
        res = []
        for master in masters:
            res_m = get_master_alist(master)
            res.append((master.name, res_m))
        return render_to_response('incunabula/index.html', 
                                  {'master_arts':res}, 
                                  context_instance=RequestContext(request))
    else:
        return render_to_response('incunabula/index.html', 
                                  {'master_arts':[]}, 
                                  RequestContext(request))

def master_detail(request, master_name):
    master = MasterArticle.objects.get(name=master_name)
    res = get_master_alist(master)
    return render_to_response('incunabula/master_detail.html',
                       {'master_name':master_name, 'arts':res},
                       RequestContext(request))

def article_detail(request, article_id):
    art = Article.objects.get(id=article_id)
    return render_to_response('incunabula/article_detail.html',
                              {'article':art},
                              RequestContext(request))
