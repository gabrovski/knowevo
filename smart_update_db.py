import re, os, sys, traceback, cPickle
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from gravebook.models import Article, Category, Other

from incunabula.models import Article as IArticle
from incunabula.models import Match  as IMatch
from incunabula.models import Reference as IReference

from django.db import transaction


tpat    = re.compile('title="(.+?)"')
idpat   = re.compile('id="(.+?)"')
imgpat  = re.compile('image="(.+?)"')
bpat    = re.compile('birth="(.+?)"')
dpat    = re.compile('death="(.+?)"')
plpat   = re.compile('people_links="(.+?)"')
olpat   = re.compile('other_links="(.+?)"')
catpat  = re.compile('categories="(.+?)"')

LIMIT = 1000
START = -1


def insert_xml(path):
    f = open(path)
    count = 0

    for line in f:
        count += 1
        if count < START:
            continue

        print count,
        
        if LIMIT > 0 and count == LIMIT:
            break

        try:
            title  = tpat.search(line).group(1)
            wid    = int(idpat.search(line).group(1))
            img    = imgpat.search(line).group(1)
            birth  = int(bpat.search(line).group(1))
            death  = int(dpat.search(line).group(1))
            #cats   = catpat.search(line).group(1).split('|')
            #others = olpat.search(line).group(1).split('|')
        
            art = Article(name=title, wid=wid, image=img, birth=birth, death=death)
            art.save()

            print title
        except:
            raise
            continue
    f.close()

def add_cats(path):
    f = open(path)
    count = 0

    for line in f:
        count += 1
        if count < START:
            continue

        print count
        
        if LIMIT > 0 and count == LIMIT:
            break

        try:
            title  = tpat.search(line).group(1)
            cats   = catpat.search(line).group(1).split('|')
            
            art = Article.objects.get(name=title)
            for cat in cats:
                if cat == '':
                    continue
                try:
                    c = Category.objects.get(name=cat)
                except:
                    c = Category(name=cat)
                    c.save()

                art.categories.add(c)
            art.save()
        except:
            continue
    f.close()


def update_category_size():
    c = 0
    for cat in Category.objects.all():
        cat.size = len(cat.article_set.all())
        cat.save()

        print c
        c+=1

def build_people_graph(path):
    f = open(path)
    count = 0

    for line in f:
        count += 1
        if count < START:
            continue

        print count
        if LIMIT > 0 and count == LIMIT:
            break

        #if count == 2:
        #   break

        try:
            title  = tpat.search(line).group(1)
            people = plpat.search(line).group(1)
        
            us = Article.objects.get(name=title)
            if us.birth == -1 or us.death == -1:
                continue

            for link in people.split('|'):
                if link == '' or link == title:
                    continue
                try:
                    them = Article.objects.get(name=link)
                except:
                    #raise
                    continue
             
                us.people.add(them)
                them.save()
                us.save()
            us.save()
        except:
            raise
            continue
    f.close()

def load(path):
    f = open(path)
    revw = cPickle.load(f)
    f.close()
    return revw


def insert_incunabula_masters(revw):
    c = 0
    for k in revw.keys():
        ma = IMasterArticle(name=k)
        ma.save()

        print c
        c+=1
        if c > 0 and c >= LIMIT:
            break
        
def insert_incunabula_articles(revw):
    c = 0
    for k in revw.keys():
        ma = IMasterArticle.objects.get(name=k)

        for ed in revw[k]['editions']:
            a = revw[k]['editions'][ed][0]

            ia = IArticle(name=a['name'], art_id=a['id'], art_ed=ed, text=a['text'],
                          prank=0.0, volume_score=0.0, match_master=ma, 
                          match_score=-1.0)
            ia.save()

        print c
        c+=1
        if LIMIT > 0 and c >= LIMIT:
            break
            
    
    
if __name__ == '__main__':

    insert_xml('_data/test-people_articles_filtered.txt')
    build_people_graph('_data/test-people_articles_filtered.txt')
    add_cats('_data/test-people_articles_filtered.txt')
    update_category_size()
