import re, os, sys, traceback
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from gravebook.models import Article, Category, Other
from django.db import transaction


tpat    = re.compile('title="(.+?)"')
idpat   = re.compile('id="(.+?)"')
imgpat  = re.compile('image="(.+?)"')
bpat    = re.compile('birth="(.+?)"')
dpat    = re.compile('death="(.+?)"')
plpat   = re.compile('people_links="(.+?)"')
olpat   = re.compile('other_links="(.+?)"')
catpat  = re.compile('categories="(.+?)"')

LIMIT = 500
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
    
if __name__ == '__main__':

    insert_xml('_data/people_articles_filtered.txt')
    build_people_graph('_data/people_articles_filtered.txt')
    add_cats('_data/people_articles_filtered.txt')
    update_category_size()
