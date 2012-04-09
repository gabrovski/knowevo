import re, os, sys, traceback
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from gravebook.models import Article, Category, Other, Link
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

def build_people_graph(path):
    f = open(path)
    count = 0

    for line in f:
        count += 1
        if count < START:
            continue

        print count,
        if LIMIT > 0 and count == LIMIT:
            break

        if count == 2:
           break

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
                    #print link
                    #raise
                    continue
             
                if them.birth == -1 or them.death == -1:
                    #print them.name
                    continue
                
                print 'adding', them.name
                if us.death < them.birth:
                    #print 'inf',
                    #us.influenced.add(them)
                    #them.influences.add(us)
                    l = Link(frm=us, to=them)
                    l.save()
                elif us.birth > them.death:
                    #print 'by',
                    l = Link(frm=them, to=us)
                    l.save()
                    #us.influences.add(them)
                    #them.influenced.add(us)
                else:
                    us.peers.add(them)
                them.save()
                us.save()

                #print repr(us.influenced.all()) + '\t' +repr(them.influences.all())
                #print them.name, repr(them.influences.all())
            us.save()
        except:
            raise
            continue
    f.close()
    
if __name__ == '__main__':
    insert_xml('_data/test-people_articles_filtered.txt')
    build_people_graph('_data/test-people_articles_filtered.txt')
