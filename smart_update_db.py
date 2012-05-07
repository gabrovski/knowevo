import re, os, sys, traceback, cPickle
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from gravebook.models import Article, Category, Other

from incunabula.models import Article as IArticle
from incunabula.models import MasterArticle  as IMasterArticle
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
            #raise
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
            #raise
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
        #print k
        ma.save()

        print c
        c+=1
        if LIMIT > 0 and c > LIMIT:
            break
        
def insert_incunabula_articles(revw):
    c = 0
    sizes = revw['sizes']
    for k in revw.keys():
        try:
            ma = IMasterArticle.objects.get(name=k)

            for ed in revw[k]['editions']:
                a = revw[k]['editions'][ed][0]

                ia = IArticle(name=a['name'], art_id=a['id'], art_ed=ed, text=a['txt'],
                              prank=0.0, volume_score= (0.0 + len(a['txt'])) / sizes[ed],
                              match_master=ma, 
                              match_score=-1.0)
                #print a['name']
                ia.save()
        except:
            continue
            
        print c
        c+=1
        if LIMIT > 0 and c > LIMIT:
            print 'wtf>'
            break
            
def process_split():
    for name in os.listdir('_data/split'):
        revw = load('_data/split/'+name)
        insert_incunabula_masters(revw)

    for name in os.listdir('_data/split'):
        revw = load('_data/split/'+name)
        insert_incunabula_articles(revw)


def update_inc_volume_score():
    eds = {3 : [], 9 : [], 11 : [], 15 : []}
    for art in IArticle.objects.all():
        eds[art.art_ed].append(len(art.text))

    print 'prepped text dict'

    avg = dict()
    for k in eds.keys():
        if eds[k] == []:
            continue
        avg[k] = sum(eds[k]) / len(eds[k])

    print 'computed averages'

    sdev = {3 : 0, 9 : 0, 11 : 0, 15 : 0}
    for k in eds.keys():
        for v in eds[k]:
            sdev[k] += (avg[k] - v)**2

    print 'computed variance'
    
    for k in sdev:
        sdev[k] = sdev[k]**0.5

    
    for art in IArticle.objects.all():
        art.volume_score = (len(art.text)-avg[art.art_ed]) / sdev[art.art_ed]
        art.save()


def extract_people_graph(out):
    f = open(out, 'w')
    c = 0
    for art in Article.objects.iterator():
        f.write(str(art.wid)+' ')

        for ed in art.people.iterator():
            f.write(str(ed.wid)+' ')
            
        f.write('\n')

        print c
        c+=1

    f.close()

def get_top_people(pranked, lim=200):
    f = open(pranked)
    arts = []
    for line in f:
        arts.append(line.strip().split(':'))
    f.close()
    
    arts.sort(key=lambda x: float(x[1]), reverse=True)
    
    for wid, prank in arts[:lim]:
        art = Article.objects.get(wid=wid)
        print art.name, prank


def get_inc_matched_edition():
    for art in IArticle.objects.iterator():
        print str(art)+'#'+str(art.art_ed)+'#'+str(art.match_master)
        

def fill_gr_linked_by():
    c = 0
    for art in Article.objects.iterator():
        for art_to in art.people.iterator():
            art_to.linked_by.add(art)
        print c
        c+=1
    
if __name__ == '__main__':
    #extract_people_graph('testgraph.txt')
    #get_top_people('testout.txt')
    #get_inc_matched_edition()
    fill_gr_linked_by()

    #update_inc_volume_score()

    '''
    revw = load('_data/sample_revw.pkl')
    insert_incunabula_masters(revw)
    insert_incunabula_articles(revw)
    #process_split()
    

    insert_xml('_data/test-people_articles_filtered.txt')
    build_people_graph('_data/test-people_articles_filtered.txt')
    add_cats('_data/test-people_articles_filtered.txt')

    update_category_size()
    '''

